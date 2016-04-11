package unidue.rc.plugins.alephsync;


/**
 * Created by nils on 10.07.15.
 */
public class AlephSyncData {

    int collectionID;
    int deleted;
    int updated;
    int added;

    public AlephSyncData(int collectionID, int deleted, int updated, int added) {
        this.collectionID = collectionID;
        this.deleted = deleted;
        this.updated = updated;
        this.added = added;
    }

    public int getCollectionID() {
        return collectionID;
    }

    public int getDeleted() {
        return deleted;
    }

    public int getUpdated() {
        return updated;
    }

    public int getAdded() {
        return added;
    }
}
