package unidue.rc.statistic;



import org.apache.cayenne.di.Inject;
import org.joda.time.Days;
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
    private long from;
    private long to;

    @Inject
    private StatisticDAO statisticDAO;

    @Override
    public StatisticDataSource getVisitors(int rcId, String range, String todate, String fromdate) {
        LOG.debug("Fetching  visitors");
        return fetchStatsForAction(StatisticDAO.VIEW, rcId, range, todate, fromdate, StatisticService.TIME_OFFSET_HALFHOUR);
    }

    @Override
    public StatisticDataSource getDownloads(int rcId, String range, String todate, String fromdate) {
        LOG.debug("Fetching Downloads");
        return fetchStatsForAction(StatisticDAO.DOWNLOAD, rcId, range, todate, fromdate, StatisticService.TIME_OFFSET_NONE);
    }

    private StatsList fetchStatsForAction(String action, int rcId, String range, String todate, String fromdate, int
            timeoffset) {
        StatsList datasource = new StatsList();

        from = prepareDateForQuery(fromdate);
        to = prepareDateForQuery(todate);

        int intervalcount = 0;
        if (range.equals("month")) {
            Months months = countMonths(from, to);
            intervalcount = months.getMonths();

        } else if (range.equals("day")) {
            Days days = countDays(from, to);
            intervalcount = days.getDays();
        }
        int i;
        LOG.debug("Interval-Count is " + intervalcount);
        for (i = 0; i <= intervalcount; i++) {
            if (range.equals("month")) {

                LocalDateTime f = new LocalDateTime(from);
                LocalDateTime t = f.dayOfMonth().withMaximumValue().hourOfDay().withMaximumValue().minuteOfHour()
                        .withMaximumValue().secondOfMinute().withMaximumValue().millisOfSecond().withMaximumValue();

                String[] dateString = f.toString().split("T");

                Date date2 = t.toDate();

                long computedto = date2.getTime();
                if (to < computedto) {
                    computedto = to;
                }

                List<AccessLog> logs = statisticDAO.getAccessLog(rcId, from, computedto, action);

                Map<Integer, Integer> hitcounter = new HashMap<Integer, Integer>();

                List<AccessLog> logsToRemove = new ArrayList<AccessLog>();

                String lasthost = "";
                long lasttimestamp = 0;
                for (AccessLog log : logs) {
                    if (timeoffset != StatisticService.TIME_OFFSET_NONE && action.equals(StatisticDAO.VIEW) ) {
                        if (lasthost.equals(log.getRemoteHost())) {
                            long timestamp = log.getTimestamp();
                            long difference = timestamp - lasttimestamp;
                            if (difference <= timeoffset) {
                                logsToRemove.add(log);
                            } else {
                                lasttimestamp = timestamp;
                            }
                        } else {
                            lasthost = log.getRemoteHost();
                            lasttimestamp = 0;
                        }
                    } else {
                        raiseDownloadCount(log.getResourceID(), hitcounter);
                    }
                }
                logs.removeAll(logsToRemove);
                GenericStat stat = new GenericStat();
                stat.setCount(logs.size());
                stat.setDate(dateString[0]);
                from = getNextIntervalStart(computedto).toDate().getTime();
                datasource.addStatistic(stat);
                for (int resourceID : hitcounter.keySet()) {
                    StatisticFile file = datasource.getFile(resourceID);
                    file.addDateHit(dateString[0], hitcounter.get(resourceID));
                }
            }
        }

        return datasource;
    }

    private void raiseDownloadCount(int resourceID, Map<Integer, Integer> hitcounter) {

        if (hitcounter.containsKey(resourceID)) {
            hitcounter.put(resourceID, hitcounter.get(resourceID) + 1);
        } else {
            hitcounter.put(resourceID, 1);
        }
    }

    private Days countDays(long from, long to) {
        LocalDate fromDate = new LocalDate(from);
        LocalDate toDate = new LocalDate(to);
        LOG.debug("FromDate= " + fromDate + " ToDate " + toDate);
        return Days.daysBetween(fromDate, toDate);
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
