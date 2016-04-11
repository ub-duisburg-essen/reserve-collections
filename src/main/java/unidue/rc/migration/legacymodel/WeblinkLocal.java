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


import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import unidue.rc.model.Entry;
import unidue.rc.model.ReserveCollection;

@Root(name = "webLink", strict = false)
public class WeblinkLocal implements MigrationVisitable {

    @Element(name = "url", required = false)
    private String url;

    @Element(name = "label", required = false)
    private String label;

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return "WeblinkLocal [url=" + url + " label= " + label + "]";
    }

    @Override
    public void migrateEntryValue(MigrationVisitor visitor, ReserveCollection collection, Entry entry, EntryLocal entryLocal, String derivateID) {
        visitor.migrate(this, collection, entry, entryLocal, derivateID);
    }
}
