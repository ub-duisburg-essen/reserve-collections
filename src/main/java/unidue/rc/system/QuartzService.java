package unidue.rc.system;


import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.spi.JobFactory;

/**
 * @author Nils Verheyen
 * @since 12.12.14 09:10
 */
public interface QuartzService {

    void setJobFactory(JobFactory jobFactory) throws SchedulerException;

    void start() throws SchedulerException;

    void stop() throws SchedulerException;

    void add(JobDetail job, Trigger trigger) throws SchedulerException;
}
