package unidue.rc.dao;


import miless.model.User;
import org.apache.cayenne.validation.ValidationException;
import unidue.rc.model.*;

import java.util.List;

/**
 * A <code>RoleDAO</code> should be used as default access object to load, update and delete {@link Role} objects from
 * backend.
 *
 * @author Nils Verheyen
 * @see RoleDAOImpl
 */
public interface RoleDAO extends BaseDAO {

    String SERVICE_NAME = "RoleDAO";

    /**
     * Returns all {@link Role} object from backend.
     *
     * @return all roles or an empty list if none exists.
     */
    List<Role> getRoles();

    /**
     * Returns all {@link Role}s target user is member of.
     *
     * @param user user to use
     * @return all roles or an empty list if none exists.
     */
    List<Role> getRoles(User user);


    /**
     * Returns all {@link unidue.rc.model.Role}s which target user is member of as default.
     *
     * @param user user to use
     * @return all memberships or an empty list
     */
    List<Membership> getMembership(User user);


    /**
     * Returns all {@link Action} object that have a relation to target {@link Role}.
     *
     * @param role role, which associated actions should be returned
     * @return all actions associated with the role or an empty list
     */
    List<Action> getActions(Role role);

    /**
     * Returns the {@link Role} with target id, <code>null</code> if does not exist
     *
     * @param id the id of the role
     * @return the role by given id or <code>null</code>
     */
    Role getRoleById(Integer id);

    /**
     * Returns the {@link Role} that belongs to target {@link DefaultRole}.
     *
     * @param defaultRole role that defines the result
     * @return the role associated to target default role or <code>null</code> if it does not exist
     */
    Role getRole(DefaultRole defaultRole);

    /**
     * Deletes target {@link unidue.rc.model.Membership} inside backend.
     *
     * @param membership the membership to be deleted.
     * @throws DeleteException thrown if any that inside the {@link LibraryLocation} is invalid. take a look at the
     *                         modeler to see required values.
     */
    void delete(Membership membership) throws DeleteException;
}
