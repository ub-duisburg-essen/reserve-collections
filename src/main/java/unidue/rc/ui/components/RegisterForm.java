package unidue.rc.ui.components;

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

import miless.model.MCRCategory;
import miless.model.User;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.ValidationException;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.PasswordField;
import org.apache.tapestry5.corelib.components.TextField;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.slf4j.Logger;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.MCRCategoryDAO;
import unidue.rc.dao.OriginDAO;
import unidue.rc.auth.LocalRealm;
import unidue.rc.dao.UserDAO;
import unidue.rc.security.CollectionSecurityService;
import unidue.rc.ui.selectmodel.OriginSelectModel;
import unidue.rc.ui.valueencoder.MCRCategoryValueEncoder;

import java.util.Locale;

@Import(library = {
        "context:js/messages.js", // will be localized when the page is created 
        "context:vendor/jquery-chosen/chosen/chosen.jquery.js",
        "context:js/apply.jquery.chosen.js"
}, stylesheet = {
        "context:vendor/jquery-chosen/chosen/chosen.css"
})
public class RegisterForm {

    @Inject
    private Logger log;

    @Environmental
    private JavaScriptSupport javascriptSupport;

    @Inject
    private Messages messages;

    @Component(id = "registerForm")
    private Form form;

    @InjectComponent("newPassword")
    private PasswordField passwordField;

    @InjectComponent("newUsername")
    private TextField usernameField;

    @InjectComponent("captcha")
    @Property
    private ReCaptcha captcha;

    @Property
    @Validate("required")
    private String newUsername;

    @Property
    @Validate("required")
    private String newPassword;

    @Property
    @Validate("required")
    private String passwordRepetition;

    @Property
    private String academicTitle;

    @Property
    @Validate("required")
    private String forename;

    @Property
    @Validate("required")
    private String surname;

    @Property
    @Validate("required")
    private MCRCategory origin;

    @Property
    private String address;

    @Property
    private String phone;

    @Property
    @Validate("required")
    private String mail;

    @Property
    private Boolean isCaptchaValid;

    @Inject
    private MCRCategoryDAO mcrCategoryDAO;

    @Inject
    private OriginDAO originDAO;

    @Inject
    private UserDAO userDAO;

    @Inject
    private CollectionSecurityService securityService;

    @Inject
    private Locale locale;

    @OnEvent(value = EventConstants.VALIDATE, component = "registerForm")
    void onValidateForm() throws ValidationException {

        if (!securityService.isUsernameValid(newUsername))
            form.recordError(usernameField, messages.get("username-invalid-message"));

        if (securityService.exists(newUsername))
            form.recordError(usernameField, messages.get("user.already.exists"));

        if (!isCaptchaValid)
            form.recordError(captcha, messages.get("invalid.captcha"));

        if (newPassword != null && !newPassword.equals(passwordRepetition))
            form.recordError(passwordField, messages.get("passwords.do.not.match"));
    }

    @OnEvent(EventConstants.SUCCESS)
    void onFormSubmitted() {

        User user = new User();
        user.setEmail(mail);
        user.setUsername(newUsername);
        user.setRealname(surname + ", " + forename);
        user.setOrigin(origin.getCategid());
        user.setRealm(LocalRealm.NAME);
        user.setPassword(DigestUtils.md5Hex(newPassword));
        user.setOwnerid(0);

        try {
            userDAO.create(user);
            securityService.login(user.getUsername(), newPassword);
            log.info("user " + user + " successfully created");
        } catch (CommitException e) {
            form.recordError(messages.get("error.message.could.not.create.user"));
        }

    }

    public String getClassForCaptchaError() {
        return isCaptchaValid == null || isCaptchaValid ? "hide" : "";
    }

    /**
     * Returns the {@link SelectModel} that is used inside html select box to
     * set the origin as {@link MCRCategory} for the new reserve collection.
     *
     * @return the select model
     */
    public SelectModel getOriginSelectModel() {
        return new OriginSelectModel(originDAO, locale);
    }

    public ValueEncoder<MCRCategory> getOriginEncoder() {
        return new MCRCategoryValueEncoder(mcrCategoryDAO);
    }
}
