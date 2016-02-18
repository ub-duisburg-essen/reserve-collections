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
