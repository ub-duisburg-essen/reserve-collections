package unidue.rc.ui.components;

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