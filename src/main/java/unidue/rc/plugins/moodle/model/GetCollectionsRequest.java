package unidue.rc.plugins.moodle.model;


import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * Created by nils on 16.06.15.
 */
@Root(name = "get", strict = false)
public class GetCollectionsRequest implements Request {

    @Attribute
    private String username;

    @Attribute
    private String secret;

    @Attribute
    private String authtype;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getAuthtype() {
        return authtype;
    }

    public void setAuthtype(String authtype) {
        this.authtype = authtype;
    }
}
