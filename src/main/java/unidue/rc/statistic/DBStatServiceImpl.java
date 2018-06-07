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
package unidue.rc.statistic;



import org.apache.cayenne.di.Inject;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.Months;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unidue.rc.dao.StatisticDAO;
import unidue.rc.model.AccessLog;
import unidue.rc.model.stats.GenericStat;
import unidue.rc.model.stats.StatisticDataSource;
import unidue.rc.model.stats.StatisticFile;
import unidue.rc.model.stats.StatsList;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by marcus.koesters on 16.09.15.
 */
public class DBStatServiceImpl implements DBStatService {
    private static final Logger LOG = LoggerFactory.getLogger(DBStatServiceImpl.class);
    private long chosenFromMillis;
    private long chosenToMillis;

    @Inject
    private StatisticDAO statisticDAO;

    @Override
    public StatisticDataSource getVisitors(int rcId, String todate, String fromdate) {
        LOG.debug("Fetching  visitors");
        return fetchStatsForAction(StatisticDAO.VIEW, rcId, todate, fromdate, StatisticService.TIME_OFFSET_HALFHOUR);
    }

    @Override
    public StatisticDataSource getDownloads(int rcId, String todate, String fromdate) {
        LOG.debug("Fetching Downloads");
        return fetchStatsForAction(StatisticDAO.DOWNLOAD, rcId, todate, fromdate, StatisticService.TIME_OFFSET_NONE);
    }

    private StatsList fetchStatsForAction(String action, int rcId, String todate, String fromdate, int
            timeoffset) {
        StatsList datasource = new StatsList();

        // query start and end date dd.mm.yyyy to long in millis
        chosenFromMillis = prepareDateForQuery(fromdate);
        chosenToMillis = prepareDateForQuery(todate);

        // whole months between start and end
        Months months = countMonths(chosenFromMillis, chosenToMillis);
        int numberOfMonths = months.getMonths();

        LOG.debug("Number of months is " + numberOfMonths);
        // go over each month
        for (int i = 0; i <= numberOfMonths; i++) {

            LocalDateTime f = new LocalDateTime(chosenFromMillis);
            // last day of month of start date
            LocalDateTime t = f.dayOfMonth().withMaximumValue()
                                .hourOfDay().withMaximumValue()
                                .minuteOfHour().withMaximumValue()
                                .secondOfMinute().withMaximumValue()
                                .millisOfSecond().withMaximumValue();

            // 0 = yyyy-MM-dd
            // 1 = HH:mm:ss.SSS
            String[] dateString = f.toString().split("T");

            /* 
             * If the chosen interval is inside one month set the computation
             * date to the chosen date, otherwise use the last day of month
             * of the start day
             */
            long computedto = t.toDate().getTime();
            if (chosenToMillis < computedto) {
                computedto = chosenToMillis;
            }

            List<AccessLog> logs = statisticDAO.getAccessLog(rcId, chosenFromMillis, computedto, action);

            Map<Integer, Integer> downloadCount = new HashMap<Integer, Integer>();

            switch (action) {
                case StatisticDAO.VIEW:
                    logs = filterViewLogs(logs, timeoffset);
                    break;
                case StatisticDAO.DOWNLOAD:
                    raiseDownloadCounts(logs, downloadCount);
                    break;
            }
            GenericStat stat = new GenericStat();
            stat.setCount(logs.size());
            stat.setDate(dateString[0]);
            chosenFromMillis = getNextIntervalStart(computedto).toDate().getTime();
            datasource.addStatistic(stat);
            for (int resourceID : downloadCount.keySet()) {
                StatisticFile file = datasource.getFile(resourceID);
                file.addDateHit(dateString[0], downloadCount.get(resourceID));
            }
        }

        return datasource;
    }

    private List<AccessLog> filterViewLogs(List<AccessLog> logs, int timeoffset) {

        // save last host and user agent to check for multiple accesses
        String lastHost = "";
        String lastUserAgent = "";
        long lastTimestamp = 0;
        List<AccessLog> logsToRemove = new ArrayList<>();
        for (AccessLog log : logs) {

            /*
             * If last host and user agent are equal skip the log entry
             */
            if (lastHost.equals(log.getRemoteHost()) && lastUserAgent.equals(log.getUserAgent())) {

                long timestamp = log.getTimestamp();
                long difference = timestamp - lastTimestamp;
                /*
                 * If the difference between the last access time and the current access time is
                 * smaller than the given offset ignore the log. We do not want hits, but
                 * visits. This does only work because the list of logs is ordered by hostname
                 * and user agent.
                 */
                if (difference <= timeoffset) {
                    logsToRemove.add(log);
                } else {
                    lastTimestamp = timestamp;
                }
            } else {
                lastHost = log.getRemoteHost();
                lastUserAgent = log.getUserAgent();
                lastTimestamp = 0;
            }
        }
        logs.removeAll(logsToRemove);
        return logs;
    }

    private void raiseDownloadCounts(List<AccessLog> logs, Map<Integer, Integer> downloadCount) {

        for (AccessLog log : logs) {
            Integer resourceID = log.getResourceID();
            if (downloadCount.containsKey(resourceID)) {
                downloadCount.put(resourceID, downloadCount.get(resourceID) + 1);
            } else {
                downloadCount.put(resourceID, 1);
            }
        }
    }

    private Months countMonths(long from, long to) {
        LocalDate fromDate = new LocalDate(from);
        LocalDate toDate = new LocalDate(to);
        LOG.debug("FromDate= " + fromDate + " ToDate " + toDate);
        return Months.monthsBetween(fromDate, toDate);
    }


    private LocalDateTime getNextIntervalStart(long time) {
        LocalDateTime date = new LocalDateTime(time);

        return date.plusMillis(1);
    }

    private long prepareDateForQuery(String input) {


        String fromPattern = "yyyy-MM-dd";
        SimpleDateFormat format = new SimpleDateFormat(fromPattern);

        Date date = null;
        try {
            date = format.parse(input);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date.getTime();
    }
}
