package unidue.rc.auth;


import miless.model.User;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;

/**
 * @author Nils Verheyen
 * @since 17.07.13 10:47
 */
public abstract class LocalRealm extends AuthorizingRealm {

    public static final String NAME = "local";

    /**
     * Clears authorization data of current authenticated subject by the use of
     * {@link org.apache.shiro.realm.AuthorizingRealm#doClearCache(PrincipalCollection)}.
     *
     * @param user the user which auth cache should be cleared
     */
    public void clearAuthCache(User user) {
        SimplePrincipalCollection principals = new SimplePrincipalCollection(user.getUsername(), NAME);
        super.clearCachedAuthorizationInfo(principals);
    }


    /**
     * Checks if a user with target username exists inside the ldap directory.
     *
     * @param username the username that should be checked
     * @return <code>true</code> if a user with given username exists
     */
    public abstract boolean exists(String username);
}
