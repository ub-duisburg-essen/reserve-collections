package unidue.rc.migration.legacymodel;


import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Root(name = "semesterapparat", strict = false)
public class ReserveCollectionLocal {

    @Attribute(name = "ID")
    private String collectionID;

    @Attribute(name = "active", required = false)
    private boolean isActive;

    @ElementList(inline = true, required = false)
    private List<EntryLocal> entries;

    private String docID;

    private SlotLocal slotinfo;

    public String getCollectionID() {
        return collectionID;
    }

    public void setCollectionID(String collectionID) {
        this.collectionID = collectionID;
    }

    public boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    /**
     * @return the entries
     */
    public List<EntryLocal> getEntries() {
        return entries != null
                ? entries.stream()
                    .filter(entry -> !entry.isDeleted())
                    .collect(Collectors.toList())
                : Collections.EMPTY_LIST;
    }

    @Override
    public String toString() {
        return "ReserveCollection [isActive=" + isActive + " id= " + collectionID + "]";
    }

    public String getDocID() {
        return docID;
    }

    public void setDocID(String docID) {
        this.docID = docID;
    }

    /**
     * @return the slotinfo
     */
    public SlotLocal getSlotinfo() {
        return slotinfo;
    }
}
