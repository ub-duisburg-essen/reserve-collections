package unidue.rc.statistic;


import unidue.rc.model.stats.StatisticDataSource;

/**
 * Created by marcus.koesters on 10.09.15.
 */
public interface StatisticService {
     static final int TIME_OFFSET_HOUR = 1000*60*60;
     static final int TIME_OFFSET_HALFHOUR = 1000*60*30;
     static final int TIME_OFFSET_QUARTERHOUR = 1000*60*15;
     static final int TIME_OFFSET_MINUTE = 1000*60;
     static final int TIME_OFFSET_NONE = 0;
     static final String INTERVAL_RANGE_MONTH = "month";
     static final String INTERVAL_RANGE_DAY = "day";

     /*
     *    All dates have to be in the following format: yyyy-MM-dd
      */
     StatisticDataSource getVisitors(int rcId, String range, String todate, String fromdate);

     StatisticDataSource getDownloads(int rcId,String range, String todate, String fromdate);

}
