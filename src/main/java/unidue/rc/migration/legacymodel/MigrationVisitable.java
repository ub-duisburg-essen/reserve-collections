package unidue.rc.migration.legacymodel;


import unidue.rc.migration.MigrationException;
import unidue.rc.model.Entry;
import unidue.rc.model.ReserveCollection;

/**
 * @author Nils Verheyen
 * @since 07.04.14 15:42
 */
public interface MigrationVisitable {

    void migrateEntryValue(MigrationVisitor visitor, ReserveCollection collection, Entry entry, EntryLocal entryLocal, String derivateID) throws MigrationException;
}
