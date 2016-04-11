package unidue.rc.migration.legacymodel;


import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import unidue.rc.model.Entry;
import unidue.rc.model.ReserveCollection;

@Root(name = "webLink", strict = false)
public class WeblinkLocal implements MigrationVisitable {

    @Element(name = "url", required = false)
    private String url;

    @Element(name = "label", required = false)
    private String label;

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return "WeblinkLocal [url=" + url + " label= " + label + "]";
    }

    @Override
    public void migrateEntryValue(MigrationVisitor visitor, ReserveCollection collection, Entry entry, EntryLocal entryLocal, String derivateID) {
        visitor.migrate(this, collection, entry, entryLocal, derivateID);
    }
}
