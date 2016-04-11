package unidue.rc.ui.pages.migration;


import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.slf4j.Logger;
import se.unbound.tapestry.breadcrumbs.BreadCrumb;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.MigrationDAO;
import unidue.rc.migration.MigrationException;
import unidue.rc.migration.MigrationService;
import unidue.rc.model.ActionDefinition;
import unidue.rc.model.Migration;
import unidue.rc.model.ReserveCollection;
import unidue.rc.security.RequiresActionPermission;
import unidue.rc.ui.ProtectedPage;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by nils on 01.07.15.
 */
@BreadCrumb(titleKey = "migrate-title")
@ProtectedPage
public class Index {

    @Inject
    private Logger log;

    @Inject
    private MigrationService migrationService;

    @Inject
    private MigrationDAO migrationDAO;

    @Inject
    private Messages messages;

    @Inject
    private PageRenderLinkSource linkSource;

    @Component(id = "migrationForm")
    private Form migrationForm;

    @Property
    @Validate("required")
    private String documentID;

    @Property
    @Persist(PersistenceConstants.FLASH)
    private String stacktrace;

    @Property
    @Persist(PersistenceConstants.FLASH)
    private String migrationMessage;

    @Property
    @Persist(PersistenceConstants.FLASH)
    private Integer number;

    @Property
    @Persist(PersistenceConstants.FLASH)
    private Integer collectionID;

    @Property
    @Persist(PersistenceConstants.FLASH)
    private ReserveCollection collection;


    @RequiresActionPermission(ActionDefinition.MIGRATE_OLD_COLLECTIONS)
    void onActivate() {
    }

    @OnEvent(value = EventConstants.VALIDATE, component = "migrationForm")
    void onValidateFromMigrationForm() {
        try {
            Migration migration = migrationDAO.getMigrationByDocID(documentID);
            if (migration == null) {

                migration = new Migration();
                migration.setMigrationCode(RandomStringUtils.random(16, true, true));
                migration.setDocumentID(Integer.valueOf(documentID));
                migrationDAO.create(migration);
            }

            collection = migrationService.migrateReserveCollection(migration, migration.getMigrationCode());
        } catch (MigrationException e) {
            migrationForm.recordError(messages.format("error.msg.could.not.migrate.collection", documentID));
            migrationForm.recordError(e.getMessage());

            StringWriter output = new StringWriter();
            PrintWriter writer = new PrintWriter(output);
            e.printStackTrace(writer);
            stacktrace = output.toString();
            writer.close();

        } catch (ConfigurationException e) {
            log.error("invalid config given for migration", e);
            migrationForm.recordError(e.getMessage());
        } catch (CommitException e) {
            log.error("could not update migration in db", e);
            migrationForm.recordError(e.getMessage());
        }
    }

    @OnEvent(EventConstants.SUCCESS)
    void onSuccess() {

        number = collection.getNumber().getNumber();
        collectionID = collection.getId();
        migrationMessage = messages.format("success.msg.collection.migration", collection.getTitle());
    }
}
