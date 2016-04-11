package unidue.rc.migration;


/**
 * @author Nils Verheyen
 * @since 07.04.14 11:44
 */
public class MigrationException extends Exception {
    public MigrationException(Throwable cause) {
        super(cause);
    }

    public MigrationException(String message) {
        super(message);
    }

    public MigrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
