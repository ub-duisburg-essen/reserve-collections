package unidue.rc.system;


/**
 * Created by nils on 25.06.15.
 */
public interface SystemMessageService {

    /**
     * Returns the message belonging to target key, or <code>null</code> if it is not present.
     *
     * @param key key for message
     * @return the message or <code>null</code> if none was be found
     * @see org.apache.commons.configuration.Configuration#getString(String)
     */
    String get(String key);
}
