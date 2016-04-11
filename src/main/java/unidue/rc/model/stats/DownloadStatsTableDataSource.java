package unidue.rc.model.stats;

import java.util.List;
import java.util.Set;


/**
 * Created by marcus.koesters on 08.09.15. interface for using datasources / wrapper classes with
 * DownloadsStatsTable-Component
 */
public interface DownloadStatsTableDataSource {



     void addFile(Integer key, StatisticFile file);

     Set<Integer> getResourceIds();

     StatisticFile getFile(Integer key);

     Integer getTotalHitCount();

     public List<DownloadDate> getDownloadDates();

}
