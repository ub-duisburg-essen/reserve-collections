package unidue.rc.ui.pages.user;

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

import miless.model.MCRCategory;
import miless.model.User;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.tapestry5.*;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.TextField;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;
import se.unbound.tapestry.breadcrumbs.BreadCrumb;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.MCRCategoryDAO;
import unidue.rc.dao.OriginDAO;
import unidue.rc.dao.UserDAO;
import unidue.rc.model.ActionDefinition;
import unidue.rc.model.Membership;
import unidue.rc.security.CollectionSecurityService;
import unidue.rc.system.CryptService;
import unidue.rc.ui.ProtectedPage;
import unidue.rc.ui.SecurityContextPage;
import unidue.rc.ui.selectmodel.OriginSelectModel;
import unidue.rc.ui.valueencoder.BaseValueEncoder;
import unidue.rc.ui.valueencoder.MCRCategoryValueEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by nils on 30.07.15.
 */
@BreadCrumb(titleKey = "user.page.title")
@ProtectedPage
public class Index implements SecurityContextPage {

    @Inject
    private CryptService cryptService;

    @Inject
    @Service(UserDAO.SERVICE_NAME)
    private UserDAO userDAO;

    @Inject
    private OriginDAO originDAO;

    @Inject
    private MCRCategoryDAO mcrcategoryDAO;

    @Inject
    private Locale locale;

    @Inject
    private Request request;

    @Inject
    private Messages messages;

    @Inject
    private AjaxResponseRenderer ajaxResponseRenderer;

    // working fields

    @Component(id = "edit_user_form")
    private Form userForm;

    @Component(id = "realname")
    private TextField realnameField;

    @Component(id = "email")
    private TextField emailField;

    @Component(id = "userSearchForm")
    private Form userSearchForm;

    @Component(id = "userSearch")
    private TextField searchField;

    @InjectComponent
    private Zone userZone;

    @InjectComponent
    private Zone userSearchZone;

    @InjectComponent
    private Zone chooseUserZone;

    // screen values

    @Property
    @Persist(PersistenceConstants.FLASH)
    private String userSearch;

    @Property
    private User user;

    @Property
    private String password;

    @Property
    private String passwordRepeat;

    @Property
    private MCRCategory origin;

    // user to loop over user list
    @Property
    private List<User> users;

    @OnEvent(EventConstants.ACTIVATE)
    void onActivate(Integer userID) {
        loadUser(userID);
    }

    private void loadUser(Integer userID) {
        this.user = userDAO.getUserById(userID);
        this.origin = originDAO.getOrigin(user.getOrigin());
    }

    @OnEvent(EventConstants.PASSIVATE)
    Integer onPassivate() {
        return user != null ? user.getId() : null;
    }


    @OnEvent(value = EventConstants.VALIDATE, component = "userSearchForm")
    void onValidateSearchWord() {

        if (userSearch == null || userSearch.isEmpty())
            userSearchForm.recordError(searchField, messages.get("search-required-message"));
    }

    @OnEvent(value = EventConstants.SUCCESS, component = "userSearchForm")
    void onSearch() {

        // load books an put them to session for later submission
        users = userDAO.search(userSearch);

        if (request.isXHR())
            ajaxResponseRenderer.addRender(userSearchZone).addRender(chooseUserZone);
    }

    @OnEvent(component = "choose")
    void onUserChosen(User user) {
        loadUser(user.getId());

        if (request.isXHR())
            ajaxResponseRenderer.addRender(userSearchZone).addRender(userZone);
    }

    @OnEvent(value = EventConstants.VALIDATE, component = "edit_user_form")
    void onValidateFromUserForm() {
        if (StringUtils.isEmpty(user.getRealname()))
            userForm.recordError(realnameField, messages.get("realname-required-message"));

        if (StringUtils.isEmpty(user.getEmail()))
            userForm.recordError(emailField, messages.get("email-required-message"));

        if (!StringUtils.isEmpty(password) && !password.equals(passwordRepeat))
            userForm.recordError(emailField, messages.get("check-password-repetition-message"));
    }

    @OnEvent(value = EventConstants.SUCCESS, component = "edit_user_form")
    void onSuccessFromUserForm() {
        try {

            if (!StringUtils.isEmpty(password))
                user.setPassword(DigestUtils.md5Hex(password));

            if (origin != null)
                user.setOrigin(origin.getCategid());

            userDAO.update(user);
        } catch (CommitException e) {
            userForm.recordError(messages.get("error.msg.could.not.commit.user"));
        }
    }

    public SelectModel getOriginSelectModel() {
        return new OriginSelectModel(originDAO, locale);
    }

    public ValueEncoder<MCRCategory> getOriginEncoder() {
        return new MCRCategoryValueEncoder(mcrcategoryDAO);
    }

    @Override
    public void checkPermission(CollectionSecurityService securityService, EventContext activationContext) throws AuthorizationException {
        User currentUser = securityService.getCurrentUser();
        Integer targetUserID = activationContext.get(Integer.class, 0);
        if (currentUser != null
                // current user wants to edit his own details
                && currentUser.getId().equals(targetUserID)
                // but has no access
                && !securityService.isPermitted(ActionDefinition.EDIT_USER, targetUserID)) {

            // create permissions
            try {
                securityService.createInstancePermissions(targetUserID, targetUserID, User.class, ActionDefinition.EDIT_USER);
            } catch (CommitException e) {
                throw new AuthorizationException(e);
            }
        }
        if (targetUserID == null)
            securityService.checkPermission(ActionDefinition.EDIT_USER);
        else
            securityService.checkPermission(ActionDefinition.EDIT_USER, targetUserID);
    }

    public ValueEncoder<User> getUserEncoder() {
        return new BaseValueEncoder<>(User.class, userDAO);
    }
}
