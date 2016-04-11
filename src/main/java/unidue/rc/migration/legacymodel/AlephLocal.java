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


import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "aleph", strict = false)
public class AlephLocal {

    @Attribute(name = "userID", required = false)
    private String alephuserid;

    @Attribute(name = "systemID", required = false)
    private String alephsystemid;

    /**
     * @return the alephuserid
     */
    public String getAlephuserid() {
        return alephuserid;
    }

    /**
     * @return the alephsystemid
     */
    public String getAlephsystemid() {
        return alephsystemid;
    }

}
