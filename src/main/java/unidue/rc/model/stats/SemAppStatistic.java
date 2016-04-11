package unidue.rc.model.stats;


import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by marcus.koesters on 05.08.15.
 */
@Root(name = "run")
public class SemAppStatistic {

    @Attribute(name = "date")
    private String date;

    @Element(name = "file")
    private Integer file;

    @Element(name = "chapter")
    private Integer chapter;

    @Element(name = "entries")
    private Integer entries;

    @Element(name = "html")
    private Integer html;

    @Element(name = "freeText")
    private Integer freeText;

    @Element(name = "article")
    private Integer article;

    @Element(name = "webLink")
    private Integer webLink;

    @Element(name = "book")
    private Integer book;

    @Element(name = "headline")
    private Integer headline;

    @Element(name ="num")
    private Integer num;


    //Gesamtanzahl an Dateien
    @Element(name = "files")
    private Integer files;

    @Element(name = "milessLink")
    private Integer milessLink;

    public Integer getFiles() {
        return files;
    }

    public void setFiles(Integer files) {
        this.files = files;
    }

    public Integer getMilessLink() {
        return milessLink;
    }

    public void setMilessLink(Integer milessLink) {
        this.milessLink = milessLink;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Integer getFile() {
        return file;
    }

    public void setFile(Integer file) {
        this.file = file;
    }

    public Integer getChapter() {
        return chapter;
    }

    public void setChapter(Integer chapter) {
        this.chapter = chapter;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public Integer getEntries() {
        return entries;
    }

    public void setEntries(Integer entries) {
        this.entries = entries;
    }

    public Integer getHtml() {
        return html;
    }

    public void setHtml(Integer html) {
        this.html = html;
    }

    public Integer getFreeText() {
        return freeText;
    }

    public void setFreeText(Integer freeText) {
        this.freeText = freeText;
    }

    public Integer getArticle() {
        return article;
    }

    public void setArticle(Integer article) {
        this.article = article;
    }

    public Integer getWebLink() {
        return webLink;
    }

    public void setWebLink(Integer webLink) {
        this.webLink = webLink;
    }

    public Integer getBook() {
        return book;
    }

    public void setBook(Integer book) {
        this.book = book;
    }

    public Integer getHeadline() {
        return headline;
    }

    public void setHeadline(Integer headline) {
        this.headline = headline;
    }
}
