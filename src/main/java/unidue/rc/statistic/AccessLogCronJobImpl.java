package unidue.rc.statistic;

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

import org.apache.cayenne.BaseContext;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.di.Inject;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unidue.rc.dao.BaseDAO;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.StatisticDAO;
import unidue.rc.model.AccessLog;
import unidue.rc.model.accesslog.Access;
import unidue.rc.system.BaseCronJob;

import java.util.Date;
import java.util.List;

/**
 * Created by nils on 15.09.15.
 */
public class AccessLogCronJobImpl extends BaseCronJob implements AccessLogCronJob {

    private static final Logger LOG = LoggerFactory.getLogger(AccessLogCronJobImpl.class);

    @Inject
    private StatisticDAO statisticDAO;

    private ObjectContext context;
    private Date importDate;
    private List<AccessLogable> logables;

    @Override
    protected void run(JobExecutionContext context) throws JobExecutionException {

        LOG.info("running access log import");

        this.context = BaseContext.getThreadObjectContext();

        importAccessLogs(context);

        LOG.info("...access log import finished");
    }

    private void importAccessLogs(JobExecutionContext jobContext) {

        Date lastAccessLogDate = statisticDAO.getLastAccessLogDate();
        this.importDate = lastAccessLogDate != null
                ? lastAccessLogDate
                : new Date(0);

        this.logables = (List<AccessLogable>) jobContext.getMergedJobDataMap().get(ACCESS_LOGABLE_PARAM);

        importAccessLogs(0, BaseDAO.MAX_RESULTS);
    }

    private void importAccessLogs(int offset, int maxResults) {

        List<Access> access = statisticDAO.getAccess(importDate, offset, maxResults);
        for (Access a : access) {
            for (AccessLogable accessLogable : logables) {
                if (accessLogable.matches(a)) {

                    AccessLog log = accessLogable.createAccessLog(a);
                    try {
                        statisticDAO.create(log);
                    } catch (CommitException e) {
                        LOG.error("could not create log object " + log, e);
                    }
                }
            }
        }

        if (access.size() >= maxResults)
            importAccessLogs(offset + maxResults, maxResults);
    }
}
