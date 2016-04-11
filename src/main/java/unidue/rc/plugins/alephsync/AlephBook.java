package unidue.rc.plugins.alephsync;


import unidue.rc.model.Book;

/**
 * Created by nils on 29.06.15.
 */
public class AlephBook extends Book {

    private String recordID;

    public String getRecordID() {
        return recordID;
    }

    public void setRecordID(String recordID) {
        this.recordID = recordID;
    }
}
