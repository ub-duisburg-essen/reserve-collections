package unidue.rc.dao;


import miless.model.LegalEntity;

/**
 * A <code>LegalEntityDAO</code> should be used as default access object to load, update and delete {@link LegalEntity}
 * objects from backend.
 *
 * @author Nils Verheyen
 * @see LegalEntityXMLFileDAO
 */
public interface LegalEntityDAO {

    /**
     * Returns the {@linkplain LegalEntity} with target id or <code>null</code> if no entity could be found
     *
     * @param id id of the {@linkplain LegalEntity} to retrieve
     * @return the found {@linkplain LegalEntity} or <code>null</code> if it does not exist
     */
    LegalEntity getLegalEntityById(Integer id);
}
