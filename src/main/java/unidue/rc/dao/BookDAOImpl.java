package unidue.rc.dao;


import org.apache.cayenne.BaseContext;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.query.NamedQuery;
import unidue.rc.model.Book;
import unidue.rc.model.ReserveCollection;

import java.util.Collections;
import java.util.List;

/**
 * @author Nils Verheyen
 * @since 29.11.13 10:25
 */
public class BookDAOImpl extends EntryDAOImpl implements BookDAO {

    @Override
    public List<Book> getBooksByCollection(ReserveCollection collection) {
        NamedQuery query = new NamedQuery("select_books_by_collection", Collections.singletonMap("collectionID",
                collection.getId()));

        ObjectContext context = BaseContext.getThreadObjectContext();
        List<Book> books = context.performQuery(query);
        return books != null ? books : Collections.EMPTY_LIST;
    }
}
