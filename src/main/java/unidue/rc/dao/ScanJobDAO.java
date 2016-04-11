package unidue.rc.dao;


import unidue.rc.model.LibraryItem;
import unidue.rc.model.LibraryLocation;
import unidue.rc.model.ReserveCollection;
import unidue.rc.model.ScanJob;

import java.util.List;

/**
 * A <code>ScanJobDAO</code> should be used as default access object to load, update and delete {@link
 * unidue.rc.model.ScanJob} objects from backend.
 *
 * @author Nils Verheyen
 * @since 11.12.13 16:45
 */
public interface ScanJobDAO extends BaseDAO {

    static final String SERVICE_NAME = "ScanJobDAO";

    /**
     * Retrieves a {@link java.util.List} of all available {@link unidue.rc.model.ScanJob} objects in backend.
     *
     * @return a list with all jobs or an empty list
     */
    List<ScanJob> getJobs();

    /**
     * Returns the {@link unidue.rc.model.LibraryItem} that is assigned to target scan job.
     *
     * @param scanJob scan job of the item
     * @return returns the library target job is assigned to, <code>null</code> otherwise.
     * @see unidue.rc.model.JournalArticle
     * @see unidue.rc.model.BookChapter
     */
    LibraryItem getLibraryItem(ScanJob scanJob);
}
