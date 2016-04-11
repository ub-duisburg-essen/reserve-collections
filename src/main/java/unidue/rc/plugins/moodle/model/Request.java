package unidue.rc.plugins.moodle.model;


/**
 * A <code>Request</code> contains data that each http request from moodle must contain.
 */
public interface Request {

    /**
     * Returns the username for target request.
     *
     * @return the username for the request.
     */
    String getUsername();

    /**
     * Returns the secret defined between this instance and moodle.
     *
     * @return the secret used in this request
     */
    String getSecret();

    /**
     * Returns the auth type that is used by moodle.
     *
     * @return the authentication type used by moodle
     * @see unidue.rc.plugins.moodle.services.MoodleService#getRealm(String)
     */
    String getAuthtype();
}
