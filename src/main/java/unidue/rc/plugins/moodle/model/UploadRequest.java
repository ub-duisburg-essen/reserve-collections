package unidue.rc.plugins.moodle.model;


import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * Created by nils on 22.09.15.
 */
@Root(name = "upload", strict = false)
public class UploadRequest implements Request {

    @Attribute
    private int collectionID;

    @Attribute
    private String secret;

    @Attribute
    private String filename;

    @Attribute
    private String username;

    @Attribute
    private String authtype;

    public int getCollectionID() {
        return collectionID;
    }

    public String getSecret() {
        return secret;
    }

    public String getFilename() {
        return filename;
    }

    public String getUsername() {
        return username;
    }

    public String getAuthtype() {
        return authtype;
    }
}
