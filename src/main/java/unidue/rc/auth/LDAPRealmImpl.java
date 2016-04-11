package unidue.rc.auth;


import miless.model.User;
import org.apache.cayenne.di.Inject;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authc.credential.AllowAllCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.realm.ldap.LdapContextFactory;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.UserDAO;
import unidue.rc.security.ShiroPermissionUtils;
import unidue.rc.system.SystemConfigurationService;

import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.util.Date;
import java.util.Properties;

/**
 * The <code>LDAPRealmImpl</code> provides all functionality of an {@linkplain LDAPRealm}. An instance is configured
 * through this application <code>sysconfig.xml</code>. Have a look at the ldap section for detailed description
 * about single properties
 *
 * @author Nils Verheyen
 * @since 23.07.13 11:22
 */
public class LDAPRealmImpl extends LDAPRealm {

    private static Logger LOG = LoggerFactory.getLogger(LDAPRealm.class);

    private static final String cacheManagerConfigFile = "classpath:ude_ehcache.xml";

    private final SystemConfigurationService config;

    @Inject
    private UserDAO userDAO;

    @Inject
    private ShiroPermissionUtils shiroPermissionUtils;

    public LDAPRealmImpl(@Inject SystemConfigurationService config) {

        this.config = config;

        setName(NAME);
        setUrl(config.getString("ldap.provider.url"));
        setCredentialsMatcher(new AllowAllCredentialsMatcher());

        boolean useCache = this.config.getBoolean("security.cache.enabled", false);
        if (useCache) {
            EhCacheManager cacheManager = new EhCacheManager();
            cacheManager.setCacheManagerConfigFile(cacheManagerConfigFile);
            setCacheManager(cacheManager);
        }
    }

    @Override
    protected AuthenticationInfo queryForAuthenticationInfo(AuthenticationToken token, LdapContextFactory ldapContextFactory) throws NamingException {
        UsernamePasswordToken authenticationToken = (UsernamePasswordToken) token;
        String username = userDAO.normalizeUsername(authenticationToken.getUsername());
        String password = new String(authenticationToken.getPassword());

        // load user of db
        try {
            login(username, password);
        } catch (CommitException e) {
            throw new AuthenticationException("could not update user details", e);
        }

        return new SimpleAuthenticationInfo(username, DigestUtils.md5(password), NAME);
    }

    @Override
    protected AuthorizationInfo queryForAuthorizationInfo(PrincipalCollection principal, LdapContextFactory ldapContextFactory) throws NamingException {
        return shiroPermissionUtils.buildAuthorizationInfo(principal, NAME);
    }

    private User login(String username, String password) throws NamingException, CommitException {

        DirContext authenticationContext = null;
        try {
            // principal for user login is "uid=<username>,ou=..."
            String principal = String.format(config.getString("ldap.base.dn"), username);

            // login
            authenticationContext = createAuthenticationContext(principal, password);

            // get user from local db
            User user = userDAO.getUser(username, NAME);

            // create new user if necessary
            if (user == null) {
                user = new User();
                user.setUsername(username);
                user.setLastlogin(new Date());
                user.setRealm(NAME);
                user.setOwnerid(0);
                user.setLeid(0);
                loadUserProperties(authenticationContext, user);
                userDAO.create(user);
            } else {
                // otherwise just set last login
                user.setLastlogin(new Date());
                userDAO.update(user);
            }
            return user;
        } finally {
            if (authenticationContext != null) {
                authenticationContext.close();
            }
        }
    }

