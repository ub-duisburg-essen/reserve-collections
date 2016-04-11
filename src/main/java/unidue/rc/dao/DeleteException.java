package unidue.rc.dao;


public class DeleteException extends Exception {

    private static final long serialVersionUID = 1L;

    public DeleteException(String message, Throwable cause) {
        super(message, cause);
    }

    public DeleteException(String message) {
        super(message);
    }

    public DeleteException(Throwable cause) {
        super(cause);
    }

}
