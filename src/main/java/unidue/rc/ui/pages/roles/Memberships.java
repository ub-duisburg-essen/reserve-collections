/**
 * Copyright (C) 2014 - 2016 Universitaet Duisburg-Essen (semapp|uni-due.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package unidue.rc.ui.pages.roles;

/*
 * #%L
 * Semesterapparate
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 Universitaet Duisburg Essen
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
import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.TextField;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.SelectModelFactory;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;
import org.slf4j.Logger;
import se.unbound.tapestry.breadcrumbs.BreadCrumb;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.DeleteException;
import unidue.rc.dao.RoleDAO;
import unidue.rc.dao.UserDAO;
import unidue.rc.model.ActionDefinition;
import unidue.rc.model.Membership;
import unidue.rc.model.Role;
import unidue.rc.security.RequiresActionPermission;
import unidue.rc.ui.ProtectedPage;
import unidue.rc.ui.valueencoder.BaseValueEncoder;
import unidue.rc.ui.valueencoder.RoleValueEncoder;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * @author Nils Verheyen
 * @since 06.08.14 16:24
 */
@Import(library = {
        "context:vendor/spin.min.js"
})
@BreadCrumb(titleKey = "memberships.page.title")
@ProtectedPage
public class Memberships {

    @Inject
    private Logger log;

    @Inject
    private RoleDAO roleDAO;

    @Inject
    private UserDAO userDAO;

    @Inject
    SelectModelFactory selectModelFactory;

    @Inject
    private Messages messages;

    @Inject
    private Request request;

    @Inject
    private AjaxResponseRenderer ajaxResponseRenderer;

    // Working fields

    @InjectComponent
    private Zone userSearchZone;

    @InjectComponent
    private Zone chooseUserZone;

    @InjectComponent
    private Zone membershipZone;

    @Component(id = "userSearchForm")
    private Form userSearchForm;

    @Component(id = "userSearch")
    private TextField searchField;

    // Screen fields

    @Property
    private boolean isSearchEmpty;

    @Property(write = false)
    @Persist(PersistenceConstants.FLASH)
    private String addedMessage;

    @Property(write = false)
    @Persist(PersistenceConstants.FLASH)
    private String removedMessage;

    @Property
    @Persist(PersistenceConstants.FLASH)
    private String userSearch;

    // user to loop over user list
    @Property
    private User user;

    @Property
    private List<User> users;

    // chosen user to edit memberships
    @Property
    @Persist(PersistenceConstants.FLASH)
    private User chosenUser;

    @Property
    @Persist(PersistenceConstants.FLASH)
    private List<Role> selectedRoles;

    @SetupRender
    @RequiresActionPermission(ActionDefinition.EDIT_USER_ROLES)
    void onSetupRender() {}

    @OnEvent(value = EventConstants.VALIDATE, component = "userSearchForm")
    void onValidateSearchWord() {

        log.debug("searching for " + userSearch);

        if (userSearch == null || userSearch.isEmpty())
            userSearchForm.recordError(searchField, messages.get("search-required-message"));
    }

    @OnEvent(value = EventConstants.FAILURE, component = "userSearchForm")
    void onFailureFromSearch() {
        if (request.isXHR())
            ajaxResponseRenderer.addRender(userSearchZone);
    }

    @OnEvent(value = EventConstants.SUCCESS, component = "userSearchForm")
    void onSearch() {

        // load books an put them to session for later submission
        users = userDAO.search(userSearch);

        addedMessage = (users == null || users.isEmpty())
                ? messages.get("error.msg.search.no.results")
                : null;

        if (request.isXHR())
            ajaxResponseRenderer.addRender(userSearchZone).addRender(chooseUserZone);
    }

    @OnEvent(component = "choose")
    void onUserChosen(User user) {
        log.debug("chosen user " + user);
        this.chosenUser = user;

        List<Membership> membership = roleDAO.getMembership(chosenUser);
        this.selectedRoles = new ArrayList<>();
        for (Membership r : membership)
            selectedRoles.add(r.getRole());

        if (request.isXHR())
            ajaxResponseRenderer.addRender(userSearchZone).addRender(membershipZone);
    }

    @OnEvent(value = EventConstants.SUBMIT, component = "membershipForm")
    void onRolesSelected() {
        final List<Membership> currentMemberships = roleDAO.getMembership(chosenUser);

        // remove all roles from current membership which are not in selected roles
        List<Membership> deselectedMemberships = currentMemberships.stream()
                .filter(membership ->!selectedRoles.contains(membership.getRole()))
                .collect(Collectors.toList());
        String removedRoles = StringUtils.join(deselectedMemberships.stream()
                .map(membership -> membership.getRole().getName())
                .collect(Collectors.toList()), ", ");

        // delete deselected membership
        for (Membership deselectedMembership : deselectedMemberships) {
            try {
                roleDAO.delete(deselectedMembership);
            } catch (DeleteException e) {
                log.error("could not delete membership " + deselectedMembership, e);
            }
        }
        currentMemberships.removeAll(deselectedMemberships);

        // find new roles
        List<Role> newRoles = selectedRoles.stream()
                .filter(role ->
                    !currentMemberships.stream()
                            .filter(membership -> membership.getRole().equals(role))
                            .findAny()
                            .isPresent())
                .collect(Collectors.toList());

        // create new membership object for each new role
        for (Role r : newRoles) {
            unidue.rc.model.Membership membership = new unidue.rc.model.Membership();
            membership.setUserID(chosenUser.getId());
            membership.setRole(r);
            try {
                roleDAO.create(membership);
            } catch (CommitException e) {
                log.error("could not create membership " + membership, e);
            }
        }
        String addedRoles = StringUtils.join(newRoles.stream()
                .map(role -> role.getName())
                .collect(Collectors.toList()), ", ");

        if (newRoles.size() > 0)
            addedMessage = messages.format("msg.added.membership", chosenUser.getRealname(), addedRoles);
        if (deselectedMemberships.size() > 0)
            removedMessage = messages.format("msg.removed.membership", chosenUser.getRealname(), removedRoles);
    }

    public ValueEncoder<Role> getRoleEncoder() {
        return new RoleValueEncoder(roleDAO);
    }

    public ValueEncoder<User> getUserEncoder() {
        return new BaseValueEncoder<>(User.class, roleDAO);
    }

    public SelectModel getRoleSelectModel() {
        return selectModelFactory.create(roleDAO.getRoles(), "name");
    }
}
