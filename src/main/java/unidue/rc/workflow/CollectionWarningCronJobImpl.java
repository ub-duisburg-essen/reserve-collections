package unidue.rc.workflow;

/*
 * #%L
 * Semesterapparate
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 - 2016 Universitaet Duisburg Essen
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
