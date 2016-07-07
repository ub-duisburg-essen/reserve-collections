package unidue.rc.ui.pages.print;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;
import unidue.rc.dao.BaseDAO;
import unidue.rc.model.BookChapter;
import unidue.rc.model.Entry;
import unidue.rc.model.JournalArticle;
import unidue.rc.model.ReserveCollection;
import unidue.rc.model.solr.SolrScanJobView;
import unidue.rc.search.SolrService;
import unidue.rc.system.BaseURLService;
import unidue.rc.system.TemplateService;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by nils on 27.06.16.
 */
@Import(stylesheet = {
        "context:css/dina5.css"
})
public class ScanJobs {

    public static final String PARAM_SCANJOB_IDS = "scanJobIDs";
    private static final String PARAM_SCANJOB_DIVIDER = ",";
    private static final String PARAM_SCANJOB_FORMAT = "[0-9]+(" + PARAM_SCANJOB_DIVIDER + "[0-9]+)*";
    private static final String ORIGIN_DIVIDER = " &raquo; ";
    private static final String AUTHOR_DIVIDER = ", ";

    @Inject
    private Logger logger;

    @Inject
    private TemplateService templateService;

    @Inject
    private BaseURLService urlService;

    @Inject
    private SolrService searchService;

    @Inject
    @Service(BaseDAO.SERVICE_NAME)
    private BaseDAO baseDAO;

    @Persist(PersistenceConstants.FLASH)
    private List<SolrScanJobView> scanjobs;

    @Property
    private SolrScanJobView scanjob;

    @OnEvent(EventConstants.ACTIVATE)
    void onActivate(@RequestParameter(PARAM_SCANJOB_IDS) String scanJobIDsParam) {
        if (!scanJobIDsParam.matches(PARAM_SCANJOB_FORMAT))
            return;

        String[] scanJobIDs = scanJobIDsParam.split(PARAM_SCANJOB_DIVIDER);
        scanjobs = Arrays.stream(scanJobIDs)
                .map(this::getScanjob)
                .filter(job -> job != null)
                .collect(Collectors.toList());
    }

    private SolrScanJobView getScanjob(String id) {
        try {
            return searchService.getById(SolrScanJobView.class, id);
        } catch (SolrServerException e) {
            logger.error("could not get scan job from solr " + id, e);
        }
        return null;
    }

    public String getScanjobPrintTemplate() {

        TemplateService.Builder builder = templateService.builder();
        ReserveCollection collection = baseDAO.get(ReserveCollection.class, scanjob.getReserveCollectionID());
        Entry entry = baseDAO.get(Entry.class, scanjob.getEntryID());

        // collection.vm
        builder.put("collection", collection)
                .put("collectionLink", urlService.getViewCollectionURL(collection))
                .put("authors", templateService.buildAuthors(collection, AUTHOR_DIVIDER))
                .put("origin", templateService.buildOrigin(collection, ORIGIN_DIVIDER));

        // entry.vm
        builder.put("entry", entry)
                .put("entryLink", urlService.getEntryLink(entry));

        String template;
        switch (scanjob.getScannableType()) {
            // journal.article.vm
            case "JournalArticle":
                template = "/vt/print.journal.article.vm";
                builder.put("article", baseDAO.get(JournalArticle.class, entry.getId()));
                break;
            // book.chapter.vm
            case "BookChapter":
                template = "/vt/print.book.chapter.vm";
                builder.put("chapter", baseDAO.get(BookChapter.class, entry.getId()));
                break;
            default:
                template = null;
                break;
        }

        try {
            if (template != null) {
                return builder.build(template);
            }
        } catch (IOException e) {
            logger.error("could not build template " + template, e);
        }

        return null;
    }

    public List<SolrScanJobView> getScanjobs() {
        return scanjobs;
    }

    public void setScanjobs(List<SolrScanJobView> scanjobs) {
        this.scanjobs = scanjobs;
    }
}
