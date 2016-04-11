package unidue.rc.workflow;


import org.apache.cayenne.di.Inject;
import org.apache.commons.configuration.ConfigurationException;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import unidue.rc.system.BaseCronJob;
import unidue.rc.system.SystemConfigurationService;

import java.time.LocalDate;

/**
 * Created by nils on 16.03.16.
 */
public class CollectionWarningCronJobImpl extends BaseCronJob implements CollectionWarningCronJob {

    @Inject
    private CollectionWarningService warningService;

    @Inject
    private SystemConfigurationService config;

    private LocalDate baseDate;

    @Override
    protected void run(JobExecutionContext jobContext) throws JobExecutionException {

        boolean sendWarnings = config.getBoolean("send.expiration.warnings", true);
        if (sendWarnings) {
            baseDate = LocalDate.now();
            try {
                warningService.sendWarnings(baseDate);
            } catch (ConfigurationException e) {
                throw new JobExecutionException(e);
            }
        }
    }
}
