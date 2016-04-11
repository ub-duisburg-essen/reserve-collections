/**
 * Copyright (C) 2014 - 2016 Universitaet Duisburg-Essen (semapp|uni-due.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package unidue.rc.ui.pages.stats;


import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;
import se.unbound.tapestry.breadcrumbs.BreadCrumb;
import se.unbound.tapestry.breadcrumbs.BreadCrumbList;
import unidue.rc.dao.ReserveCollectionDAO;
import unidue.rc.dao.StatisticDAO;
import unidue.rc.model.ActionDefinition;
import unidue.rc.model.Statistic;
import unidue.rc.model.stats.GenericStat;
import unidue.rc.model.stats.StatisticDataSource;
import unidue.rc.model.stats.StatsList;
import unidue.rc.security.RequiresActionPermission;
import unidue.rc.system.SystemConfigurationService;
import unidue.rc.ui.ProtectedPage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

/**
 * @author Nils Verheyen
 * @since 16.08.13 14:56
 */
@BreadCrumb(titleKey = "title-label")
@ProtectedPage
public class GeneralStats {

    @Inject
    SystemConfigurationService systemConfigurationService;

    @Inject
    private Logger log;

    @Inject
    private ReserveCollectionDAO collectionDAO;

    @Inject
    private Messages messages;

    @SessionState
    private BreadCrumbList breadCrumbList;

    @Inject
    private Locale locale;

    @Property
    private List<StatisticDataSource> dataSources;

    @Inject
    private StatisticDAO statDAO;

    @SetupRender
    @RequiresActionPermission(ActionDefinition.READ_COLLECTION_STATISTICS)
    public void beginRender() {

        dataSources = new ArrayList<StatisticDataSource>();
        breadCrumbList.getLastCrumb().setTitle(messages.get("generalstatistic"));
        getEntryCounts(systemConfigurationService.getInt("stats.maxdays"), dataSources);

    }

    public void getEntryCounts(int range, List<StatisticDataSource> dataSources) {

        SimpleDateFormat format = new SimpleDateFormat();
        String pattern = "yyyy-MM-dd";
        format.applyPattern(pattern);

        StatsList entryStatList = new StatsList();
        entryStatList.setLegend(messages.get("generalstatistic.entries"));
        entryStatList.setIsVisible(false);
        StatsList rcStatList = new StatsList();
        rcStatList.setLegend(messages.get("generalstatistic.collection"));
        StatsList fileStatList = new StatsList();
        fileStatList.setLegend(messages.get("generalstatistic.fileentries"));
        fileStatList.setIsVisible(false);
        StatsList fileTotalStatList = new StatsList();
        fileTotalStatList.setLegend(messages.get("generalstatistic.files"));
        fileTotalStatList.setIsVisible(false);
        StatsList bookStatList = new StatsList();
        bookStatList.setLegend(messages.get("generalstatistic.books"));
        bookStatList.setIsVisible(false);
        StatsList chapterStatList = new StatsList();
        chapterStatList.setLegend(messages.get("generalstatistic.chapter"));
        chapterStatList.setIsVisible(false);
        StatsList htmlStatList = new StatsList();
        htmlStatList.setLegend(messages.get("generalstatistic.html"));
        htmlStatList.setIsVisible(false);
        StatsList headlineStatList = new StatsList();
        headlineStatList.setLegend(messages.get("generalstatistic.headlines"));
        headlineStatList.setIsVisible(false);
        StatsList journalStatList = new StatsList();
        journalStatList.setLegend(messages.get("generalstatistic.journal"));
        journalStatList.setIsVisible(false);
        StatsList weblinkStatList = new StatsList();
        weblinkStatList.setLegend(messages.get("generalstatistic.weblinks"));
        weblinkStatList.setIsVisible(false);

        List<Statistic> stats = null;
        if(range > 0) {
            Calendar cal = new GregorianCalendar();
            String to = format.format(cal.getTime());
            cal.add(Calendar.DAY_OF_YEAR, -range);
            String from = format.format(cal.getTime());
            log.debug("selecting stats from " + from + " to " + to);
             stats = statDAO.getGeneralStatisticForRange(from, to);

        } else {
            stats = statDAO.getGeneralStatistic();
        }

        log.debug(stats.size() + "Stats found");
        for (Statistic stat : stats) {
            GenericStat entryStat = new GenericStat();
            entryStat.setCount(stat.getEntryCount());
            entryStat.setDate(stat.getDate());
            entryStatList.addStatistic(entryStat);

            GenericStat rcStat = new GenericStat();
            rcStat.setCount(stat.getReservecollectionCount());
            rcStat.setDate(stat.getDate());
            rcStatList.addStatistic(rcStat);

            GenericStat fileStat = new GenericStat();
            fileStat.setCount(stat.getFileCount());
            fileStat.setDate(stat.getDate());
            fileStatList.addStatistic(fileStat);

            GenericStat fileTotalStat = new GenericStat();
            fileTotalStat.setCount(stat.getTotalFilesCount());
            fileTotalStat.setDate(stat.getDate());
            fileTotalStatList.addStatistic(fileTotalStat);

            GenericStat bookStat = new GenericStat();
            bookStat.setCount(stat.getBookCount());
            bookStat.setDate(stat.getDate());
            bookStatList.addStatistic(bookStat);

            GenericStat chapterStat = new GenericStat();
            chapterStat.setCount(stat.getChapterCount());
            chapterStat.setDate(stat.getDate());
            chapterStatList.addStatistic(chapterStat);

            GenericStat htmlStat = new GenericStat();
            htmlStat.setCount(stat.getHtmlCount());
            htmlStat.setDate(stat.getDate());
            htmlStatList.addStatistic(htmlStat);

            GenericStat headlineStat = new GenericStat();
            headlineStat.setCount(stat.getHeadlineCount());
            headlineStat.setDate(stat.getDate());
            headlineStatList.addStatistic(headlineStat);

            GenericStat journalStat = new GenericStat();
            journalStat.setCount(stat.getArticleCount());
            journalStat.setDate(stat.getDate());
            journalStatList.addStatistic(journalStat);

            GenericStat weblinkStat = new GenericStat();
            weblinkStat.setCount(stat.getWeblinkCount());
            weblinkStat.setDate(stat.getDate());
            weblinkStatList.addStatistic(weblinkStat);
        }

        dataSources.add(entryStatList);
        dataSources.add(fileStatList);
        dataSources.add(rcStatList);
        dataSources.add(bookStatList);
        dataSources.add(chapterStatList);
        dataSources.add(htmlStatList);
        dataSources.add(headlineStatList);
        dataSources.add(journalStatList);
        dataSources.add(fileTotalStatList);
        dataSources.add(weblinkStatList);

    }
}
