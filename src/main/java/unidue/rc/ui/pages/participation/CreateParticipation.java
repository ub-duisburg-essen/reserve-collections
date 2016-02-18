package unidue.rc.ui.pages.participation;

/*
 * #%L
 * Semesterapparate
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 - 2015 Universitaet Duisburg Essen
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import miless.model.User;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.TextField;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.HttpError;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.SelectModelFactory;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;
import org.slf4j.Logger;
import se.unbound.tapestry.breadcrumbs.BreadCrumb;
import se.unbound.tapestry.breadcrumbs.BreadCrumbList;
import unidue.rc.dao.*;
import unidue.rc.model.ActionDefinition;
import unidue.rc.model.DefaultRole;
import unidue.rc.model.Participation;
import unidue.rc.model.PermissionDefinition;
import unidue.rc.model.ReserveCollection;
import unidue.rc.model.Role;
import unidue.rc.security.CollectionSecurityService;
import unidue.rc.security.RequiresActionPermission;
import unidue.rc.ui.ProtectedPage;
import unidue.rc.ui.valueencoder.RoleValueEncoder;
import unidue.rc.workflow.CollectionService;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by marcus.koesters on 29.04.15.
 */

@BreadCrumb(titleKey = "add.participation")
@ProtectedPage
public class CreateParticipation {

    @Inject
    private Logger log;

    @Inject
    private Request request;


    @Inject
    private PageRenderLinkSource linkSource;

    @Inject
    private Messages messages;

    @SessionState
    private BreadCrumbList breadCrumbList;

    @Inject
    private ReserveCollectionDAO collectionDAO;

    @Inject
    private UserDAO userDAO;

    @Inject
    private RoleDAO roleDAO;

    @Inject
    private ParticipationDAO participationDAO;

    @Inject
    SelectModelFactory selectModelFactory;

    @Inject
    CollectionService collectionService;

    @Property
    private ReserveCollection collection;


    @Property
    @Persist
    private SelectModel roleSelectModel;

    @Inject
    private CollectionSecurityService securityService;

    @Component(id = "userSearchForm")
    private Form userSearchForm;

    @Component(id = "chooseUserForm")
    private Form chooseUserForm;


    @Component(id = "userSearch")
    private TextField searchField;

    @Property
    @Validate("required")
    private String userSearch;

    @Property(write = false)
    @Persist(PersistenceConstants.FLASH)
    private String message;

    @Property
    @Persist(PersistenceConstants.FLASH)
    private List<UserRoleMapper> userRoleMappers;

    private List<UserRoleMapper> participationsToAdd;


    @Property
    private UserRoleMapper mapping;

    @InjectComponent
    private Zone chooseUserZone;

    @InjectComponent
    private Zone userSearchZone;

    @Inject
    private AjaxResponseRenderer ajaxResponseRenderer;

    private boolean inFormSubmission;

    @SetupRender
    public void onSetupRender() {

        User loggedInUser = securityService.getCurrentUser();
        roleDAO.getRoles(loggedInUser);


        List<Role> roles = roleDAO.getRoles();
        List<Role> instanceBoundRoles = new ArrayList<>();
        for (Role role : roles) {
            for (PermissionDefinition permdef : role.getPermissionDefinitions()) {
                if (permdef.isIsInstanceBound() & !instanceBoundRoles.contains(role)) {

                    if (checkEditPermissionForRole(role)) {
                        instanceBoundRoles.add(role);
                    }

                }
            }

        }

        roleSelectModel = selectModelFactory.create(sortRoles(instanceBoundRoles),
                Role.NAME_PROPERTY);


    }


    @OnEvent(EventConstants.ACTIVATE)
    @RequiresActionPermission(ActionDefinition.EDIT_RESERVE_COLLECTION)
    Object onPageActivate(int reserveCollectionId) {
        collection = collectionDAO.get(ReserveCollection.class, reserveCollectionId);
        // load resources
        if (collection == null)
            return new HttpError(HttpServletResponse.SC_NOT_FOUND, messages.get("error.msg.collection.not.found"));
        return null;
    }

    @OnEvent(value = EventConstants.VALIDATE, component = "userSearchForm")
    void onValidateSearchWord() {

        log.debug("searching for " + userSearch);

        if (userSearch == null || userSearch.isEmpty())
            userSearchForm.recordError(searchField, messages.get("search-required-message"));
    }


