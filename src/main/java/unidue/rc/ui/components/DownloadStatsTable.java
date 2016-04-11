package unidue.rc.ui.components;


import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;
import unidue.rc.dao.ResourceDAO;
import unidue.rc.model.Resource;
import unidue.rc.model.stats.DownloadDate;
import unidue.rc.model.stats.DownloadStatsTableDataSource;

/**
 * Created by marcus.koesters on 08.09.15.
 */
public class DownloadStatsTable {


    @Parameter(required = true)
    @Property
    private DownloadStatsTableDataSource dataSource;

    @Property
    private DownloadDate downloadDate;

    @Property
    private Integer id;

    @Inject
    private ComponentResources resources;

    @Inject
    private ResourceDAO resourceDAO;

    @Inject
    private Logger log;


    public String getDateHeading(String date) {
        String[] dateArray = date.split("-");
        return dateArray[1]+"/"+dateArray[2];
    }

    public String getFileName(int resourceId) {
        Resource resource = resourceDAO.get(Resource.class, resourceId);
        return resource.getFileName();
    }

}