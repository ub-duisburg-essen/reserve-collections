package unidue.rc.migration.filefilter;


/**
 * The ReserveFilter returns true if a given File has a special suffix.
 * 
 * @author Marcus Koesters

 */

import java.io.File;
import java.io.FileFilter;

public class ReserveCollectionFileFilter implements FileFilter {

    private static String COLLECTION_FILE_SUFFIX = ".msa";

    private String documentID;

    public ReserveCollectionFileFilter() {
    }

    public ReserveCollectionFileFilter(String documentID) {
        this.documentID = documentID;
    }

    @Override
    public boolean accept(File pathname) {
        String filename = pathname.getName();
        int startIndexDocID = filename.lastIndexOf('-');
        int endIndexDocID = filename.lastIndexOf('_');
        return documentID != null // document id must be present
                && startIndexDocID >= 0 // start index of document id
                && endIndexDocID > startIndexDocID // end index of document > start index
                && filename.substring(startIndexDocID + 1, endIndexDocID).equals(documentID) // doc id present?
                && filename.endsWith(COLLECTION_FILE_SUFFIX); // ends with .msa
    }

}
