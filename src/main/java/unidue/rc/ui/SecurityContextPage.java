package unidue.rc.ui;


import org.apache.shiro.authz.AuthorizationException;
import org.apache.tapestry5.EventContext;
import unidue.rc.security.CollectionSecurityService;

/**
 * A <code>SecurityContextPage</code> is a tapestry page that displays details of parts of a
 * {@link unidue.rc.model.ReserveCollection}.
 * <p>
 * Created by nils on 11.06.15.
 */
public interface SecurityContextPage {

    /**
     * Checks if current user is authorized to view target page.
     *
     * @param securityService   contains the security service that should be used to check authorization
     * @param activationContext contains the activation context that will be used on page activation ({@link org.apache.tapestry5.EventConstants#ACTIVATE})
     * @throws AuthorizationException thrown if the user is not authorized
     */
    void checkPermission(CollectionSecurityService securityService, EventContext activationContext) throws AuthorizationException;
}
