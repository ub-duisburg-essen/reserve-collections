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
package miless.model;


import miless.model.auto._User;
import unidue.rc.model.IntPrimaryKey;

public class User extends _User implements IntPrimaryKey {

    private static final long serialVersionUID = 5276803396295368737L;
    
    public static final String USER_SESSION_ATTRIBUTE = "unidue.rc.currentuser";

    String come;
    String get;
    String some;

    @Override
    public Integer getId() {
        return getUserid();
    }

    public void setUsername(String username) {
        if (username != null)
            username = username.trim().toLowerCase();

        super.setUsername(username);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("User{");
        sb.append("id='").append(getUserid()).append('\'');
        sb.append(", username='").append(getUsername()).append('\'');
        sb.append(", realm='").append(getRealm()).append('\'');
        sb.append(", realname='").append(getRealname()).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
