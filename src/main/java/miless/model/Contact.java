package miless.model;

/*
 * #%L
 * Semesterapparate
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 Universitaet Duisburg Essen
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
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * @author Nils Verheyen
 */
@Root(name = "contact", strict = false)
public class Contact {

    @Attribute(name = "type")
    private String type;

    @Attribute(name = "publish")
    private Boolean publish;

    @Element(name = "institution", required = false)
    private String institution;

    @Element(name = "comment", required = false)
    private String comment;

    @ElementList(name = "adresses", entry = "address", required = false)
    private List<String> adresses;

    @ElementList(name = "phoneNumbers", entry = "phone", required = false)
    private List<String> phoneNumbers;

    @ElementList(name = "faxNumbers", entry = "fax", required = false)
    private List<String> faxNumbers;

    @ElementList(name = "emails", entry = "email", required = false)
    private List<String> emails;

    @ElementList(name = "webSites", entry = "url", required = false)
    private List<String> webSites;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
