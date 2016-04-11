package unidue.rc.model.rss;


import org.simpleframework.xml.*;

import java.util.List;

/**
 * Created by nils on 20.10.15.
 */
@Root(strict = false, name = "channel")
public class Channel {

    @Element(required = false)
    private String title;

    @Element(required = false)
    private String description;

    @Element(required = false)
    private String managingEditor;

    @Element(required = false)
    private String docs;

    @Element(required = false)
    private String generator;

    @Element(required = false)
    private String copyright;

    @Element(required = false)
    private String webMaster;

    @Element(required = false)
    private String category;

    @Element(required = false)
    private String link;


    @Element(required = false)
    private Image image;

    @ElementList(inline = true, required = false)
    private List<Item> items;

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getManagingEditor() {
        return managingEditor;
    }

    public void setManagingEditor(String managingEditor) {
        this.managingEditor = managingEditor;
    }

    public String getDocs() {
        return docs;
    }

    public void setDocs(String docs) {
        this.docs = docs;
    }

    public String getGenerator() {
        return generator;
    }

    public void setGenerator(String generator) {
        this.generator = generator;
    }

    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    public String getWebMaster() {
        return webMaster;
    }

    public void setWebMaster(String webMaster) {
        this.webMaster = webMaster;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }
}
