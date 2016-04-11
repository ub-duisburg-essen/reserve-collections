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
package unidue.rc.model.stats;



import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by marcus.koesters on 04.08.15.
 */
public class StatsList implements StatisticDataSource {

    private static final Logger LOG = LoggerFactory.getLogger(StatsList.class);


    private List<GenericStat> genericStats = new ArrayList<GenericStat>();

    private String legend;

    public void addStatistic(GenericStat stat) {
        genericStats.add(stat);
        GenericDownloadDate gddate = new GenericDownloadDate();
        gddate.setHitSum(stat.getCount());
        gddate.setVisitsum(stat.getCount());
        gddate.setDate(stat.getDate());
        downloadDates.add(gddate);
    }

    public List<GenericStat> getGenericStats() {
        return genericStats;
    }

    private boolean visible = true;

    private Map<Integer, StatisticFile> files = new HashMap<Integer, StatisticFile>();
    private List<DownloadDate> downloadDates = new ArrayList<DownloadDate>();


    @Override
    public String getGraphValues() {
        String data = "visible: "+visible+", name: '" + legend + "', data :[" + StringUtils.join(getGenericStats(), ",") + "]";
        return data;
    }

    @Override
    public String getLegend() {
        return legend;
    }

    @Override
    public void setLegend(String legend) {
        this.legend = legend;
    }

    @Override
    public List<String> getXCats() {
        List<String> xCats = new ArrayList<String>();

        for (GenericStat stat : getGenericStats()) {
            SimpleDateFormat format = new SimpleDateFormat();
            format.applyPattern("yyyy-MM-dd");
            try {
                java.util.Date date = format.parse(stat.getDate());
                String formattedDate = reformatDate(date);
                xCats.add("'" + formattedDate + "'");

            } catch (ParseException e) {
                LOG.error("Could not parse Date " + stat.getDate(), e);
            }


        }
        return xCats;
    }

    @Override
    public List<String> getYCats() {
        return null;
    }

    @Override
    public void setIsVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public String toString() {
        return "{" + getGraphValues() + "}";
    }

    private String reformatDate(java.util.Date date) {
        SimpleDateFormat format = new SimpleDateFormat();
        String pattern = "dd/MM/yyyy";
        format.applyPattern(pattern);
        String newDate = format.format(date);
        return newDate;
    }

        @Override
        public void addFile(Integer key, StatisticFile file) {
            files.put(key, file);
        }

        @Override
        public Set<Integer> getResourceIds() {
            return files.keySet();
        }

        @Override
        public StatisticFile getFile(Integer key) {
            StatisticFile file = files.get(key);
            if (file == null) {
                file = new StatisticFile();
                addFile(key, file);
            }
            return file;
        }

        @Override
        public Integer getTotalHitCount() {
            Integer hitCount = 0;

            for (Integer key : files.keySet()) {
                StatisticFile file = files.get(key);
                hitCount += file.getTotalHits();
            }
            return hitCount;
        }


    public List<DownloadDate> getDownloadDates() {

        return downloadDates;
    }


}
