package unidue.rc.workflow;


import org.apache.cayenne.di.Inject;
import org.apache.solr.client.solrj.SolrServerException;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unidue.rc.dao.BaseDAO;
import unidue.rc.dao.ScanJobDAO;
import unidue.rc.model.*;
import unidue.rc.search.SolrService;
import unidue.rc.system.BaseCronJob;

import java.io.IOException;
import java.util.List;

/**
 * Created by nils on 15.07.15.
 */
public class ScanJobSyncCronJobImpl extends BaseCronJob implements ScanJobSyncCronJob {

    private static final Logger LOG = LoggerFactory.getLogger(ScanJobSyncCronJobImpl.class);

    @Inject
    private ScanJobService scanJobService;

    @Inject
    private SolrService searchService;

    @Inject
    private BaseDAO baseDAO;

    @Override
    protected void run(JobExecutionContext context) throws JobExecutionException {

        LOG.info("synchronizing scan jobs");

        sync(JournalArticle.class, 0, BaseDAO.MAX_RESULTS);
        sync(BookChapter.class, 0, BaseDAO.MAX_RESULTS);
        try {
            searchService.fullImport(SolrService.Core.ScanJob);
        } catch (SolrServerException |IOException e) {
            throw new JobExecutionException("could not run full import on core " + SolrService.Core.ScanJob.name(), e);
        }
        LOG.info("...synchronization finished");
    }

    private <T extends Scannable> void sync(Class<T> scannableClass, int offset, int maxResults) {

        List<T> scannables = baseDAO.get(scannableClass, offset, maxResults);

        scannables.forEach(scanJobService::checkScanJob);

        if (scannables.size() >= maxResults)
            sync(scannableClass, offset + maxResults, maxResults);
    }
}
