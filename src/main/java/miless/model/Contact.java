package miless.model;


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
