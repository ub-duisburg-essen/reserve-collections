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
package unidue.rc.auth;


import miless.model.User;
import org.apache.shiro.realm.ldap.AbstractLdapRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;


/**
 * With an instance of <code>LDAPRealm</code> one is able to operate on ldap services and load/set/update {@link User}
 * details.
 *
 * @author Nils Verheyen
 * @since 23.07.13 11:20
 */
public abstract class LDAPRealm extends AbstractLdapRealm {

    public static final String NAME = "ude";

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
