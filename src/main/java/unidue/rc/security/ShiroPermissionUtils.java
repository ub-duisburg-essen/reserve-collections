package unidue.rc.security;

/*
 * #%L
 * Semesterapparate
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 - 2015 Universitaet Duisburg Essen
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
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.subject.PrincipalCollection;
import unidue.rc.model.ActionDefinition;

import java.util.Collection;

/**
 * Created by nils on 29.05.15.
 */
public interface ShiroPermissionUtils {

    /**
     * Builds all shiro {@link Permission} objects that belong to target {@link User}.
     *
     * @param user user to build permissions for
     * @return a collection with all permissions, or an empty collection if there are no permissions.
     */
    Collection<Permission> buildPermissions(User user);

    /**
     * Builds a {@link Permission} object that represents the general privilege to do an action defined by target
     * definition.
     *
     * @param action action to use
     * @return the permission that can be used by shiro
     */
    Permission buildPermission(ActionDefinition action);

    /**
     * Builds a {@link Permission} object that represents the privilege to do an action defined by target
     * definition on a given object.
     *
     * @param action   action to use
     * @param objectID object id to use
     * @return the permission that can be used by shiro
     */
    Permission buildPermission(ActionDefinition action, String objectID);

    /**
     * Builds the {@link AuthorizationInfo} for the user with target principals (username) and its realm.
     *
     * @param principals principals to use for auth
     * @param realm      realm to use for auth
     * @return the complete authorization info
     */
    AuthorizationInfo buildAuthorizationInfo(PrincipalCollection principals, String realm);
}
