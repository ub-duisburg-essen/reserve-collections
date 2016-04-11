package unidue.rc.model.stats;


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
