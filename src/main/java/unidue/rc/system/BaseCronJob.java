package unidue.rc.system;


import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Created by nils on 13.07.15.
 */
public abstract class BaseCronJob implements Job {

    @Override
    public final void execute(JobExecutionContext context) throws JobExecutionException {

        CayenneService contextBuilder = (CayenneService) context.getMergedJobDataMap().get(CayenneService.CONTEXT_BUILDER);
        if (contextBuilder == null)
            throw new JobExecutionException("no database context available");

        contextBuilder.createContext();

        run(context);
    }

    protected abstract void run(JobExecutionContext context) throws JobExecutionException;
}
