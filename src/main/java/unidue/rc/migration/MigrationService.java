package unidue.rc.migration;


import org.apache.commons.configuration.ConfigurationException;
import unidue.rc.model.Migration;
import unidue.rc.model.ReserveCollection;

/**
 * Created by nils on 01.07.15.
 */
public interface MigrationService {

    ReserveCollection migrateReserveCollection(Migration migration, String migrationCode) throws MigrationException, ConfigurationException;
}
