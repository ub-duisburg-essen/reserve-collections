package unidue.rc.plugins.moodle.model;


import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * Created by nils on 16.06.15.
 */
@Root(name = "moodle")
public class Moodle {

    @Attribute(name = "data")
    private String data;

    @Attribute(name = "skey")
    private String symKey;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getSymKey() {
        return symKey;
    }

    public void setSymKey(String symKey) {
        this.symKey = symKey;
    }
}
