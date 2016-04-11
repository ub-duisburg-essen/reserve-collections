package unidue.rc.migration.legacymodel;


import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import unidue.rc.migration.MigrationException;
import unidue.rc.model.Entry;
import unidue.rc.model.ReserveCollection;

@Root(name = "file", strict = false)
public class FileLocal implements MigrationVisitable, ResourceValue {

    @Element(name = "path", required = false)
    private String path;

    @Element(name = "label", required = false)
    private String label;

    @Element(name = "itemStatus", required = false)
    private String reviewStatus;

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    @Override
    public String getUrl() {
        return null;
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    public String getReviewStatus() {
        return reviewStatus;
    }

    @Override
    public String toString() {
        return "FileLoca [path= " + path + " label= " + label + "]";
    }

    @Override
    public void migrateEntryValue(MigrationVisitor visitor, ReserveCollection collection, Entry entry, EntryLocal entryLocal, String derivateID) throws MigrationException {
        visitor.migrate(this, collection, entry, entryLocal, derivateID);
    }
}
