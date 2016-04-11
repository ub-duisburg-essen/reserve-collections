package unidue.rc.security;


import miless.model.User;
import org.apache.cayenne.di.Inject;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.authz.permission.AllPermission;
import org.apache.shiro.authz.permission.WildcardPermission;
import org.apache.shiro.subject.PrincipalCollection;
import unidue.rc.dao.PermissionDAO;
import unidue.rc.dao.RoleDAO;
import unidue.rc.dao.UserDAO;
import unidue.rc.model.Action;
import unidue.rc.model.ActionDefinition;
import unidue.rc.model.PermissionDefinition;
import unidue.rc.model.Role;
import unidue.rc.system.SystemConfigurationService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by nils on 29.05.15.
 */
public class ShiroPermissionUtilsImpl implements ShiroPermissionUtils {

    private static final char SHIRO_STRING_PERMISSION_SEPARATOR = ':';

    @Inject
    private UserDAO userDAO;

    @Inject
    private RoleDAO roleDAO;

    @Inject
    private PermissionDAO permissionDAO;

    @Inject
    private SystemConfigurationService config;

    @Override
    public Collection<Permission> buildPermissions(User user) {

        List<Permission> result = new ArrayList<>();

        List<Role> roles = roleDAO.getRoles(user);
        for (Role role : roles) {
            List<PermissionDefinition> permissionDefinitions = role.getPermissionDefinitions();
            permissionDefinitions
                    .stream()
                    .filter(definition -> !definition.isIsInstanceBound())
                    .forEach(definition -> result.add(buildPermission(definition.getAction())));
        }
        List<unidue.rc.model.Permission> permissions = permissionDAO.getPermissions(user);
        permissions.forEach(permission -> result.add(buildPermission(permission.getAction(), Integer.toString(permission.getInstanceID()))));
        return result;
    }

    @Override
    public Permission buildPermission(ActionDefinition action) {
        return buildPermission(action.getResource(), action.getName());
    }

    @Override
    public Permission buildPermission(ActionDefinition action, String objectID) {
        return buildPermission(action.getResource(), action.getName(), objectID);
    }

    @Override
    public AuthorizationInfo buildAuthorizationInfo(PrincipalCollection principals, String realm) {

        if (!config.getBoolean("use.security.checks")) {

            SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
            authorizationInfo.addObjectPermission(new AllPermission());
            return authorizationInfo;
        }

        if (principals == null)
            throw new AuthorizationException("PrincipalCollection was null, which should not happen");

        if (principals.isEmpty())
            return null;

        Collection subjectPrincipals = principals.fromRealm(realm);
        if (subjectPrincipals.size() <= 0)
            return null;

        String username = (String) subjectPrincipals.iterator().next();
        if (username == null)
            return null;
        User user = userDAO.getUser(username);
        if (user == null)
            return null;

        List<Role> roles = roleDAO.getRoles(user);
        Set<String> roleNames = roles.stream().map(Role::getName).collect(Collectors.toSet());
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo(roleNames);

        authorizationInfo.addObjectPermissions(buildPermissions(user));

        return authorizationInfo;
    }

    private Permission buildPermission(Action action) {
        return buildPermission(action.getResource(), action.getName());
    }

    private Permission buildPermission(Action action, String objectID) {
        return buildPermission(action.getResource(), action.getName(), objectID);
    }

    private Permission buildPermission(String resource, String name) {
        StringBuilder builder = new StringBuilder(resource);
        builder.append(SHIRO_STRING_PERMISSION_SEPARATOR);
        builder.append(name);

        return new WildcardPermission(builder.toString());
    }

    private Permission buildPermission(String resource, String name, String objectID) {

        StringBuilder builder = new StringBuilder(resource);
        builder.append(SHIRO_STRING_PERMISSION_SEPARATOR);
        builder.append(name);
        builder.append(SHIRO_STRING_PERMISSION_SEPARATOR);
        builder.append(objectID);

        return new WildcardPermission(builder.toString());
    }
}
