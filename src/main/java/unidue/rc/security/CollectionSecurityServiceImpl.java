/*
 * Copyright 2014 Universitaet Duisburg Essen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package unidue.rc.security;

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
import org.apache.cayenne.BaseContext;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.query.NamedQuery;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.UnavailableSecurityManagerException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unidue.rc.auth.CollectionAuthenticationToken;
import unidue.rc.auth.LDAPRealm;
import unidue.rc.auth.LocalRealm;
import unidue.rc.dao.*;
import unidue.rc.model.*;
import unidue.rc.system.SystemConfigurationService;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * @author Nils Verheyen
 * @since 06.08.14 15:00
 */
public class CollectionSecurityServiceImpl implements CollectionSecurityService {

    private static final Logger LOG = LoggerFactory.getLogger(CollectionSecurityServiceImpl.class);

    private static final String USE_SECURITY_CHECKS_SETTINGS_KEY = "use.security.checks";

    private static final String USERNAME_PATTERN = "(\\S)*";

    @Inject
    private SystemConfigurationService config;

    @Inject
    private PermissionDAO permissionDAO;

    @Inject
    private ParticipationDAO participationDAO;

    @Inject
    private ActionDAO actionDAO;

    @Inject
    private RoleDAO roleDAO;

    @Inject
    private UserDAO userDAO;

    @Inject
    private ShiroPermissionUtils shiroPermissionUtils;

    @Inject
    private LocalRealm localRealm;

    @Inject
    private LDAPRealm ldapRealm;

    @Override
    public User getCurrentUser() {
        Session shiroSession = SecurityUtils.getSubject().getSession(false);
        return shiroSession != null
                ? (User) shiroSession.getAttribute(User.USER_SESSION_ATTRIBUTE)
                : null;
    }

    @Override
    public void login(String username, String password) throws AuthenticationException {
        Subject subject = SecurityUtils.getSubject();
        User user = userDAO.getUser(username);

        CollectionAuthenticationToken authToken = new CollectionAuthenticationToken(username, password);
        /*
        1. user present && local user   -> UsernamePasswordToken
        2. user present && ude user     -> LDAPToken
        3. user not present             -> LDAPToken
         */
        String realm = user != null && StringUtils.equals(user.getRealm(), LocalRealm.NAME)
                ? LocalRealm.NAME
                : LDAPRealm.NAME;
        authToken.setRealm(realm);
        subject.login(authToken);

        user = userDAO.getUser(username);
        Session shiroSession = SecurityUtils.getSubject().getSession(false);
        shiroSession.setAttribute(User.USER_SESSION_ATTRIBUTE, user);
        LOG.info("user '" + user  + "' authenticated");
    }

    @Override
    public void logout() {
        SecurityUtils.getSubject().logout();
    }

    @Override
    public boolean isPermitted(ActionDefinition actionDefinition) {
        try {
            checkPermission(actionDefinition);
            return true;
        } catch (AuthorizationException e) {
            return false;
        }
    }

    @Override
    public boolean isPermitted(ActionDefinition actionDefinition, Integer objectID) {
        try {
            checkPermission(actionDefinition, objectID);
            return true;
        } catch (AuthorizationException e) {
            return false;
        }
    }

    @Override
    public boolean isPermitted(ActionDefinition actionDefinition, String objectID) {
        try {
            checkPermission(actionDefinition, objectID);
            return true;
        } catch (AuthorizationException e) {
            return false;
        }
    }

    @Override
    public boolean isPermitted(ActionDefinition actionDefinition, Integer userID, Integer objectID) {

        String[] keys = {"userID", "resource", "name"};
        String[] values = {userID.toString(), actionDefinition.getResource(), actionDefinition.getName()};
        NamedQuery query = new NamedQuery(ReserveCollectionsDatamap.SELECT_PERMISSION_BY_USER_ACTION_QUERYNAME, keys, values);

        ObjectContext context = BaseContext.getThreadObjectContext();

        List<Permission> permissions = context.performQuery(query);
        return permissions != null && permissions.size() > 0;
    }

    @Override
    public boolean isPermitted(ActionDefinition actionDefinition, Integer userID, String objectID) {
        return NumberUtils.isNumber(objectID)
                ? isPermitted(actionDefinition, userID, Integer.valueOf(objectID))
                : false;
    }

    public void checkPermission(ActionDefinition actionDefinition) {
        if (!config.getBoolean(USE_SECURITY_CHECKS_SETTINGS_KEY))
            return;

        SecurityUtils.getSubject().checkPermission(shiroPermissionUtils.buildPermission(actionDefinition));
    }

    public void checkPermission(ActionDefinition actionDefinition, Integer objectID) {

        checkPermission(actionDefinition, objectID.toString());
    }

    public void checkPermission(ActionDefinition actionDefinition, String objectID) {
        if (!config.getBoolean(USE_SECURITY_CHECKS_SETTINGS_KEY))
            return;

        org.apache.shiro.authz.Permission permission = objectID == null
                ? shiroPermissionUtils.buildPermission(actionDefinition)
                : shiroPermissionUtils.buildPermission(actionDefinition, objectID);

        SecurityUtils.getSubject().checkPermission(permission);
    }

