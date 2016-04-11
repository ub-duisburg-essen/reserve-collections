package unidue.rc.system;


import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nils Verheyen
 * @since 12.12.14 09:10
 */
public class QuartzServiceImpl implements QuartzService {

    private static final Logger LOG = LoggerFactory.getLogger(QuartzServiceImpl.class);

    private Scheduler scheduler;
    private JobFactory jobFactory;

    public void add(JobDetail job, Trigger trigger) throws SchedulerException {
        if (scheduler == null)
            start();

        scheduler.scheduleJob(job, trigger);
    }

    @Override
    public void setJobFactory(JobFactory jobFactory) throws SchedulerException {
        this.jobFactory = jobFactory;
    }

    public void start() throws SchedulerException {

        if (jobFactory == null)
            throw new SchedulerConfigException("job factory not set, default not permitted");

        SchedulerFactory sf = new StdSchedulerFactory();
        scheduler = sf.getScheduler();
        scheduler.setJobFactory(jobFactory);
        scheduler.start();
        LOG.info("quartz scheduler " + scheduler.getSchedulerName() + " started");
    }

    public void stop() throws SchedulerException {
        scheduler.shutdown(true);
        LOG.info("quartz scheduler " + scheduler.getSchedulerName() + " shut down");
    }
}
