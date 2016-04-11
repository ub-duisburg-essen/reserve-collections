package unidue.rc.dao;


import unidue.rc.model.Entry;
import unidue.rc.model.Headline;

/**
 * @author Nils Verheyen
 * @since 08.01.14 09:04
 */
public interface HeadlineDAO extends EntryDAO {

    String SERVICE_NAME = "HeadlineDAO";

    /**
     * Moves target existing {@link Entry} to the end of entry after target headline and moves it to the last position
     * below target headline. The collection of target entry and headline must not be <code>null</code>.
     *
     * @param entry    entry to add to the headline/reserve collection
     * @param headline contains the headline which should be added
     * @throws CommitException thrown if one of the objects could not be saved
     */
    void move(Entry entry, Headline headline) throws CommitException;

    /**
     * Returns the next headline inside the collection of target headline according to the sort of entries.
     *
     * @param headline target headline
     * @return the next headline if one could be found, <code>null</code> otherwise.
     */
    Headline getNextHeadline(Headline headline);
}
