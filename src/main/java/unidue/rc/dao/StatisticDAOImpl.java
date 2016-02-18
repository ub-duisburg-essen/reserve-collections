package unidue.rc.dao;

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
import org.apache.cayenne.DataRow;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.NamedQuery;
import org.apache.cayenne.query.Ordering;
import org.apache.cayenne.query.SelectQuery;
import org.apache.cayenne.query.SortOrder;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unidue.rc.model.AccessLog;
import unidue.rc.model.ReserveCollection;
import unidue.rc.model.ReserveCollectionsDatamap;
import unidue.rc.model.Resource;
import unidue.rc.model.Statistic;
import unidue.rc.model.accesslog.Access;
import unidue.rc.model.stats.SemAppStatistic;
import unidue.rc.model.stats.SemAppStatistics;

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by marcus.koesters on 05.08.15.
 */
public class StatisticDAOImpl extends BaseDAOImpl implements StatisticDAO {

    private static final Logger LOG = LoggerFactory.getLogger(StatisticDAOImpl.class);

    @Inject
    ReserveCollectionDAO reserveCollectionDAO;

    @Override
    public List<Statistic> getGeneralStatisticForRange(String from, String to) {

        String[] key = new String[2];
        key[0] = "from";
        key[1] = "to";
        String[] value = new String[2];
        value[0] = from;
        value[1] = to;


        NamedQuery query = new NamedQuery(ReserveCollectionsDatamap.SELECT_STATISTICS_FOR_RANGE_QUERYNAME, key, value);
        ObjectContext objectContext = BaseContext.getThreadObjectContext();
        List<Statistic> stats = objectContext.performQuery(query);
        return stats != null
                ? stats
                : Collections.EMPTY_LIST;
    }

    @Override
    public List<Statistic> getGeneralStatistic() {
        NamedQuery query = new NamedQuery(ReserveCollectionsDatamap.SELECT_WHOLE_STATISTICS_QUERYNAME);
        ObjectContext objectContext = BaseContext.getThreadObjectContext();
        List<Statistic> stats = objectContext.performQuery(query);
        return stats != null
                ? stats
                : Collections.EMPTY_LIST;
    }


    @Override
    public void addStatistics(SemAppStatistics statistics) {
        ObjectContext objectContext = BaseContext.getThreadObjectContext();

        for (SemAppStatistic semAppStatistic : statistics.getSemAppStatistics()) {

            String date = semAppStatistic.getDate();
            String[] dateParts = date.split(" ");

            if (!alreadyExistsInDB(dateParts[0])) {
                Statistic statistic = objectContext.newObject(Statistic.class);
                statistic.setArticleCount(semAppStatistic.getArticle());
                statistic.setBookCount(semAppStatistic.getBook());
                statistic.setChapterCount(semAppStatistic.getChapter());
                statistic.setEntryCount(semAppStatistic.getEntries());
                statistic.setChapterCount(semAppStatistic.getChapter());
                statistic.setFileCount(semAppStatistic.getFile());
                statistic.setHeadlineCount(semAppStatistic.getHeadline());
                statistic.setHtmlCount(semAppStatistic.getHtml() + semAppStatistic.getFreeText());
                statistic.setWeblinkCount(semAppStatistic.getWebLink() + semAppStatistic.getMilessLink());
                statistic.setReservecollectionCount(semAppStatistic.getNum());
                statistic.setTotalFilesCount(semAppStatistic.getFiles());
                statistic.setDate(dateParts[0]);
            }
        }

        // write changes to db
        objectContext.commitChanges();
    }

    @Override
    public List<Access> getAccess(Date after, int offset, int maxResults) {

        ObjectContext context = BaseContext.getThreadObjectContext();

        SelectQuery query = new SelectQuery(Access.class);
        query.setQualifier(ExpressionFactory.greaterOrEqualExp(Access.TIMESTAMP_PROPERTY, after));
        query.setFetchOffset(offset);
        query.setFetchLimit(maxResults);
        query.addOrdering(Access.TIMESTAMP_PROPERTY, SortOrder.ASCENDING);
        List<Access> access = context.performQuery(query);
        return access != null
                ? access
                : Collections.EMPTY_LIST;
    }

    @Override
    public List<AccessLog> getAccessLog(int id, long from, long to, String actiontype) {

        Integer[] resourceIDs;

        if (actiontype.equals(StatisticDAO.DOWNLOAD)) {
            ReserveCollection collection = reserveCollectionDAO.get(ReserveCollection.class, id);
            List<Resource> resources = collection.getResources();
            resourceIDs = resources.stream()
                    .map(resource -> resource.getId())
                    .collect(Collectors.toList())
                    .toArray(new Integer[resources.size()]);
        } else {
            resourceIDs = new Integer[1];
            resourceIDs[0] = id;
        }

        ObjectContext context = BaseContext.getThreadObjectContext();
        SelectQuery query = new SelectQuery(AccessLog.class);
        query.setQualifier(ExpressionFactory.likeExp(AccessLog.ACTION_PROPERTY, actiontype)
                .andExp(ExpressionFactory.inExp(AccessLog.RESOURCE_ID_PROPERTY, resourceIDs))
                .andExp(ExpressionFactory.greaterOrEqualDbExp(AccessLog.TIMESTAMP_PROPERTY, from))
                .andExp(ExpressionFactory.lessOrEqualDbExp(AccessLog.TIMESTAMP_PROPERTY, to))
        );
        query.addOrdering(new Ordering(AccessLog.RESOURCE_ID_PROPERTY, SortOrder.ASCENDING));
        query.addOrdering(new Ordering(AccessLog.REMOTE_HOST_PROPERTY, SortOrder.ASCENDING));
        query.addOrdering(new Ordering(AccessLog.TIMESTAMP_PROPERTY, SortOrder.ASCENDING));

        List<AccessLog> access = context.performQuery(query);

        return access;
    }

