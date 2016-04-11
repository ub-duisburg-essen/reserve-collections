package unidue.rc.ui.components;


import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;
import unidue.rc.dao.OriginDAO;
import unidue.rc.dao.UserDAO;
import unidue.rc.model.JournalArticle;
import unidue.rc.model.ScanJob;
import unidue.rc.workflow.CollectionService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * @author Nils Verheyen
 * @since 17.12.13 10:11
 */
public class ScanArticleDefinitionList {

    private static final DateFormat MODIFICATION_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy hh:mm");

    @Inject
    private Logger log;

    @Parameter(required = true, allowNull = false)
    @Property
    private JournalArticle journalArticle;

    @Parameter(required = true, allowNull = false)
    @Property
    private ScanJob scanJob;

    @Property
    private String docent;

    @Parameter(required = true, allowNull = false)
    @Property
    private String barcodeContent;

    @Inject
    private UserDAO userDAO;

    @Inject
    private OriginDAO originDAO;

    @Inject
    private CollectionService collectionService;

    @Inject
    private Messages messages;

    @Inject
    private Locale locale;

    public List<String> getDocents() {
        return collectionService.getDocents(journalArticle.getReserveCollection());
    }

    public String getOriginLabel() {
        return originDAO.getOriginLabel(locale, journalArticle.getReserveCollection().getOriginId());
    }

    public String getJobModificationDate() {
        return MODIFICATION_DATE_FORMAT.format(scanJob.getModified());
    }

    public String getLocalizedStatus() {
        return messages.get(scanJob.getStatus().name());
    }
}
