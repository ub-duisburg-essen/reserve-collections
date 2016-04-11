package unidue.rc.dao;


import unidue.rc.model.Migration;

import java.util.List;

/**
 * Created by nils on 06.07.15.
 */
public interface MigrationDAO extends BaseDAO {

    String SERVICE_NAME = "MigrationDAO";

    /**
     * Returns the {@link Migration} object that is associated with target document id, or <code>null</code> if
     * it does not exist.
     *
     * @param documentID document id of the migration
     * @return the migration or <code>null</code> if none was found
     */
    Migration getMigrationByDocID(String documentID);
}
