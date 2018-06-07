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
package unidue.rc.dao;


import unidue.rc.model.AccessLog;
import unidue.rc.model.Statistic;
import unidue.rc.model.accesslog.Access;
import unidue.rc.model.stats.SemAppStatistics;

import java.util.Date;
import java.util.List;


/**
 * A <code>StatisticDAO</code> should be used as default access object to retrieve or update statistics {@link SemAppStatistics}
 * objects from backend.
 *
 * @author Marcus Koesters
 */
public interface StatisticDAO extends BaseDAO {

    String SERVICE_NAME = "StatisticDAO";

    String VIEW = "view";
    String DOWNLOAD = "download";


    List<Statistic> getGeneralStatisticForRange(String from, String to);

    List<Statistic> getGeneralStatistic();

    Integer getReserveCollectionCount(int status);

    Integer getEntryCount();

    Integer getBookCount();

    Integer getChapterCount();

    Integer getHTMLCount();

    Integer getHeadlineCount();

    Integer getArticleCount();

    Integer getTotalFileCount();

    Integer getFileEntryCount();

    Integer getWeblinkCount();

    SemAppStatistics getXMLStats(String path);

    void addStatistics(SemAppStatistics statistics);

    /**
     * Returns a list with {@link Access} objects that contain a timestamp that if after or equal to
     * target date. The list count of returned objects is based on target offset and maximum results.
     *
     * @param after      start date
     * @param offset     starting index
     * @param maxResults maximum results
     * @return all calculated access objects or an empty list
     */
    List<Access> getAccess(Date after, int offset, int maxResults);


    /**
     * Returns a list with {@link AccessLog} objects that contain a timestamp that if after or equal to
     * from date and before or equal to to date for a special reserve collection. The list count of returned objects is
     * based on target and reservecollection id results.
     *
     * @param rcid       collection id
     * @param from       start time (unix time stamp)
     * @param to         end time (unix time stamp)
     * @param actiontype type of executed action
     * @return all calculated access objects sorted by resource id, 
     *         remote host, user agent and timestamp or an empty list
     */
    List<AccessLog> getAccessLog(int rcid, long from, long to, String actiontype);

    /**
     * Returns the highest timestamp of {@link unidue.rc.model.AccessLog} objects if one exists. If none is present,
     * <code>null</code> is returned.
     *
     * @return greatest value of access log timestamp
     */
    Date getLastAccessLogDate();

    /**
     * Returns the count a class was accessed through an action
     *
     * @param resourceClass class of the object
     * @param id            id of the object
     * @param action        executed action
     * @param start         start date
     * @param end           end date
     * @return calculated count
     */
    long getAccessLogCount(Class resourceClass, Integer id, String action, Date start, Date end);
}
