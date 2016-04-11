package unidue.rc.model.rss;


import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by nils on 20.10.15.
 */
@Root(name = "item", strict = false)
public class Item {
    @Element(required = false)
    private String title;

    @Element(required = false)
    private String description;

    @Element(required = false)
    private String category;

    @Element(required = false)
    private String link;

    @Element(required = false)
    private String pubDate;

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }
}
