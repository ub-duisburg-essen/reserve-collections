package unidue.rc.dao;


/**
 * A <code>CommitException</code> should be used when errors occur during create or update of objects.
 *
 * @author Nils Verheyen
 * @since 04.11.13 10:32
 */
public class CommitException extends Exception {

    public CommitException(String message) {
        super(message);
    }

    public CommitException(String message, Throwable cause) {
        super(message, cause);
    }
}
