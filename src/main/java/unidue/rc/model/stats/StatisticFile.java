package unidue.rc.model.stats;

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

import java.util.HashMap;
import java.util.Map;

/**
 * Created by marcus.koesters on 03.08.15.
 */
public class StatisticFile {

    private String url;

    private Map<String, Integer> hits = new HashMap<String, Integer>();

    public void addDateHit(String date, Integer count) {
        hits.put(date,count);
    }

    public Integer getHitsForDate(String date) {
        Integer hitsForDate = hits.get(date);
        if(hitsForDate == null) return 0;
        return hits.get(date);
    }


    public Integer getTotalHits() {
        Integer hitcount = 0;
        for (String date : hits.keySet()) {
            hitcount += hits.get(date);
        }
        return hitcount;
    }

}
