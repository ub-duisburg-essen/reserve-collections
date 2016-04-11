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
