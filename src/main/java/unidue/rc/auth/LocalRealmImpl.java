package unidue.rc.auth;


import miless.model.User;
import org.apache.cayenne.di.Inject;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.apache.shiro.subject.PrincipalCollection;
import unidue.rc.dao.*;
import unidue.rc.security.ShiroPermissionUtils;
import unidue.rc.system.SystemConfigurationService;

import java.util.*;

/**
 * @author Nils Verheyen
 * @since 17.07.13 09:43
 */
public class LocalRealmImpl extends LocalRealm {

    private static final String cacheManagerConfigFile = "classpath:local_ehcache.xml";

    @Inject
    private UserDAO userDAO;

    @Inject
    private ShiroPermissionUtils shiroPermissionUtils;

    public LocalRealmImpl(@Inject SystemConfigurationService config) {

        setName(NAME);
        setAuthenticationTokenClass(UsernamePasswordToken.class);
        setCredentialsMatcher(new HashedCredentialsMatcher(Md5Hash.ALGORITHM_NAME));

        boolean useCache = config.getBoolean("security.cache.enabled", false);
        if (useCache) {
            EhCacheManager cacheManager = new EhCacheManager();
            cacheManager.setCacheManagerConfigFile(cacheManagerConfigFile);
            setCacheManager(cacheManager);
        }
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {

        UsernamePasswordToken upToken = (UsernamePasswordToken) token;

        String username = upToken.getUsername();
        String password = new String(upToken.getPassword());

        // Null username is invalid
        if (username == null) { throw new AccountException("Null usernames are not allowed by this realm."); }

        User user = userDAO.getUser(username, NAME);
        if (user == null)
            throw new AccountException("Unknown user in local realm with username " + username);

        String md5Password = DigestUtils.md5Hex(password);

        // check password
        if (!md5Password.equals(user.getPassword())) {
            throw new AuthenticationException("authentication of user " + username + " failed");
        }
        user.setLastlogin(new Date());
        try {
            userDAO.update(user);
        } catch (CommitException e) {
            throw new AuthenticationException("unable to update user", e);
        }

        return new SimpleAuthenticationInfo(username, md5Password, NAME);
    }

    @Override
    public boolean supports(AuthenticationToken token) {
        if (token instanceof CollectionAuthenticationToken) {
            CollectionAuthenticationToken t = (CollectionAuthenticationToken) token;
            return NAME.equals(t.getRealm());
        }
        return false;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        return shiroPermissionUtils.buildAuthorizationInfo(principals, NAME);
    }

    @Override
    public boolean exists(String username) {
        return userDAO.exists(username);
    }
}
