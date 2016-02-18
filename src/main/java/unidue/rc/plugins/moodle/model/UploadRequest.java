package unidue.rc.plugins.moodle.model;

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

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * Created by nils on 22.09.15.
 */
@Root(name = "upload", strict = false)
public class UploadRequest implements Request {

    @Attribute
    private int collectionID;

    @Attribute
    private String secret;

    @Attribute
    private String filename;

    @Attribute
    private String username;

    @Attribute
    private String authtype;

    public int getCollectionID() {
        return collectionID;
    }

    public String getSecret() {
        return secret;
    }

    public String getFilename() {
        return filename;
    }

    public String getUsername() {
        return username;
    }

    public String getAuthtype() {
        return authtype;
    }
}
