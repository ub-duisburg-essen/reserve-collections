package unidue.rc.dao;


import miless.model.MCRCategory;

import java.util.List;
import java.util.Locale;

/**
 * An {@link OriginDAO} is a convenience interface to load {@link MCRCategory}
 * objects that belong to the classification <code>ORIGIN</code>. To access all
 * {@link MCRCategory} objects use {@link MCRCategoryDAO} instead of this
 * interface.
 *
 * @author Nils Verheyen
 * @see OriginDAOImpl
 */
public interface OriginDAO {

    /**
     * Returns a {@link List} of all origins exept the root origin, that are
     * available inside the backend.
     *
     * @return a list of all origins that are available or an empty list.
     */
    List<MCRCategory> getOrigins();

    MCRCategory getOrigin(String id);

    String getOriginLabel(Locale locale, Integer originID);
}
