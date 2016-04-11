package unidue.rc.dao;


import unidue.rc.model.Entry;
import unidue.rc.model.EntryValue;
import unidue.rc.model.ReserveCollection;

import java.util.List;

/**
 * A <code>EntryDAO.java.java</code> should be used as default access object to load, update and delete {@link Entry}
 * objects from backend.
 *
 * @author Marcus Koesters
 * @see EntryDAOImpl
 */
public interface EntryDAO extends BaseDAO {

    String SERVICE_NAME = "EntryDAO";

    /**
     * Creates a new entry that connects the value to target collection.
     *
     * @param collection contains the reserve collection that target value should be associated to
     * @return the new created entry for given collection
     * @throws CommitException when one of the objects could not be created
     */
    Entry createEntry(ReserveCollection collection) throws CommitException;

    /**
     * Creates target {@link EntryValue} and a new {@link Entry} inside target {@link ReserveCollection}.
     *
     * @param value      value that should be used in target collection
     * @param collection collection which is used
     * @throws CommitException when one of the objects could not be created
     */
    void createEntry(EntryValue value, ReserveCollection collection) throws CommitException;

    /**
     * Returns a list with all {@link Entry} objects.
     *
     * @param rc the collection which entries should be returned
     * @return a list with all reserve collections or an empty list.
     */
    List<Entry> getEntries(ReserveCollection rc);

    /**
     * Moves target entry below base entry.
     *
     * @param target entry that should be moved
     * @param base   base where the target should be moved to
     * @throws CommitException thrown if one of the objects could not be saved
     */
    void move(Entry target, Entry base) throws CommitException;
}
