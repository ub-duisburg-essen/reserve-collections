package unidue.rc.ui.pages.migrate;


import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.Block;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;
import org.slf4j.Logger;
import se.unbound.tapestry.breadcrumbs.BreadCrumb;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.MigrationDAO;
import unidue.rc.dao.ReserveCollectionDAO;
import unidue.rc.migration.MigrationException;
import unidue.rc.migration.MigrationService;
import unidue.rc.model.Migration;
import unidue.rc.model.ReserveCollection;
import unidue.rc.ui.ProtectedPage;
import unidue.rc.ui.pages.collection.ViewCollection;

/**
 * Created by nils on 17.07.15.
 */
@BreadCrumb(titleKey = "migrate-title")
@ProtectedPage
public class Index {

    @Inject
    private Logger log;

    @Inject
    @Service(MigrationDAO.SERVICE_NAME)
    private MigrationDAO migrationDAO;

    @Inject
    private MigrationService migrationService;

    @Inject
    private Messages messages;

    @Inject
    private Request request;

    @Inject
    private PageRenderLinkSource linkSource;

    @Inject
    @Property
    private Block collectionLink;

    @Component(id = "migrationForm")
    private Form migrationForm;

    @Property
    private ReserveCollection collection;

    @Property
    private String infoMessage;

    @Property
    private String errorMessage;

    @Property
    @Validate("required")
    private Integer documentID;

    @Property
    @Validate("required")
    private String migrationCode;

    @Property
    private Migration migration;

    @OnEvent(EventConstants.ACTIVATE)
    public void onActivate(Integer documentID, String migrationCode) {

        this.documentID = documentID;
        this.migrationCode = migrationCode;

        if (this.documentID != null) {
            this.migration = migrationDAO.getMigrationByDocID(documentID.toString());
            if (migration != null && migration.isFinished()) {
                collection = migration.getReserveCollection();
                infoMessage = messages.format("info.msg.collection.migrated", collection.getTitle(), migration.getDocumentID());
            }
        }
    }

    @OnEvent(EventConstants.PASSIVATE)
    public Object[] onPassivate() {
        return new Object[]{this.documentID, this.migrationCode};
    }

    @OnEvent(EventConstants.SUCCESS)
    Link onMigrationFinished() {

        Migration migration = migrationDAO.getMigrationByDocID(documentID.toString());
        if (migration == null) {
            migrationForm.recordError(messages.get("error.msg.invalid.doc.for.migration"));
            return null;
        }
        if (migration.isFinished() || migration.getCodeUsedDate() != null) {
            migrationForm.recordError(messages.get("error.msg.migration.already.finished"));
            return null;
        }
        if (!migration.getMigrationCode().equals(migrationCode)) {
            migrationForm.recordError(messages.get("error.msg.invalid.migration.code"));
            return null;
        }

        try {
            collection = migrate(migration, migrationCode);
        } catch (MigrationException e) {
            log.error("could not migrate document " + documentID + " with code " + migrationCode, e);
            migrationForm.recordError(messages.get("error.msg.collection.migration"));
            return null;
        }
        return linkSource.createPageRenderLinkWithContext(ViewCollection.class, collection.getId());
    }

    private ReserveCollection migrate(Migration migration, String migrationCode) throws MigrationException {
        try {
            return migrationService.migrateReserveCollection(migration, migrationCode);
        } catch (ConfigurationException e) {
            log.error("invalid configuration given for migration", e);
            throw new MigrationException(e);
        }
    }

    public boolean isFinished() {
        return migration != null && migration.isFinished();
    }
}
