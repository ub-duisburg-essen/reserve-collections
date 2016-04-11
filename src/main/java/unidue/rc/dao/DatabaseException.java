package unidue.rc.dao;


/**
 * An instance of <code>DatabaseException</code> should be used if any exception regarding connections to
 * the data in use occure.
 *
 * @author Nils Verheyen
 * @since 17.06.13 10:55
 */
public class DatabaseException extends Exception {

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
