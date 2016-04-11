package unidue.rc.dao;


import miless.model.User;

import java.util.Collection;
import java.util.List;

/**
 * A <code>UserDAO</code> should be used as default access object to load, update and delete {@link User} objects from
 * backend.
 *
 * @author Nils Verheyen
 * @see RoleDAOImpl
 */
public interface UserDAO extends BaseDAO {

    String SERVICE_NAME = "UserDAO";

    /**
     * Returns the {@link User} with target id if one exists, <code>null</code> otherwise.
     *
     * @param userId user id
     * @return the user or <code>null</code> if none could be found
     */
    User getUserById(Integer userId);

    /**
     * Returns a {@link List} of all users kept inside backend
     *
     * @return a new list with all users. the list is empty if no users are stored.
     */
    List<User> getUsers();

    /**
     * Returns a {@link List} of users that got an id of target user ids.
     *
     * @param userIDs collection of user ids
     * @return a new list with all users. the list is empty if no users are stored.
     */
    List<User> getUsers(Collection<Integer> userIDs);

    /**
     * Returns the user with target username if one exists.
     *
     * @param username username to use
     * @return {@link User} with target name or <code>null</code> if it does not exist
     */
    User getUser(String username);

    /**
     * Returns the user identified by target username and realmid.
     *
     * @param username username to use during selection
     * @param realmID  realmID to use during selection
     * @return the {@linkplain User} which was found, <code>null</code> otherwise.
     */
    User getUser(String username, String realmID);

    /**
     * Returns the users identified by target legal entity id if one or more could be found, an empty list otherwise.
     *
     * @param legalEntityID id of the legal entity
     * @return the list of all users or an empty list
     */
    List<User> getUsersByLegalEntityId(Integer legalEntityID);

    /**
     * Searches for a user which username, real name or email contains target word as substring.
     *
     * @param searchWord text to search for in username, real name and email.
     * @return a list of {@link miless.model.User}s that match the search word or an empty list.
     */
    List<User> search(String searchWord);

    /**
     * Normalizes target username, that it can be used for further access.
     *
     * @param username to normalize
     * @return normalized username
     */
    String normalizeUsername(String username);

    /**
     * Checks if a user with target username already exists in db.
     *
     * @param username username to use
     * @return <code>true</code> if a user already exists, <code>false</code> otherwise
     */
    boolean exists(String username);
}
