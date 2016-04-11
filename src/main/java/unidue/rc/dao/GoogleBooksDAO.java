package unidue.rc.dao;


/**
 * See: <a href="http://code.google.com/apis/explorer">Googles API Explorer</a> for sample requests
 */
public interface GoogleBooksDAO {

    /**
     * Returns a thumbnail url for target isbn number or <code>null</code> if nothing could be found.
     *
     * @param isbn isbn that the thumbnail should be returned for
     * @return url pointing to the thumbnail if one could be found, <code>null</code> otherwise
     */
    String getThumbnail(String isbn);
}
