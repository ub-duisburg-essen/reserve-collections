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

import org.simpleframework.xml.*;

@Root(name = "mycoreobject", strict = false)
@NamespaceList(value = { @Namespace(prefix = "mods", reference = "http://www.loc.gov/mods/v3") })
public class ReserveCollectionMetaDataLocal {

    @Path("metadata")
    @Element(name = "def.modsContainer")
    private DefModsContainer container;

    /**
     * @return the container
     */
    public DefModsContainer getContainer() {
        return container;
    }

    /**
     * @param container
     *            the container to set
     */
    public void setContainer(DefModsContainer container) {
        this.container = container;
    }

}