    /**
     * Loads all necessary user data from ldap into target user
     *
     * @param context contains normally the context with which the user has authenticated itself
     * @param user    contains the user which attributes should be set
     * @throws NamingException thrown if attributes could not be loaded
     */
    private void loadUserProperties(DirContext context, User user) throws NamingException {

        String mapName = config.getString("ldap.mapping.name");
        String mapEMail = config.getString("ldap.mapping.email");
        NamingEnumeration<SearchResult> searchResults = search(context, user.getUsername());

        while (searchResults.hasMore()) {
            SearchResult searchResult = searchResults.next();

            Attributes attributes = searchResult.getAttributes();

            try {

                // go through all ldap attributes
                for (NamingEnumeration<String> attributeIDs = attributes.getIDs(); attributeIDs.hasMore(); ) {
                    String attributeID = attributeIDs.next();
                    Attribute attribute = attributes.get(attributeID);

                    // go through all values of given attribute
                    for (NamingEnumeration values = attribute.getAll(); values.hasMore(); ) {
                        String attributeValue = values.next().toString();

                        // set real name if mapping for it is found
                        if (attributeID.equals(mapName) && (user.getRealname() == null))
                            user.setRealname(formatName(attributeValue));

                        // set email if mapping for it is found
                        if (attributeID.equals(mapEMail) && (user.getEmail() == null))
                            user.setEmail(attributeValue);
                    }
                }
            } catch (NamingException e) {
                LOG.error("could not access attributes or values.", e);
            }
        }
    }

    /**
     * Searches inside given context for {@link SearchResult}s.
     *
     * @param username Contains the username that the user uses to authenticate itself, not the complete ldap username
     * @return all found search results that are offered by ldap
     * @throws InvalidSearchFilterException
     * @throws InvalidSearchControlsException
     * @throws NamingException
     * @see DirContext#search(String, String, SearchControls)
     */
    private NamingEnumeration<SearchResult> search(DirContext context, String username) throws NamingException {

        // Get user properties from LDAP server
        try {
            String baseDN = String.format(config.getString("ldap.base.dn"), username);
            String uidFilter = config.getString("ldap.uid.filter");

            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            return context.search(baseDN, String.format(uidFilter, username), controls);
        } catch (InvalidSearchFilterException e) {
            LOG.error("invalid search filter given", e);
            throw e;
        } catch (InvalidSearchControlsException e) {
            LOG.error("invalid search controls given", e);
            throw e;
        } catch (NamingException e) {
            LOG.error("other naming exception occurred", e);
            throw e;
        }
    }

    /**
     * Creates a {@link DirContext} that has authenticated the user with target principal and its credentials.
     *
     * @return the directory context that can be used for further use, for example the search for user attributes.
     * Be sure to close the context after use.
     * @throws NamingException thrown if the authentication failed or any other naming errors
     */
    private DirContext createAuthenticationContext(Object principal, Object credentials) throws NamingException {

        String providerURL = config.getString("ldap.provider.url");

        Properties ldapEnv = new Properties();
        ldapEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        ldapEnv.put("com.sun.jndi.ldap.read.timeout", config.getString("ldap.read.timeout"));
        ldapEnv.put(Context.PROVIDER_URL, providerURL);
        if (StringUtils.startsWithIgnoreCase(providerURL, "ldaps"))
            ldapEnv.put(Context.SECURITY_PROTOCOL, "ssl");
        ldapEnv.put(Context.SECURITY_AUTHENTICATION, "simple");
        ldapEnv.put(Context.SECURITY_PRINCIPAL, principal);
        ldapEnv.put(Context.SECURITY_CREDENTIALS, credentials);
        try {
            return new InitialDirContext(ldapEnv);
        } catch (NamingException e) {
            LOG.error("could not create authentication context for principal " + principal, e);
            throw e;
        }
    }

    /**
     * Formats a user name into "lastname, firstname" syntax.
     */
    private String formatName(String name) {
        name = name.replaceAll("\\s+", " ").trim();
        if (name.contains(","))
            return name;
        int pos = name.lastIndexOf(' ');
        if (pos == -1)
            return name;
        return name.substring(pos + 1, name.length()) + ", " + name.substring(0, pos);
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
    public boolean exists(String username) {
        DirContext context = null;
        try {
            context = createAuthenticationContext(config.getString("ldap.security.principal"),
                    config.getString("ldap.security.credentials"));
            NamingEnumeration<SearchResult> search = search(context, username);
            return search.hasMore();
        } catch (NameNotFoundException e) {
            LOG.error("could not find user with username " + username, e);
        } catch (NamingException e) {
            LOG.error("other naming exception occurred", e);
        } finally {
            if (context != null) {
                try {
                    context.close();
                } catch (NamingException e) {
                    LOG.error("could not close context", e);
                }
            }
        }
        return false;
    }
}
