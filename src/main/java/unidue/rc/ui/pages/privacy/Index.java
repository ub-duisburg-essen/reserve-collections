package unidue.rc.ui.pages.privacy;


import miless.model.User;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Cookies;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;
import org.slf4j.Logger;
import se.unbound.tapestry.breadcrumbs.BreadCrumb;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.UserDAO;
import unidue.rc.security.CollectionSecurityService;

/**
 * Created by marcus.koesters on 29.06.15.
 */

@BreadCrumb(titleKey = "privacy.title")
public class Index {

    public static final String TRACKING_COOKIE = "trackReserveCollections";
    public static final String TRACKING_PERMITTED = "doTrack";
    public static final String TRACKING_REJECTED = "doNotTrack";

    @Inject
    private Logger log;

    @Inject
    private Cookies cookies;

    @Inject
    private Request request;

    @Inject
    private Messages messages;

    @Inject
    private AjaxResponseRenderer ajaxResponseRenderer;

    @InjectComponent
    private Zone privacyObjectionZone;

    @Property
    private boolean isTrackingPermitted;

    @Inject
    private CollectionSecurityService securityService;

    @Inject
    private UserDAO userDAO;

    @SetupRender
    void init() {
        String cookieValue = cookies.readCookieValue(TRACKING_COOKIE);
        isTrackingPermitted = cookieValue != null && cookieValue.equals(TRACKING_PERMITTED);
    }

    @OnEvent(value = EventConstants.SUCCESS, component = "privacyObjectionForm")
    public Object onSetConsent() {

        String cookieValue = isTrackingPermitted
                             ? TRACKING_PERMITTED
                             : TRACKING_REJECTED;
        cookies.writeCookieValue(TRACKING_COOKIE, cookieValue);
        updateCurrentUser(isTrackingPermitted);
        return request.isXHR()
               ? privacyObjectionZone.getBody()
               : null;
    }

    private void updateCurrentUser(boolean isTrackingAllowed) {
        User user = securityService.getCurrentUser();
        if (user != null) {
            user.setIsTrackingAllowed(isTrackingAllowed);
            try {
                userDAO.update(user);
            } catch (CommitException e) {
                log.error("could not update user " + user, e);
            }
        }
    }

}
