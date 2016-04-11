package unidue.rc.ui.pages.login;


import org.apache.shiro.authc.AuthenticationException;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.TextField;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.RequestGlobals;
import org.slf4j.Logger;
import unidue.rc.security.CollectionSecurityService;
import unidue.rc.system.SystemConfigurationService;
import unidue.rc.ui.Referrer;

public class Index {

    private static final String ACTIVE_LINK_CLASS = "active";
    private static final String ACTIVE_COMPONENT_CLASS = "show"; // bootstrap.css
    private static final String INACTIVE_COMPONENT_CLASS = "hide"; // bootstrap.css

    @Inject
    private Logger log;

    @Persist(PersistenceConstants.FLASH)
    @Property
    private Boolean isLoginVisible;

    @Persist(PersistenceConstants.FLASH)
    @Property
    private Boolean isRegisterVisible;

    @Inject
    private SystemConfigurationService sysConfig;

    @Inject
    private PageRenderLinkSource linkSource;

    @Inject
    private RequestGlobals globals;

    @Inject
    private Messages messages;

    @Inject
    private CollectionSecurityService securityService;

    @SessionState
    private Referrer referrer;

    @Property(write = false)
    @Persist(PersistenceConstants.FLASH)
    private String unauthorizedMessage;

    @Component(id = "loginForm")
    private Form loginForm;

    @Component(id = "username")
    private TextField usernameField;

    @Property
    private String username;

    @Property
    private String password;

    @SetupRender
    void beforeRender() {

        /*
         * set both visible markers to false, that on action from local login
         * link or register link these variables are available
         */
        isLoginVisible = isLoginVisible == null
                ? Boolean.TRUE
                : isLoginVisible;
        isRegisterVisible = isRegisterVisible == null
                ? Boolean.FALSE
                : isRegisterVisible;

        unauthorizedMessage = globals.getRequest().getParameterNames().contains("unauthorized")
                ? messages.get("error.msg.unauthorized")
                : null;
    }

    @OnEvent(component = "login")
    Object onActionFromLogin() {

        setComponentVisibility(Boolean.TRUE, Boolean.FALSE);

        return Index.class;
    }

    @OnEvent(component = "register")
    Object onActionFromRegister() {

        setComponentVisibility(Boolean.FALSE, Boolean.TRUE);

        return Index.class;
    }

    /**
     * This method is called after form inside {@link unidue.rc.ui.components.RegisterForm} component was successfully
     * submitted.
     *
     * @return
     */
    Object onSuccessFromRegisterForm() {

        setComponentVisibility(Boolean.TRUE, Boolean.FALSE);
        return referrer != null && referrer.getPageLink() != null
                ? referrer.getPageLink()
                : unidue.rc.ui.pages.Index.class;
    }

    Object onFailureFromRegisterForm() {

        setComponentVisibility(Boolean.FALSE, Boolean.TRUE);
        return Index.class;
    }

    /**
     * Called after form of login component was submitted.
     *
     * @return page which should be shown. If login was successful the referrer is returned, otherwise the login page
     * again
     */
    @OnEvent(value = EventConstants.VALIDATE, component = "loginForm")
    void onValidateFromLoginForm() {

        try {

            securityService.login(username, password);
        } catch (AuthenticationException e) {
            log.error("auth of user '" + username  + "' failed", e);
            loginForm.recordError(usernameField, messages.get("error.msg.invalid.auth.data"));
            setComponentVisibility(Boolean.TRUE, Boolean.FALSE);
        }
    }

    @OnEvent(value = EventConstants.SUCCESS, component = "loginForm")
    Object onSuccessFromLoginForm() {

        return referrer != null && referrer.getPageLink() != null
                ? referrer.getPageLink()
                : unidue.rc.ui.pages.Index.class;
    }

    /**
     * Sets the attributes of this page which components should be visible or invisible.
     *
     * @param isLoginVisible    {@linkplain Boolean#TRUE} if the component for local login should be visible,
     *                          {@linkplain Boolean#FALSE} otherwise
     * @param isRegisterVisible {@linkplain Boolean#TRUE} if the component for registering a new user should be
     *                          visible, {@linkplain Boolean#FALSE} otherwise
     */
    private void setComponentVisibility(Boolean isLoginVisible, Boolean isRegisterVisible) {

        this.isLoginVisible = isLoginVisible;
        this.isRegisterVisible = isRegisterVisible;
    }

    public String getClassForLoginLink() {
        return isLoginVisible
                ? ACTIVE_LINK_CLASS
                : "";
    }

    public String getClassForRegisterLink() {
        return isRegisterVisible
                ? ACTIVE_LINK_CLASS
                : "";
    }
}