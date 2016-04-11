package unidue.rc.model.legacy;


import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * Created by nils on 12.08.15.
 */
@Root(name = "lecturer", strict = false)
public class Lecturer {

    @Attribute(name ="email", required = false)
    private String mail;

    public String getMail() {
        return mail;
    }
}
