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

@Root(name = "slot", strict = false)
public class SlotLocal {

    @Element(name = "aleph", required = false)
    private AlephLocal aleph;

    @Element(name = "validTo", required = false)
    private String validTo;

    @Attribute(name = "ID")
    private String id;

    @Attribute(name = "eOnly",required = false)
    private boolean eOnly;

    @Attribute(name = "status", required = false)
    private String status;
    
    @Attribute(name = "sendWarnings", required = false)
    private boolean sendWarnings;

    @Element(name = "comment", required = false)
    private String comment;

    @Path("document")
    @Attribute(name = "ID")
    private String documentID;

    @ElementList(inline=true, required=false)
    private ArrayList<LecturerLocal> lecturer; 
    
    /**
     * @return the aleph
     */
    public AlephLocal getAleph() {
        return aleph;
    }

    /**
     * @return the validTo
     */
    public String getValidTo() {
        return validTo;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @return the eOnly
     */
    public boolean iseOnly() {
        return eOnly;
    }

    /**
     * @return the sendWarnings
     */
    public boolean isSendWarnings() {
        return sendWarnings;
    }

    /**
     * @return the comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @return the lecturer
     */
    public ArrayList<LecturerLocal> getLecturer() {
        return lecturer;
    }

    public String getDocumentID() {
        return documentID;
    }

    public int getNumber() {
        return id != null && id.matches("[0-9]+")
                ? Integer.valueOf(id.substring(0))
                : -1;
    }
}
