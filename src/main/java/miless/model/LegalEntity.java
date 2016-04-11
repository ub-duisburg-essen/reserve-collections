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
package miless.model;


import org.joda.time.DateTime;
import org.simpleframework.xml.*;
import org.simpleframework.xml.convert.Convert;
import unidue.rc.io.DayOfBirthConverter;

import java.util.List;

/**
 * A <code>LegalEntity</code> is the metadata container to a {@linkplain miless.model.User}.
 *
 * @author Nils Verheyen
 */
@Root(name = "legalEntity", strict = false)
public class LegalEntity {

    @Attribute(name = "ID")
    private Integer id;

    @Attribute(name = "type")
    private String type;

    @Attribute(name = "pid", required = false)
    private Integer pid;

    @Element(name = "title", required = false)
    private String title;

    @Path("born")
    @Element(name = "place", required = false)
    private String placeOfBirth;

    @Path("born")
    @Element(name = "date", required = false)
    @Convert(DayOfBirthConverter.class)
    private DateTime dayOfBirth;

    @ElementList(name = "names", entry = "name")
    private List<String> names;

    @Element(name = "origin")
    private String origin;

    @Element(name = "comment")
    private String comment;

    @ElementList(name = "contacts", required = false)
    private List<Contact> contacts;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getPid() {
        return pid;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPlaceOfBirth() {
        return placeOfBirth;
    }

    public void setPlaceOfBirth(String placeOfBirth) {
        this.placeOfBirth = placeOfBirth;
    }

    public DateTime getDayOfBirth() {
        return dayOfBirth;
    }

    public void setDayOfBirth(DateTime dayOfBirth) {
        this.dayOfBirth = dayOfBirth;
    }

    public List<String> getNames() {
        return names;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
    }
}
