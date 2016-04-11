package unidue.rc.model;


import org.apache.cayenne.Persistent;

/**
 * An <code>EntryValue</code> is an object that can be assigned to a {@link unidue.rc.model.ReserveCollection} via an
 * {@link unidue.rc.model.Entry}
 *
 * @author Nils Verheyen
 * @since 18.03.14 11:18
 */
public interface EntryValue extends Persistent {

    /**
     * Returns the entry assigned to this value.
     *
     * @return the entry assigned to this value.
     */
    Entry getEntry();

    /**
     * Sets the entry to this value.
     *
     * @param entry sets the entry of this value
     */
    void setEntry(Entry entry);
}
