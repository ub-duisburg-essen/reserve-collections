package unidue.rc.dao;


import miless.model.User;
import unidue.rc.model.*;

import java.time.LocalDate;
import java.util.List;

/**
 * A <code>ReserveCollectionDAO</code> should be used as default access object to load, update and delete {@link
 * ReserveCollection} objects from backend.
 *
 * @author Nils Verheyen
 * @see ReserveCollectionDAOImpl
 */
public interface ReserveCollectionDAO extends BaseDAO {

    String SERVICE_NAME = "ReserveCollectionDAO";

    /**
     * Returns a list with all {@link ReserveCollection} objects.
     *
     * @return a list with all reserve collections or an empty list.
     */
    List<ReserveCollection> getReserveCollections();

    /**
     * Returns a list with all {@link ReserveCollection} objects that match target location.
     *
     * @param location location for the collections
     * @return a list with all reserve collections by target location or an empty list.
     */
    List<ReserveCollection> getCollections(LibraryLocation location);

    /**
     * Returns a list of all reserve collections that target user participates and has permissions to perform target
     * action or an empty list if there are no such participations.
     *
     * @param user   see description
     * @param action see description
     * @return all evaluated collections or an empty list
     */
    List<ReserveCollection> getCollections(User user, ActionDefinition action);

    /**
     * Returns a list of collections that expire in a certain amount of days given by a base date. Collections
     * that contain a date to dissolve are excluded.
     *
     * @param baseDate            base operation date used to detect expiration
     * @param daysUntilExpiration days until expiration of the collection
     * @param status              status of the collection
     * @return a list with all collections that matches the conditions or an empty list.
     */
    List<ReserveCollection> getExpiringCollections(LocalDate baseDate, int daysUntilExpiration, ReserveCollectionStatus status);
}
