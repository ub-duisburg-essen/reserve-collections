/**
 * Copyright (C) 2014 - 2016 Universitaet Duisburg-Essen (semapp|uni-due.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package unidue.rc.dao;


import miless.model.User;
import org.apache.cayenne.validation.ValidationException;
import unidue.rc.model.*;

import java.util.List;

/**
 * A <code>RoleDAO</code> should be used as default access object to load, update and delete {@link Role} objects from
 * backend.
 *
 * @author Nils Verheyen
 * @see RoleDAOImpl
 */
public interface RoleDAO extends BaseDAO {

    String SERVICE_NAME = "RoleDAO";

    /**
     * Returns all {@link Role} object from backend.
     *
     * @return all roles or an empty list if none exists.
     */
    List<Role> getRoles();

    /**
     * Returns all {@link Role}s target user is member of.
     *
     * @param user user to use
     * @return all roles or an empty list if none exists.
     */
    List<Role> getRoles(User user);


    /**
     * Returns all {@link unidue.rc.model.Role}s which target user is member of as default.
     *
     * @param user user to use
     * @return all memberships or an empty list
     */
    List<Membership> getMembership(User user);


    /**
     * Returns all {@link Action} object that have a relation to target {@link Role}.
     *
     * @param role role, which associated actions should be returned
     * @return all actions associated with the role or an empty list
     */
    List<Action> getActions(Role role);

    /**
     * Returns the {@link Role} with target id, <code>null</code> if does not exist
     *
     * @param id the id of the role
     * @return the role by given id or <code>null</code>
     */
    Role getRoleById(Integer id);

    /**
     * Returns the {@link Role} that belongs to target {@link DefaultRole}.
     *
     * @param defaultRole role that defines the result
     * @return the role associated to target default role or <code>null</code> if it does not exist
     */
    Role getRole(DefaultRole defaultRole);
}
