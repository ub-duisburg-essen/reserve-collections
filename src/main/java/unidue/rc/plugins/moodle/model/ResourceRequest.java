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
package unidue.rc.plugins.moodle.model;


import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * Created by nils on 17.06.15.
 */
@Root(name = "get", strict = false)
public class ResourceRequest implements Request {

    @Attribute
    private int collectionID;

    @Attribute
    private int resourceID;

    @Attribute
    private String username;

    @Attribute
    private String secret;

    @Attribute(required = false)
    private String firstname;

    @Attribute(required = false)
    private String lastname;

    @Attribute(required = false)
    private String email;

    @Attribute
    private String authtype;

    public int getCollectionID() {
        return collectionID;
    }

    public int getResourceID() {
        return resourceID;
    }

    public String getUsername() {
        return username;
    }

    public String getSecret() {
        return secret;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getEmail() {
        return email;
    }

    public String getAuthtype() {
        return authtype;
    }
}
