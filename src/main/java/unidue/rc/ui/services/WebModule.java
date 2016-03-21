package unidue.rc.ui.services;

/*
 * #%L
 * Semesterapparate
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 Universitaet Duisburg Essen
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.apache.commons.configuration.ConfigurationException;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.InjectService;
import org.apache.tapestry5.ioc.annotations.Startup;
import org.apache.tapestry5.ioc.services.RegistryShutdownHub;
import org.apache.tapestry5.upload.services.UploadSymbols;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.ScheduleBuilder;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unidue.rc.dao.DatabaseException;
import unidue.rc.migration.MigrationCodeCronJob;
import unidue.rc.statistic.*;
import unidue.rc.system.CayenneService;
import unidue.rc.system.CayenneServiceImpl;
import unidue.rc.system.MailCronJob;
import unidue.rc.system.QuartzService;
import unidue.rc.system.SystemConfigurationService;
import unidue.rc.workflow.CollectionWarningCronJob;
import unidue.rc.workflow.ScanJobSyncCronJob;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This module is automatically included as part of the Tapestry IoC Registry if <em>tapestry.execution-mode</em>
 * includes <code>development</code>.
 */
public class WebModule {

    private static final Logger LOG = LoggerFactory.getLogger(WebModule.class);
    private static final long ONE_YEAR_IN_MILLIS = 1000 * 60 * 60 * 24 * 365;

    public static void bind(ServiceBinder binder) {

        /*
         * Make bind() calls on the binder object to define most IoC services.
         * Use service builder methods (example below) when the implementation
         * is provided inline, or requires more initialization than simply
         * invoking the constructor.
         */
        binder.bind(CayenneService.class, CayenneServiceImpl.class);
    }

    public static void contributeApplicationDefaults(
            final Logger log,
            MappedConfiguration<String, Object> configuration,
            final @InjectService("CayenneService") CayenneService cayenneService,
            final @InjectService("SystemConfigurationService") SystemConfigurationService sysConfig) {

        log.info("Starting up web module...");
        try {
            cayenneService.init();
        } catch (DatabaseException e) {
            log.error("could not initialize cayenne: ", e);
        } catch (ConfigurationException e) {
            log.error("could not load default config: ", e);
        }

        // The application version number is incorprated into URLs for some
        // assets. Web browsers will cache assets because of the far future expires
        // header. If existing assets are changed, the version number should also
        // change, to force the browser to download new versions.
        configuration.add(SymbolConstants.APPLICATION_VERSION, "1.0-SNAPSHOT-DEV");
        configuration.add(SymbolConstants.MINIFICATION_ENABLED, sysConfig.getString(SymbolConstants.MINIFICATION_ENABLED));

        long maxAge = sysConfig.getLong(SymbolConstants.COOKIE_MAX_AGE, ONE_YEAR_IN_MILLIS);
        configuration.add(SymbolConstants.COOKIE_MAX_AGE, maxAge);

        configuration.add(UploadSymbols.REPOSITORY_LOCATION, sysConfig.getString(UploadSymbols.REPOSITORY_LOCATION));
        if (sysConfig.contains(UploadSymbols.REPOSITORY_THRESHOLD))
            configuration.add(UploadSymbols.REPOSITORY_THRESHOLD, sysConfig.getString(UploadSymbols
                    .REPOSITORY_THRESHOLD));
        if (sysConfig.contains(UploadSymbols.FILESIZE_MAX))
            configuration.add(UploadSymbols.FILESIZE_MAX, sysConfig.getString(UploadSymbols.FILESIZE_MAX));
        if (sysConfig.contains(UploadSymbols.REQUESTSIZE_MAX))
            configuration.add(UploadSymbols.REQUESTSIZE_MAX, sysConfig.getString(UploadSymbols.REQUESTSIZE_MAX));
    }

    /**
     * This method is executed on startup of Tapestries IoC Registry. RegistryStartup occurs after eager loaded services
     * are instantiated.
     *
     * @param log            web logger
     * @param cayenneService cayennes startup service, which is injected through {@link org.apache.tapestry5.ioc
     *                       .annotations.InjectService}
     * @param quartzService  application scheduler
     * @param shutdownHub    contains the shutdown hub that is used to bind shutdown listeners to
     * @throws SchedulerException thrown if the scheduler could not be started
     */
    @Startup
    public static void onApplicationStart(final Logger log,
                                          final @InjectService("CayenneService") CayenneService cayenneService,
                                          @InjectService("QuartzService") final QuartzService quartzService,
                                          RegistryShutdownHub shutdownHub) throws SchedulerException {

        quartzService.setJobFactory(cayenneService);
        quartzService.start();

        shutdownHub.addRegistryWillShutdownListener(() -> {
            cayenneService.shutdown();
            try {
                quartzService.stop();
            } catch (SchedulerException e) {
                log.error("could not shutdown quartz", e);
            }
        });

        HashMap<String, Object> jobMapData = new HashMap<>();
        jobMapData.put(CayenneService.CONTEXT_BUILDER, cayenneService);

        /* CRON JOBS */

        // send unsend mails
        quartzService.add(createCronJob(MailCronJob.class, jobMapData),
                createCronJobTrigger(SimpleScheduleBuilder.simpleSchedule()
                        .repeatForever()
                        .withIntervalInMinutes(20)));

        // create migration codes
        quartzService.add(createCronJob(MigrationCodeCronJob.class, jobMapData),
                createCronJobTrigger(CronScheduleBuilder
                        .dailyAtHourAndMinute(1, 30)));

        // create scan job sync
        quartzService.add(createCronJob(ScanJobSyncCronJob.class, jobMapData),
                createCronJobTrigger(CronScheduleBuilder.dailyAtHourAndMinute(2, 30)));

        // create generate statistics
        quartzService.add(createCronJob(StatisticCronJob.class, jobMapData),
                createCronJobTrigger(CronScheduleBuilder.dailyAtHourAndMinute(1, 0)));

        // create access log statistics
        List<AccessLogable> accessLogables = Arrays.<AccessLogable>asList(
                new ViewCollectionLog(),
                new FileDownloadLog());
        HashMap<String, Object> statsJobContext = new HashMap<>();
        statsJobContext.put(CayenneService.CONTEXT_BUILDER, cayenneService);
        statsJobContext.put(AccessLogCronJob.ACCESS_LOGABLE_PARAM, accessLogables);
        quartzService.add(createCronJob(AccessLogCronJob.class, statsJobContext),
                createCronJobTrigger(SimpleScheduleBuilder.simpleSchedule()
                        .repeatForever()
                        .withIntervalInMinutes(30)));

        // send warning mails
        quartzService.add(createCronJob(CollectionWarningCronJob.class, jobMapData),
                createCronJobTrigger(CronScheduleBuilder.dailyAtHourAndMinute(10, 0)));
    }

    private static JobDetail createCronJob(Class clazz, Map<String, Object> mapData) {
        return JobBuilder.newJob(clazz)
                .usingJobData(new JobDataMap(mapData))
                .build();
    }

    private static Trigger createCronJobTrigger(ScheduleBuilder scheduleBuilder) {
        return TriggerBuilder.newTrigger()
                .startNow()
                .withSchedule(scheduleBuilder)
                .build();
    }

}
