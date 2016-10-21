package unidue.rc.dao;

import unidue.rc.model.LibraryLocation;
import unidue.rc.model.OrderMailRecipient;

import javax.mail.internet.InternetAddress;
import java.util.List;

/**
 * A <code>OrderMailRecipientDAO</code> can be used to run crud operations on {@linkplain OrderMailRecipient} objects
 * inside the backend.
 * <p>
 * Created by nils on 20.10.16.
 */
public interface OrderMailRecipientDAO extends BaseDAO {

    String SERVICE_NAME = "OrderMailRecipientDAO";

    /**
     * Retrieves all {@linkplain OrderMailRecipient} object from backend that match given location and class.
     *
     * @param location      location to match against
     * @param instanceClass class to match against
     * @return a list with all recipients or an empty list if nothing was found
     */
    List<OrderMailRecipient> getRecipients(LibraryLocation location, Class instanceClass);

    /**
     * Adds a mail address that is responsible to handle orders to a specific object instance in a given location.
     *
     * @param location      location where orders are created
     * @param mail          mail of the user that is responsible of the order
     * @param instanceClass instance class of the object
     * @throws CommitException thrown if any object could not be saved in backend
     */
    OrderMailRecipient addOrderMailRecipient(LibraryLocation location, InternetAddress mail, Class instanceClass) throws CommitException;
}
