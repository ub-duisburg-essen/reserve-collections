package unidue.rc.migration.legacymodel;


import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;
import unidue.rc.migration.CopyrightReviewMapping;
import unidue.rc.migration.MigrationException;
import unidue.rc.model.Entry;
import unidue.rc.model.ReserveCollection;

@Root(name = "book", strict = false)
public class BookLocal implements MigrationVisitable, ResourceValue {

    @Attribute(name = "nonLendingCollection", required = false)
    private boolean nonLendingCollection = false;

    @Attribute(name = "ref", required = false)
    private String collectionRef;

    @Element(name = "title", required = false)
    private String title;

    @Element(name = "author", required = false)
    private String author;

    @Element(name = "editor", required = false)
    private String editor;

    @Element(name = "publisher", required = false)
    private String publisher;

    @Element(name = "place", required = false)
    private String place;

    @Element(name = "year", required = false)
    private String year;

    @Element(name = "isbn", required = false)
    private String isbn;

    @Element(name = "signature", required = false)
    private String signature;
    
    @Element(name = "comment", required = false)
    private String comment;
    
    @Element(name = "url", required = false)
    private String url;
    
    @Element(name = "edition", required = false)
    private String edition;
    
    @Path("text")
    @Element(name = "path", required = false)
    private String path;

    @Path("text")
    @Element(name = "attachmentNo", required = false)
    private String refNo;

    @Path("text")
    @Element(name = "volume", required = false)
    private String volume;

    /**
     * @return the nonLendingCollection
     */
    public boolean isNonLendingCollection() {
        return nonLendingCollection;
    }

    public String getCollectionRef() {
        return collectionRef;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the publisher
     */
    public String getPublisher() {
        return publisher;
    }

    /**
     * @return the place
     */
    public String getPlace() {
        return place;
    }

    /**
     * @return the year
     */
    public String getYear() {
        return year;
    }

    /**
     * @return the isbn
     */
    public String getIsbn() {
        return isbn;
    }

    /**
     * @return the signature
     */
    public String getSignature() {
        return signature;
    }

    @Override
    public String toString() {
        return "BookLocal [author= " + author + " title= " + title + " isbn= " + isbn + "]";
    }

    /**
     * @return the author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * @return the editor
     */
    public String getEditor() {
        return editor;
    }

    /**
     * @return the comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    @Override
    public String getReviewStatus() {
        return CopyrightReviewMapping.ReviewedFree.getLegacyStatus();
    }

    /**
     * @return the edition
     */
    public String getEdition() {
        return edition;
    }

    /**
     * @return the refNo
     */
    public String getRefNo() {
        return refNo;
    }

    public String getVolume() {
        return volume;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    @Override
    public void migrateEntryValue(MigrationVisitor visitor, ReserveCollection collection, Entry entry, EntryLocal entryLocal, String derivateID) throws MigrationException {
        visitor.migrate(this, collection, entry, entryLocal, derivateID);
    }
}
