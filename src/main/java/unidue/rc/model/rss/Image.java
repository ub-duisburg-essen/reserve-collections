package unidue.rc.model.rss;


import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by nils on 20.10.15.
 */
@Root(name = "image", strict = false)
public class Image {

    @Element(required = false)
    private String title;

    @Element(required = false)
    private String description;

    @Element(required = false)
    private String url;

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
