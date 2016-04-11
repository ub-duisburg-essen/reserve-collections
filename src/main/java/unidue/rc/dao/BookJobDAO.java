package unidue.rc.dao;


import unidue.rc.model.BookJob;
import unidue.rc.model.LibraryLocation;
import unidue.rc.model.ReserveCollection;

import java.util.List;

/**
 * A <code>BookJobDAO</code> should be used as default access object to load, update and delete {@link BookJob} objects
 * from backend.
 *
 * @author Nils Verheyen
 * @since 29.11.13 09:50
 */
public interface BookJobDAO extends BaseDAO {

    String SERVICE_NAME = "BookJobDAO";

    /**
     * Retrieves a {@link List} of all available {@link BookJob} objects in backend.
     *
     * @return a list with all jobs or an empty list
     */
    List<BookJob> getJobs();

    /**
     * Returns the {@link unidue.rc.model.BookJob} with target id if one was found.
     *
     * @param bookJobID  id of the book job
     * @return the book job, <code>null</code> otherwise
     */
    BookJob getJob(Integer bookJobID);
}
