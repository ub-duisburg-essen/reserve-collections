package unidue.rc.model;


import java.util.Date;

/**
 * A <code>LibraryItem</code> is usually a special {@link Entry}, that represents a physical item inside the library.
 * Therefore it is usually safe to use it as an {@link Entry}.
 *
 * @author Nils Verheyen
 * @see Book
 * @see BookChapter
 * @see JournalArticle
 * @since 12.12.13 15:18
 */
public interface LibraryItem extends ResourceContainer {

    /**
     * Returns the id of this library item object. This is usually the entry id.
     *
     * @return id of the item
     */
    Integer getId();

    /**
     * Returns the {@link ReserveCollection} that this item belongs to.
     *
     * @return the collection the item belongs to
     */
    ReserveCollection getReserveCollection();

    /**
     * Returns the date this library item was last modified.
     *
     * @return the {@link Date} of modification
     */
    Date getModified();

    /**
     * The signature of this item. It may be possible that the signature is not present, because the item is not yet
     * present inside the library.
     *
     * @return the signature, if one is given, <code>null</code> otherwise.
     */
    String getSignature();

    /**
     * Returns the title of this item, usually the entry title.
     *
     * @return the main title of the item
     */
    String getTitle();
}
