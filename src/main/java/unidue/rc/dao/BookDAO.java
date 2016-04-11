package unidue.rc.dao;


import unidue.rc.model.Book;
import unidue.rc.model.ReserveCollection;

import java.util.List;

/**
 * @author Nils Verheyen
 * @since 29.11.13 10:25
 */
public interface BookDAO extends EntryDAO {
    static final String SERVICE_NAME = "BookDAO";

    List<Book> getBooksByCollection(ReserveCollection collection);
}
