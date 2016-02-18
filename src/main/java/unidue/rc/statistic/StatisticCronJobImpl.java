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

import org.apache.cayenne.di.Inject;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unidue.rc.dao.StatisticDAO;
import unidue.rc.model.ReserveCollectionStatus;
import unidue.rc.model.Setting;
import unidue.rc.model.stats.SemAppStatistic;
import unidue.rc.model.stats.SemAppStatistics;
import unidue.rc.system.BaseCronJob;
import unidue.rc.system.SystemConfigurationService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by marcus.koesters on 10.08.15.
 */
public class StatisticCronJobImpl extends BaseCronJob implements StatisticCronJob {

    private static final Logger LOG = LoggerFactory.getLogger(StatisticCronJobImpl.class);

    @Inject
    private SystemConfigurationService config;

    @Inject
    private StatisticDAO statisticDAO;

    @Override
    protected void run(JobExecutionContext jobContext) throws JobExecutionException {
        LOG.debug("Running StatisticCronJob");

        Calendar cal = new GregorianCalendar();
        Setting importXMLuntil = config.getSetting("stats.import.xmlstats.until");
        SimpleDateFormat format = new SimpleDateFormat();
        String pattern = importXMLuntil.getFormat();
        format.applyPattern(pattern);
        java.util.Date untilDate;
        try {
            untilDate = format.parse(importXMLuntil.getValue());
            SemAppStatistics statistics;
            if (cal.getTime().compareTo(untilDate) < 0) {
                LOG.debug("Actual Date (" + cal.getTime() + ") is before Changeover-Date (" + untilDate + ") using " +
                        "XML source");
                statistics = statisticDAO.getXMLStats(config.getString("stats.xml.source"));
            } else {
                LOG.debug("Actual Date (" + cal.getTime() + ") is after Changeover-Date (" + untilDate + ") using " +
                        "database");
                statistics = new SemAppStatistics();
                SemAppStatistic newDailyStat = new SemAppStatistic();
                newDailyStat.setHeadline(statisticDAO.getHeadlineCount());
                newDailyStat.setArticle(statisticDAO.getArticleCount());
                newDailyStat.setBook(statisticDAO.getBookCount());
                newDailyStat.setChapter(statisticDAO.getChapterCount());
                newDailyStat.setEntries(statisticDAO.getEntryCount());
                newDailyStat.setFile(statisticDAO.getFileEntryCount());
                newDailyStat.setFiles(statisticDAO.getTotalFileCount());
                newDailyStat.setHtml(statisticDAO.getHTMLCount());
                newDailyStat.setWebLink(statisticDAO.getWeblinkCount());
                newDailyStat.setFreeText(0);
                newDailyStat.setMilessLink(0);

                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH) + 1;
                int day = cal.get(Calendar.DAY_OF_MONTH);

                newDailyStat.setDate(year + "-" + month + "-" + day);
                newDailyStat.setNum(statisticDAO.getReserveCollectionCount(ReserveCollectionStatus.ACTIVE.getValue()));
                List<SemAppStatistic> stats = new ArrayList<>();
                stats.add(newDailyStat);
                statistics.setSemAppStatistics(stats);

            }
            statisticDAO.addStatistics(statistics);

        } catch (ParseException e) {
            LOG.error("Unable to parse Changeover-Date. Aborting...  " + e);

        }

        LOG.debug("...StatisticCronJob finished");
    }
}
