package unidue.rc.system;


import miless.model.User;
import unidue.rc.model.ReserveCollection;
import unidue.rc.model.Resource;

/**
 * Created by nils on 13.07.15.
 */
public interface BaseURLService {

    /**
     * Returns the base url that consists of a protocol, server name and optionally by a port. For example
     * <code>http://localhost:8080</code>.
     *
     * @return see description
     */
    String getBaseURL();

    /**
     * Returns the base url that consists of a protocol, server name, optionally a port and an application path
     * that is used by the web container. For example <code>http://localhost:8080/reserve-collections</code>.
     *
     * @return see description
     */
    String getApplicationURL();

    /**
     * Returns the url with which a {@link ReserveCollection} can be addressed.
     *
     * @param collection url that points to given collection
     * @return see description
     */
    String getViewCollectionURL(ReserveCollection collection);

    /**
     * Returns the url with which a {@link User} can be edited.
     *
     * @param user url that points to edit user page
     * @return see description
     */
    String getEditUserLink(User user);

    /**
     * Returns an URL with which a file of a resource can be downloaded
     *
     * @param resource url that points to download target resource
     * @return see description
     */
    String getDownloadLink(Resource resource);

    /**
     * Returns the url with which a user is able to prolong a collection.
     *
     * @param collection see description
     * @return a url in string form that points to the prolong page
     */
    String getProlongLink(ReserveCollection collection);
}