    @Override
    public Date getLastAccessLogDate() {
        ObjectContext context = BaseContext.getThreadObjectContext();

        SelectQuery query = new SelectQuery(AccessLog.class);
        query.addOrdering(AccessLog.TIMESTAMP_PROPERTY, SortOrder.DESCENDING);
        query.setFetchLimit(1);

        List<AccessLog> logs = context.performQuery(query);
        return logs != null && logs.size() == 1
                ? new Date(logs.get(0).getTimestamp())
                : null;
    }

    @Override
    public long getAccessLogCount(Class resourceClass, Integer resourceID, String action, Date start, Date end) {

        ObjectContext context = BaseContext.getThreadObjectContext();

        String[] keys = new String[]{"resource", "resourceID", "action", "start", "end"};
        Object[] values = new Object[]{resourceClass.getSimpleName(), resourceID, action, start.getTime(), end.getTime()};
        NamedQuery query = new NamedQuery(ReserveCollectionsDatamap.SELECT_ACCESS_LOG_COUNT_QUERYNAME, keys, values);

        List<Long> count = context.performQuery(query);
        return count.size() == 1
                ? count.get(0)
                : -1;
    }

    private boolean alreadyExistsInDB(String date) {
        String[] key = new String[1];
        key[0] = "date";

        String[] value = new String[1];
        value[0] = date;
        NamedQuery query = new NamedQuery(ReserveCollectionsDatamap.CHECK_IF_DAILY_STAT_ALREADY_EXISTS_QUERYNAME,
                key, value);
        ObjectContext objectContext = BaseContext.getThreadObjectContext();
        List<Statistic> stats = objectContext.performQuery(query);
        if (stats.isEmpty()) return false;
        return true;
    }

    @Override
    public SemAppStatistics getXMLStats(String path) {
        Serializer serializer = new Persister();
        SemAppStatistics semAppStatistics;

        try {
            semAppStatistics = serializer.read(SemAppStatistics.class, new File(path));
            return semAppStatistics;
        } catch (Exception e) {
            LOG.error("Could not read file " + path, e);
        }
        return new SemAppStatistics();

    }

    @Override
    public Integer getReserveCollectionCount(int status) {
        String[] key = new String[1];
        key[0] = "status";

        String[] value = new String[1];
        value[0] = String.valueOf(status);

        return getCount(ReserveCollectionsDatamap.COUNT_RESERVECOLLECTIONS_FOR_STATUS_QUERYNAME, key, value);
    }


    @Override
    public Integer getEntryCount() {
        return getCount(ReserveCollectionsDatamap.COUNT_ENTRIES_QUERYNAME);
    }

    @Override
    public Integer getBookCount() {
        return getCount(ReserveCollectionsDatamap.COUNT_BOOKS_QUERYNAME);

    }

    @Override
    public Integer getWeblinkCount() {
        return getCount(ReserveCollectionsDatamap.COUNT_WEBLINKS_QUERYNAME);
    }

    @Override
    public Integer getChapterCount() {
        return getCount(ReserveCollectionsDatamap.COUNT_CHAPTERS_QUERYNAME);
    }

    @Override
    public Integer getHTMLCount() {
        return getCount(ReserveCollectionsDatamap.COUNT_HTML_QUERYNAME);
    }

    @Override
    public Integer getHeadlineCount() {
        return getCount(ReserveCollectionsDatamap.COUNT_HEADLINES_QUERYNAME);
    }

    @Override
    public Integer getArticleCount() {
        return getCount(ReserveCollectionsDatamap.COUNT_JOURNALS_QUERYNAME);
    }

    @Override
    public Integer getTotalFileCount() {
        return getCount(ReserveCollectionsDatamap.COUNT_TOTALFILES_QUERYNAME);
    }

    @Override
    public Integer getFileEntryCount() {
        return getCount(ReserveCollectionsDatamap.COUNT_FILEENTRIES_QUERYNAME);
    }


    private Integer getCount(String queryname, String[] keys, String[] values) {
        NamedQuery query = keys != null && values != null
                ? new NamedQuery(queryname, keys, values)
                : new NamedQuery(queryname);
        ObjectContext objectContext = BaseContext.getThreadObjectContext();
        List records = objectContext.performQuery(query);
        DataRow dr = (DataRow) records.get(0);
        return Integer.valueOf(dr.get("count").toString());
    }

    private Integer getCount(String queryname) {
        return getCount(queryname, null, null);
    }


}
