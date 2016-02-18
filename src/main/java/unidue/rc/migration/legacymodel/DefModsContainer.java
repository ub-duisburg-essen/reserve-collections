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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Root(name = "def.modsContainer", strict = false)
@NamespaceList(value = { @Namespace(prefix = "mods", reference = "http://www.loc.gov/mods/v3") })
public class DefModsContainer {

    @Path("modsContainer/mods/titleInfo")
    @Element(required = false)
    private String nonSort;

    @Path("modsContainer/mods/titleInfo")
    @Element
    private String title;

    @Path("modsContainer/mods/titleInfo")
    @Element(required = false)
    private String subTitle;

    @Path("modsContainer/mods")
    @ElementList(inline = true, required = true)
    private ArrayList<NameMetaDataLocal> name;

    @Path("modsContainer/mods")
    @ElementList(inline = true, required = true)
    private ArrayList<ClassificationMetaDataLocal> classifications;

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public String getNonSort() {
        return nonSort;
    }

    /**
     * (non-Javadoc)
     * 
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "DefModsContainer [title=" + title + "]";
    }

    public List<Integer> getTeacherIDs() {
        return this.name.stream()
                .filter(name -> name.isTeacher())
                .map(name -> name.getLegalEntityID())
                .map(id -> Integer.valueOf(id))
                .collect(Collectors.toList());
    }

    public String getOriginID() {
        if (classifications != null && classifications.size() > 0) {
            for (ClassificationMetaDataLocal classification : classifications) {
                if (classification.isOriginClassification())
                    return classification.getValueURIID();
            }
        }
        return null;
    }
}
