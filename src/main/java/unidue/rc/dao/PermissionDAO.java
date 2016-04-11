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
import unidue.rc.model.ActionDefinition;
import unidue.rc.model.Permission;
import unidue.rc.model.PermissionDefinition;
import unidue.rc.model.Role;

import java.util.List;

/**
 * Created by nils on 11.05.15.
 */
public interface PermissionDAO extends BaseDAO {

    String SERVICE_NAME = "PermissionDAO";

    /**
     * Returns all {@link Permission} objects that are bound to target user.
     *
     * @param user see description
     * @return the list with all permissions, or an empty list
     */
    List<Permission> getPermissions(User user);

    /**
     * Returns all {@link Permission} objects that users have on the object with target instance id
     *
     * @param instanceID id of the instance
     * @return the list with all permissions, or an empty list
     */
    List<Permission> getPermissions(Integer instanceID);

    /**
     * Deletes all {@link Permission} objects that belong to the object with target id.
     *
     * @param instanceID id of the instance object
     */
    void deletePermissionsByInstance(Integer instanceID);
}
