package unidue.rc.model.stats;


import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Created by marcus.koesters on 05.08.15.
 */
@Root
public class SemAppStatistics {

    @ElementList(name = "run", inline = true)
    private List<SemAppStatistic> semAppStatistics;

    public List<SemAppStatistic> getSemAppStatistics() {
        return semAppStatistics;
    }

    public void setSemAppStatistics(List<SemAppStatistic> semAppStatistics) {
        this.semAppStatistics = semAppStatistics;
    }


}
