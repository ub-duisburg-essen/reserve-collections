
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
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.Permission;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.DeleteException;
import unidue.rc.model.ActionDefinition;
import unidue.rc.model.DefaultRole;
import unidue.rc.model.ReserveCollection;
import unidue.rc.model.Role;

import javax.annotation.Nullable;


/**
 * A <code>CollectionSecurityService</code> is able to check security issues against actions regarding {@link
 * unidue.rc.model.ReserveCollection}s
 *
 * @author Nils Verheyen
 * @since 14.08.14 10:28
 */
public interface CollectionSecurityService {

    /**
     * Returns the current authenticated {@link User}, if one is available.
     *
     * @return the authenticated {@link User} or <code>null</code> if none is available
     */
    User getCurrentUser();

    /**
     * Runs login for user with given username and password
     *
     * @param username username of the user
     * @param password password of the user
     * @throws AuthenticationException if auth was unsuccessful
     */
    void login(String username, String password) throws AuthenticationException;

    /**
     * Logs current user of the active session.
     */
    void logout();

    /**
     * Permission check without exception but boolean result.
     *
     * @param actionDefinition definition of the action
     * @return <code>true</code> if the action is allowed
     * @see #checkPermission(ActionDefinition)
     */
    boolean isPermitted(ActionDefinition actionDefinition);

    /**
     * Permission check without exception but boolean result.
     *
     * @param actionDefinition definition of the action
     * @param objectID         instance id
     * @return <code>true</code> if the action is allowed
     * @see #checkPermission(ActionDefinition, Integer)
     */
    boolean isPermitted(ActionDefinition actionDefinition, Integer objectID);

    /**
     * Permission check without exception but boolean result.
     *
     * @param actionDefinition definition of the action
     * @param objectID         instance id
     * @return <code>true</code> if the action is allowed
     * @see #checkPermission(ActionDefinition, String)
     */
    boolean isPermitted(ActionDefinition actionDefinition, String objectID);

    /**
     * Permission check without exception but boolean result for user with target id.
     *
     * @param actionDefinition definition of the action
     * @param userID           user id to use
     * @param objectID         instance id
     * @return <code>true</code> if the action is allowed
     * @see #checkPermission(ActionDefinition, Integer)
     */
    boolean isPermitted(ActionDefinition actionDefinition, Integer userID, Integer objectID);

    /**
     * Permission check without exception but boolean result for user with target id.
     *
     * @param actionDefinition definition of the action
     * @param userID           user id to use
     * @param objectID         instance id
     * @return <code>true</code> if the action is allowed
     * @see #checkPermission(ActionDefinition, String)
     */
    boolean isPermitted(ActionDefinition actionDefinition, Integer userID, String objectID);

    /**
     * Checks if the current {@link org.apache.shiro.subject.Subject} is allowed to perform the action with
     * target name, independant of any object context.
     *
     * @param actionDefinition action to check
     * @throws AuthorizationException when executing the {@link unidue.rc.model.Action} with target name is not allowed
     */
    void checkPermission(ActionDefinition actionDefinition) throws AuthorizationException;

    /**
     * Same as {@link CollectionSecurityService#checkPermission(ActionDefinition, String)} but with an integer as identifier
     *
     * @param actionDefinition action to check
     * @param objectID         object to check
     * @throws AuthorizationException when executing the {@link unidue.rc.model.Action} with target name is not allowed
     */
    void checkPermission(ActionDefinition actionDefinition, Integer objectID) throws AuthorizationException;

    /**
     * Checks if the current {@link org.apache.shiro.subject.Subject} is allowed to perform the action with
     * target name on the object with given id.
     *
     * @param actionDefinition action to check
     * @param objectID         may be <code>null</code>. If it is null the permission check is run without a context to an object.
     * @throws AuthorizationException when executing the {@link unidue.rc.model.Action} with target name is not allowed
     */
    void checkPermission(ActionDefinition actionDefinition, String objectID) throws AuthorizationException;

    /**
     * Creates a new {@link Permission} that is bound to an object with target id, user and role. With the role
     * all actions that target user is able to run are bound.
     *
     * @param userID        user id for the permission
     * @param instanceID    instance id for the permission
     * @param instanceClass instance class for the permission
     * @param role          role of the permission
     * @throws CommitException if any object could not be saved
     */
    void createInstancePermissions(Integer userID, Integer instanceID, Class instanceClass, Role role) throws CommitException;

    /**
     * Creates permissions for user with target id on object with target id and given action definition
     *
     * @param userID           user id for the permission
     * @param instanceID       instance id for the permission
     * @param instanceClass    instance class for the permission
     * @param actionDefinition action of the permission
     * @throws CommitException if any object could not be saved
     */
    void createInstancePermissions(Integer userID, Integer instanceID, Class instanceClass, ActionDefinition actionDefinition) throws CommitException;

    /**
     * Removes all {@link Permission}s of target user on the object with target id.
     *
     * @param objectID instance id of the permission
     * @param user     user of the permission
     * @throws DeleteException thrown if one of the permission objects could not deleted
     */
    void removeInstancePermissions(Integer objectID, User user) throws DeleteException;

    /**
     * Should be called after an {@link ReserveCollection} was deactivated.
     *
     * @param collection collection that was deactivated
     */
    void afterCollectionDeactivated(ReserveCollection collection);

    /**
     * Should be called after an {@link ReserveCollection} was archived.
     *
     * @param collection collection that was archived
     */
    void afterCollectionArchived(ReserveCollection collection);

    /**
     * Should be called before a {@link ReserveCollection} should be deleted.
     *
     * @param collection collection that is going to be deleted
     */
    void beforeCollectionDelete(ReserveCollection collection);

    /**
     * Returns the {@link Role} associated to target default role.
     *
     * @param role definition of the role
     * @return role or <code>null</code> if none was found
     */
    Role getRole(DefaultRole role);

    /**
     * Returns <code>true</code> if a user with target username exists inside this application used
     * {@link org.apache.shiro.realm.Realm}s.
     *
     * @param username username to check
     * @return <code>true</code> if a user with given username exists
     * @see unidue.rc.ui.services.AppModule#addRealms
     */
    boolean exists(String username);

    /**
     * Returns <code>true</code> if target username is a valid username to the system
     *
     * @param username username may be null
     * @return <code>true</code> if the username is valid
     */
    boolean isUsernameValid(@Nullable String username);
}
