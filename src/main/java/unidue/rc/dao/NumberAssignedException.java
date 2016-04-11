package unidue.rc.dao;


/**
 * Created by nils on 29.07.15.
 */
public class NumberAssignedException extends Exception {

    public NumberAssignedException(String message) {
        super(message);
    }

    public NumberAssignedException(String message, Throwable cause) {
        super(message, cause);
    }

    public NumberAssignedException(Throwable cause) {
        super(cause);
    }
}
