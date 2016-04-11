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
package unidue.rc.model.legacy;


import org.simpleframework.xml.*;

import java.util.List;

/**
 * Created by nils on 12.08.15.
 */
@Root(name = "slot", strict = false)
public class Slot {

    @Attribute(name = "ID")
    private String id;

    @Attribute(name = "status", required = false)
    private String status;

    @Element(name = "validTo", required = false)
    private String validTo;

    @ElementList(inline = true, required = false)
    private List<Lecturer> lecturers;

    @Element(required = false)
    private Document document;

    public String getId() {
        return id;
    }

    public String getValidTo() {
        return validTo;
    }

    public String getStatus() {
        return status;
    }

    public List<Lecturer> getLecturers() {
        return lecturers;
    }

    public Document getDocument() {
        return document;
    }
}
