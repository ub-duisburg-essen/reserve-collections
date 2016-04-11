package unidue.rc.model.legacy;


import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * Created by nils on 12.08.15.
 */
@Root(name = "document", strict = false)
public class Document {

    @Attribute(name = "ID")
    private Integer id;

    @Attribute(name = "title")
    private String title;

    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
}
