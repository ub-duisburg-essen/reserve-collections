package unidue.rc.workflow;

import org.apache.cayenne.di.Inject;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import unidue.rc.system.BaseCronJob;

/**
 * Created by nils on 12.09.16.
 */
public class AutoDeactivationCronJobImpl extends BaseCronJob implements AutoDeactivationCronJob {

    @Inject
    private CollectionService collectionService;

    @Override
    protected void run(JobExecutionContext context) throws JobExecutionException {
        collectionService.deactivateExpired();
    }
}
