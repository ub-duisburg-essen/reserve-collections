package unidue.rc.workflow;


import unidue.rc.dao.CommitException;
import unidue.rc.dao.DeleteException;
import unidue.rc.model.*;

/**
 * With a {@code BookService} crud operations on {@link unidue.rc.model.Book} objects should be performed. All necessary
 * operations regarding a book are executed.
 *
 * @author Nils Verheyen
 * @since 05.12.13 09:27
 */
public interface BookService {

    /**
     * Creates and sets a new {@link unidue.rc.model.Entry} for target book, that belongs to target {@link
     * unidue.rc.model.ReserveCollection}
     *
     * @param book       book to create
     * @param collection collection to use for the book
     * @throws CommitException if any object could not be saved
     * @see unidue.rc.dao.BookDAO#createEntry(EntryValue, ReserveCollection)
     */
    void create(Book book, ReserveCollection collection) throws CommitException;

    /**
     * Removes target book from backend and runs all necessary operations besides the removal of the book.
     *
     * @param book book to delete
     * @throws DeleteException if the book could not be deleted
     * @see unidue.rc.dao.BookDAO#update(Object)
     */
    void delete(Book book) throws DeleteException;

    /**
     * Updates target book in backend and runs all necessary operations besides the update of the book.
     *
     * @param book        book to update
     * @param fullTextURL fulltext url for the book
     * @throws CommitException if any object could not be saved
     * @see unidue.rc.dao.BookDAO#update(Object)
     */
    void update(Book book, String fullTextURL) throws CommitException;

    /**
     * Should be called after a {@link ReserveCollection} was updated in backend.
     *
     * @param collection collection that was updated
     */
    void afterCollectionUpdate(ReserveCollection collection);

    void beforeEntryDelete(Entry entry);

    void afterEntryDelete(Entry entry);

    void afterEntryUpdate(Entry entry) throws CommitException;
}
