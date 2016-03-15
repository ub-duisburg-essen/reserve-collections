package unidue.rc.dao;

import java.util.List;

import unidue.rc.model.Department;

/**
 * A <code>DepartmentDAO</code> should be used as default access object to load,
 * update and delete {@link Department} objects from backend.
 *
 * @author Paul Rochowski
 * @see DepartmentDAOImpl
 */
public interface DepartmentDAO {

    /**
     * Returns all {@link Department} objects that have no parent.
     *
     * @return the list with all departments or an empty list.
     */
    List<Department> getRootDepartments();

    /**
     * Returns the {@link Department} with target id, <code>null</code> if it
     * does not exist.
     *
     * @param id
     *            id of the department
     * @return the department if one could be found, <code>null</code> otherwise
     */
    Department getDepartmentById(Integer id);

    /**
     * Stores target {@link Department} in backend.
     *
     * @param department
     *            department to create
     * @throws CommitException
     *             thrown if any that inside the {@link department} is invalid.
     *             Take a look at the modeler to see required values.
     */
    void create(Department department) throws CommitException;

    /**
     * Deletes target {@link Department} in backend.
     *
     * @param department
     *            {@link Department} which should be deleted.
     * @throws DeleteException
     *             thrown if a error occured during delete of target
     *             {@link Department}
     */
    void delete(Department department) throws DeleteException;

    /**
     * Updates target {@link Department} in backend.
     *
     * @param location
     *            {@link Department} which should be updated.
     * @throws CommitException
     *             thrown if any that inside the {@link Department} is invalid.
     *             Take a look at the modeler to see required values.
     */
    void update(Department department) throws CommitException;

    /**
     * Returns a list of all available {@link Department} objects.
     *
     * @return a list with all department or an empty list.
     */
    List<Department> getDepartments();
}



