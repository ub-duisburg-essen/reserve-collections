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
package unidue.rc.migration.legacymodel;


import org.simpleframework.xml.*;

@Root(name = "name", strict = false)
@NamespaceList(value = { @Namespace(prefix = "mods", reference = "http://www.loc.gov/mods/v3") })
public class NameMetaDataLocal {

    @Element(name = "displayForm")
    private String teacher;

    @Attribute(name = "valueURI")
    private String valueURI;

    @Path("role")
    @Element(name = "roleTerm")
    private String roleTerm;

    public String getLegalEntityID() {
        if (valueURI != null) {
            String[] values = valueURI.split("#");
            if (values.length > 0) {
                return values[values.length - 1];
            }
        }
        return null;
    }

    public boolean isTeacher() {
        return "tch".equalsIgnoreCase(roleTerm);
    }

    /**
     * @return the teacher
     */
    public String getTeacher() {
        return teacher;
    }

}
