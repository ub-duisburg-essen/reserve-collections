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
