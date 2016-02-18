package unidue.rc.migration.legacymodel;

/*
 * #%L
 * Semesterapparate
 * $Id$
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

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import unidue.rc.migration.MigrationException;
import unidue.rc.model.Entry;
import unidue.rc.model.ReserveCollection;

@Root(name = "file", strict = false)
public class FileLocal implements MigrationVisitable, ResourceValue {

    @Element(name = "path", required = false)
    private String path;

    @Element(name = "label", required = false)
    private String label;

    @Element(name = "itemStatus", required = false)
    private String reviewStatus;

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    @Override
    public String getUrl() {
        return null;
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    public String getReviewStatus() {
        return reviewStatus;
    }

    @Override
    public String toString() {
        return "FileLoca [path= " + path + " label= " + label + "]";
    }

    @Override
    public void migrateEntryValue(MigrationVisitor visitor, ReserveCollection collection, Entry entry, EntryLocal entryLocal, String derivateID) throws MigrationException {
        visitor.migrate(this, collection, entry, entryLocal, derivateID);
    }
}
