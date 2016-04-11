package unidue.rc.dao;


import unidue.rc.model.Action;
import unidue.rc.model.ActionDefinition;
import unidue.rc.model.Role;

import java.util.List;

/**
 * A <code>ActionDAO</code> should be used as default access object to load,
 * update and delete {@link Action} objects from backend.
 *
 * @author Nils Verheyen
 * @see RoleDAOImpl
 */
public interface ActionDAO {

    public static final String SERVICE_NAME = "ActionDAO";

    /**
     * Returns all actions defined in backend
     *
     * @return a {@link List} with all actions or an empty list if none exists
     */
    List<Action> getActions();

    /**
     * Returns the {@link Action} with target id if one exists,
     * <code>null</code> otherwise.
     *
     * @param id identifier of the action, which should be returned
     * @return The {@link Action} that belongs to target id or <code>null</code>
     * if it does not exist.
     */
    Action getActionById(Integer id);

    /**
     * Returns a list with all {@link Action} objects that do not have a relation to target role.
     *
     * @param role see description
     * @return all unrelated actions, or an empty list
     */
    List<Action> getUnrelatedActions(Role role);

    /**
     * Returns an {@link Action} that is defined by target {@link ActionDefinition} or null if it does not exist.
     *
     * @param action definition of the action
     * @return the action if one was found, <code>null</code> otherwise
     */
    Action getAction(ActionDefinition action);
}
