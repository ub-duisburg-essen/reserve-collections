package unidue.rc.migration.legacymodel;


import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;
import unidue.rc.migration.MigrationException;
import unidue.rc.model.Entry;
import unidue.rc.model.ReserveCollection;

@Root(name = "article", strict = false)
public class ArticleLocal implements MigrationVisitable, ResourceValue {

    @Element(name="signature", required = false)
    private String signature;
    
    @Element(name = "author", required = false)
    private String author;

    @Element(name = "title", required = false)
    private String title;

    @Path("location")
    @Element(name = "volume", required = false)
    private String volume;

    @Path("location")
    @Element(name = "issue", required = false)
    private String issue;

    @Path("location/pages")
    @Attribute(name = "from", required = false)
    private String pageFrom;

    @Path("location/pages")
    @Attribute(name = "to", required = false)
    private String pageTo;

    @Path("journal")
    @Element(name = "title", required = false)
    private String journalTitle;

    @Path("journal")
    @Element(name = "place", required = false)
    private String journalPlace;

    @Path("journal")
    @Element(name = "publisher", required = false)
    private String journalPublisher;

    @Path("journal")
    @Element(name = "issn", required = false)
    private String journalIssn;

    @Path("text")
    @Element(name = "path", required = false)
    private String path;

    @Path("text")
    @Element(name = "attachmentNo", required = false)
    private String refNo;
    
    
    @Path("text")
    @Element(name = "url", required = false)
    private String url;
    
    @Override
    public String toString() {
        return "ArticleLocal [author= " + author + " title= " + title + "]";
    }
    
    @Element(name = "comment", required = false)
    private String comment;

    @Path("text")
    @Element(name = "itemStatus", required = false)
    private String reviewStatus;

    /**
     * @return the author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }


    /**
     * @return the volume
     */
    public String getVolume() {
        return volume;
    }

    /**
     * @return the issue
     */
    public String getIssue() {
        return issue;
    }

    /**
     * @return the pageFrom
     */
    public String getPageFrom() {
        return pageFrom;
    }

    /**
     * @return the pageTo
     */
    public String getPageTo() {
        return pageTo;
    }

    /**
     * @return the journalTitle
     */
    public String getJournalTitle() {
        return journalTitle;
    }

    /**
     * @return the journalPlace
     */
    public String getJournalPlace() {
        return journalPlace;
    }

    /**
     * @return the journalPublisher
     */
    public String getJournalPublisher() {
        return journalPublisher;
    }

    /**
     * @return the journalIssn
     */
    public String getJournalIssn() {
        return journalIssn;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @return the comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * @return the signature
     */
    public String getSignature() {
        return signature;
    }

    /**
     * @return the refNo
     */
    public String getRefNo() {
        return refNo;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    public String getReviewStatus() {
        return reviewStatus;
    }

    @Override
    public void migrateEntryValue(MigrationVisitor visitor, ReserveCollection collection, Entry entry, EntryLocal entryLocal, String derivateID) throws MigrationException {
        visitor.migrate(this, collection, entry, entryLocal, derivateID);
    }
}
