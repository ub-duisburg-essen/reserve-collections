package unidue.rc.system;

/*
 * #%L
 * Semesterapparate
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 - 2015 Universitaet Duisburg Essen
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
