package unidue.rc.ui.pages.print;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;
import unidue.rc.dao.BaseDAO;
import unidue.rc.model.*;
import unidue.rc.model.solr.SolrScanJobView;
import unidue.rc.search.SolrService;
import unidue.rc.system.BaseURLService;
import unidue.rc.system.TemplateService;
import unidue.rc.workflow.ScanJobService;

import java.io.IOException;
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
    private Logger log;

    @Inject
    private ComponentResources resources;

    @Inject
    private ScanJobService scanJobService;

    @Inject
    private TemplateService templateService;

    @Inject
    private BaseURLService urlService;

    @Inject
    private SolrService searchService;

    @Inject
    @Service(BaseDAO.SERVICE_NAME)
    private BaseDAO baseDAO;

    @Property
    private List<SolrScanJobView> scanJobs;

    @Property
    private SolrScanJobView scanJobView;

    @OnEvent(EventConstants.ACTIVATE)
    void onActivate(List<String> scanJobIDs) {
        if (scanJobIDs == null)
            return;
        log.info("loading scanjobs");
        this.scanJobs = scanJobIDs.stream()
                .map(this::getScanJob)
                .collect(Collectors.toList());
    }

    @OnEvent(EventConstants.PASSIVATE)
    Object onPassivate() {
        return scanJobs.stream()
                .map(j -> j.getJobID())
                .toArray(Integer[]::new);
    }

    private SolrScanJobView getScanJob(String id) {
        try {
            return searchService.getById(SolrScanJobView.class, id);
        } catch (SolrServerException e) {
            log.error("could not get scan job from solr " + id, e);
        }
        return null;
    }

    public String getScanJobPrintTemplate() {

        TemplateService.Builder builder = templateService.builder();
        ReserveCollection collection = baseDAO.get(ReserveCollection.class, scanJobView.getReserveCollectionID());
        Entry entry = baseDAO.get(Entry.class, scanJobView.getEntryID());

        // collection.vm
        builder.put("collection", collection)
                .put("collectionLink", urlService.getViewCollectionURL(collection))
                .put("authors", templateService.buildAuthors(collection, AUTHOR_DIVIDER))
                .put("origin", templateService.buildOrigin(collection, ORIGIN_DIVIDER));

        // entry.vm
        builder.put("entry", entry)
                .put("entryLink", urlService.getEntryLink(entry));

        String template;
        switch (scanJobView.getScannableType()) {
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
            log.error("could not build template " + template, e);
        }
        return null;
    }

    public String getBarcode() {
        ScanJob scanJob = baseDAO.get(ScanJob.class, scanJobView.getJobID());
        String barcode = scanJobService.getUploadBarcodeContent(scanJob.getScannable());
        return barcode;
    }
}
