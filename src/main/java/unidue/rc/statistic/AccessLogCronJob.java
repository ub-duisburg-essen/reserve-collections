package unidue.rc.statistic;


import org.quartz.Job;

/**
 * Created by nils on 15.09.15.
 */
public interface AccessLogCronJob extends Job {

    String ACCESS_LOGABLE_PARAM = "logables";
}