    @OnEvent(value = EventConstants.SUCCESS, component = "userSearchForm")
    void onSearch() {

        List<User> users = userDAO.search(userSearch);
        userRoleMappers = new ArrayList<>();
        for (User user : users) {
            Role activeRole = null;
            Participation participation = participationDAO.getActiveParticipation(user, collection);
            if (participation != null) {
                activeRole = participation.getRole();
            }
            userRoleMappers.add(new UserRoleMapper(user, activeRole));
        }

        message = (users == null || users.isEmpty())
                ? messages.get("error.msg.search.no.results")
                : null;

        if (request.isXHR())
            ajaxResponseRenderer.addRender(userSearchZone).addRender(chooseUserZone);
    }

    public boolean getIsSearchEmpty() {
        return userSearch != null && message != null;
    }

    @OnEvent(value = EventConstants.PREPARE_FOR_SUBMIT, component = "chooseUserForm")
    void onPrepareForSubmit() {

        inFormSubmission = true;
    }

    @OnEvent(value = EventConstants.SUCCESS, component = "chooseUserForm")
    Object onSubmit() {

        for (UserRoleMapper mapping : userRoleMappers) {
            createParticipation(mapping.getRole(), mapping.getUser());
        }
        Link viewCollectionLink = linkSource.createPageRenderLinkWithContext(ViewParticipation.class,
                collection.getId());

        return viewCollectionLink;

    }


    @OnEvent(EventConstants.PASSIVATE)
    Integer onPagePassivate() {
        return collection != null ? collection.getId() : null;
    }


    public ValueEncoder<Role> getRoleValueEncoder() {
        return new RoleValueEncoder(roleDAO);
    }

    private List<Role> sortRoles(List<Role> result) {
        return result.stream()
                .sorted((r1, r2) -> r1.getName().compareTo(r2.getName()))
                .collect(Collectors.toList());
    }

    private void createParticipation(Role role, User user) {

        try {
            collectionService.createParticipation(user, collection, role);
        } catch (CommitException e) {
            log.error("Could not create participation for user " + user.getUsername() + " on collection " + collection.getId(), e);
        } catch (DeleteException e) {
            log.error("Could not end current participation for user " + user.getUsername() + " on collection " + collection.getId(), e);
        }
    }

    @Property
    private final ValueEncoder<UserRoleMapper> encoder = new ValueEncoder<UserRoleMapper>() {
        public String toClient(UserRoleMapper value) {
            return String.valueOf(userRoleMappers.indexOf(value));
        }

        public UserRoleMapper toValue(String clientValue) {
            int id = Integer.parseInt(clientValue);

            return userRoleMappers.get(id);
        }
    };

    public class UserRoleMapper {

        private User user;

        private Role role;

        private String roleName = "";

        public UserRoleMapper(User user, Role role) {
            this.user = user;
            this.role = role;
            if (role != null) {
                this.roleName = role.getName();
            }
        }

        public void setUser(User user) {
            this.user = user;
        }

        public void setRole(Role role) {
            this.role = role;
            if (role != null) {
                this.roleName = role.getName();
            }
        }


        public User getUser() {
            return user;
        }

        public Role getRole() {
            return role;
        }

        public String getRoleName() {
            return roleName;
        }

    }

    public boolean isDocentRole(Role role) {

        return role.getName().equals(DefaultRole.DOCENT.getName());
    }

    public boolean isAssistantRole(Role role) {

        return role.getName().equals(DefaultRole.ASSISTANT.getName());
    }

    public boolean isStudentRole(Role role) {

        return role.getName().equals(DefaultRole.STUDENT.getName());
    }

    public boolean isCustomRole(Role role) {

        return !role.getIsDefault();
    }

    private boolean currentUserHasPermission(ActionDefinition action, Integer objectID) {
        try {
            if (objectID != null)
                securityService.checkPermission(action, objectID);
            else
                securityService.checkPermission(action);
            return true;
        } catch (AuthorizationException e) {
            return false;
        }
    }

    /**
     * Checks if the current user as the necessary permission to assign a given instancebound {@link Role}
     *
     * @param role role to check
     * @return <code>true</code> if current user is allowed to perform actions on target role
     */
    public boolean checkEditPermissionForRole(Role role) {
        if (role == null)
            return true; //No limitations for Users that have no applied instance bound rules. Those may be edited by the lowest level of users.
        if (isDocentRole(role) && currentUserHasPermission(ActionDefinition.EDIT_DOCENT_PARTICIPATION, collection.getId()))
            return true;
        if (isAssistantRole(role) && currentUserHasPermission(ActionDefinition.EDIT_ASSISTANT_PARTICIPATION, collection.getId()))
            return true;
        if (isStudentRole(role) && currentUserHasPermission(ActionDefinition.EDIT_STUDENT_PARTICIPATION, collection.getId()))
            return true;
        if (isCustomRole(role) && currentUserHasPermission(ActionDefinition.EDIT_STUDENT_PARTICIPATION, collection.getId()))
            return true;
        return false;
    }
}

