package unidue.rc.plugins.moodle.model;


import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * Created by nils on 28.09.15.
 */
@Root(name = "delete", strict = false)
public class DeleteRequest implements Request {

    @Attribute
    private int resourceID;

    @Attribute
    private String secret;

    @Attribute
    private String username;

    @Attribute
    private String authtype;

    public int getResourceID() {
        return resourceID;
    }

    public String getSecret() {
        return secret;
    }

    public String getUsername() {
        return username;
    }

    public String getAuthtype() {
        return authtype;
    }

}