    @Override
    public void createInstancePermissions(Integer userID, Integer instanceID, Class instanceClass, Role role) throws CommitException {

        // add permissions
        Set<Permission> permissionsToCreate = new HashSet<>();

        role.getPermissionDefinitions()
                .stream()
                .filter(definition -> definition.isIsInstanceBound())
                .map(definition -> {
                    Permission p = new Permission();
                    p.setAction(definition.getAction());
                    p.setUserID(userID);
                    p.setInstanceID(instanceID);
                    p.setInstanceClass(instanceClass.getSimpleName());
                    return p;
                })
                .distinct()
                .forEach(permission -> permissionsToCreate.add(permission));

        // create permissions
        for (Permission p : permissionsToCreate) {
            permissionDAO.create(p);
        }

        // clear authorization cache
        tryClearAuthCache(userID);
    }

    @Override
    public void createInstancePermissions(Integer userID, Integer instanceID, Class instanceClass, ActionDefinition actionDefinition) throws CommitException {

        Permission p = new Permission();
        p.setUserID(userID);
        p.setInstanceID(instanceID);
        p.setInstanceClass(instanceClass.getSimpleName());
        p.setAction(actionDAO.getAction(actionDefinition));
        permissionDAO.create(p);

        tryClearAuthCache(userID);
    }

    @Override
    public void removeInstancePermissions(Integer objectID, User user) throws DeleteException {
        List<Permission> permissions = permissionDAO.getPermissions(user);
        for (Permission p : permissions) {
            if (objectID.equals(p.getInstanceID()))
                permissionDAO.delete(p);
        }

        tryClearAuthCache(user);
    }

    @Override
    public void afterCollectionDeactivated(ReserveCollection collection) {

        // remove all permissions as every participation has ended
        removePermissions(collection.getId());

        // add all missing permissions
        List<Participation> participations = participationDAO.getActiveParticipations(collection);
        for (Participation participation : participations) {
            try {
                createInstancePermissions(participation.getUserId(), collection.getId(), ReserveCollection.class,
                        participation.getRole());
                tryClearAuthCache(participation.getUserId());
            } catch (CommitException e) {
                LOG.error("could not create permission on " + collection
                        + " for user " + participation.getUserId()
                        + " with role " + participation.getRole());
            }
        }
    }

    @Override
    public void afterCollectionArchived(final ReserveCollection collection) {

        // get all user ids that belong to permission objects
        List<Integer> userIDs = permissionDAO.getPermissions(collection.getId())
                .stream()
                .map(p -> p.getUserID())
                .collect(Collectors.toList());

        // remove all permissions as every participation has ended
        removePermissions(collection.getId());

        // clear auth cache for each user that had permissions on given collection
        userIDs.forEach(this::tryClearAuthCache);
    }

    /**
     * Removes all permissions on the instance with target id and clears the auth cache for the user
     * that belongs to one permission.
     */
    private void removePermissions(Integer instanceID) {
        // remove all permissions as every participation has ended
        List<Permission> permissions = permissionDAO.getPermissions(instanceID);
        for (Permission permission : permissions) {
            try {
                permissionDAO.delete(permission);
                tryClearAuthCache(permission.getUserID());
            } catch (DeleteException e) {
                LOG.error("could not delete permission " + permission, e);
            }
        }
    }

    @Override
    public void beforeCollectionDelete(ReserveCollection collection) {
        permissionDAO.deletePermissionsByInstance(collection.getId());
    }

    @Override
    public Role getRole(DefaultRole role) {
        return roleDAO.getRole(role);
    }

    @Override
    public boolean exists(String username) {
        username = userDAO.normalizeUsername(username);
        return localRealm.exists(username) || ldapRealm.exists(username);
    }

    @Override
    public boolean isUsernameValid(@Nullable String username) {
        return StringUtils.isNotBlank(username) && username.matches(USERNAME_PATTERN);
    }

    private void tryClearAuthCache(int userID) {

        if (isSecurityManagerAvailable()) {

            User user = userDAO.get(User.class, userID);
            if (user != null)
                doClearAuthCache(user);
            else
                LOG.warn("could not clear auth cache for unknown user " + userID);
        }
    }

    private void tryClearAuthCache(User user) {

        if (isSecurityManagerAvailable())
            doClearAuthCache(user);
    }

    private boolean isSecurityManagerAvailable() {
        try {
            SecurityUtils.getSecurityManager();
            return true;
        } catch (UnavailableSecurityManagerException e) {
            LOG.warn("security manager not available, clear cache is not available");
            return false;
        }
    }

    private void doClearAuthCache(User user) {
        final String userRealm = user.getRealm();
        switch (userRealm) {
            case LDAPRealm.NAME:
                ldapRealm.clearAuthCache(user);
                break;
            case LocalRealm.NAME:
                localRealm.clearAuthCache(user);
                break;
        }

    }
}
