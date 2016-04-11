package unidue.rc.dao;


import miless.model.MCRCategory;

/**
 * An instance of {@link MCRCategoryDAO} can be used to load {@link MCRCategory}
 * objects from backend.
 *
 * @author Nils Verheyen
 * @see MCRCategoryDAOImpl
 */
public interface MCRCategoryDAO {

    /**
     * Returns the {@link MCRCategory} with target id if one is available.
     *
     * @param id id of the category
     * @return a {@link MCRCategory} if it could be found, <code>null</code>
     * otherwise.
     */
    MCRCategory getCategoryById(Integer id);
}
