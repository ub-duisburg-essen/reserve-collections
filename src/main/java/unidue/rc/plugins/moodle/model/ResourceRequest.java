package unidue.rc.plugins.moodle.model;


import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * Created by nils on 17.06.15.
 */
@Root(name = "get", strict = false)
public class ResourceRequest implements Request {

    @Attribute
    private int collectionID;

    @Attribute
    private int resourceID;

    @Attribute
    private String username;

    @Attribute
    private String secret;

    @Attribute(required = false)
    private String firstname;

    @Attribute(required = false)
    private String lastname;

    @Attribute(required = false)
    private String email;

    @Attribute
    private String authtype;

    public int getCollectionID() {
        return collectionID;
    }

    public int getResourceID() {
        return resourceID;
    }

    public String getUsername() {
        return username;
    }

    public String getSecret() {
        return secret;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getEmail() {
        return email;
    }

    public String getAuthtype() {
        return authtype;
    }
}
