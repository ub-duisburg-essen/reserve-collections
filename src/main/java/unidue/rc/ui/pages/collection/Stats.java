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
package unidue.rc.ui.pages.collection;


import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.Service;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.TextField;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.HttpError;
import org.slf4j.Logger;
import se.unbound.tapestry.breadcrumbs.BreadCrumb;
import se.unbound.tapestry.breadcrumbs.BreadCrumbList;
import unidue.rc.dao.ReserveCollectionDAO;
import unidue.rc.dao.ResourceDAO;
import unidue.rc.dao.UserDAO;
import unidue.rc.model.ActionDefinition;
import unidue.rc.model.ReserveCollection;
import unidue.rc.model.stats.HighChartsGraphDataSource;
import unidue.rc.model.stats.StatisticDataSource;
import unidue.rc.security.RequiresActionPermission;
import unidue.rc.statistic.DBStatService;
import unidue.rc.statistic.StatisticService;
import unidue.rc.system.SystemConfigurationService;
import unidue.rc.ui.ProtectedPage;

import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

/**
 * @author Nils Verheyen
 * @since 16.08.13 14:56
 */
@Import(library = {
        "context:js/stats.datepicker.js", "context:js/jquery.ui.datepicker.de.js"

})
@BreadCrumb(titleKey = "title-label")
@ProtectedPage
public class Stats {

    @Inject
    SystemConfigurationService systemConfigurationService;
    @Inject
    private Logger log;
    @Inject
    private ReserveCollectionDAO collectionDAO;

    @Inject
    @Service(UserDAO.SERVICE_NAME)
    private UserDAO userDAO;

    @Inject
    private Messages messages;

    @Inject
    private ComponentResources resources;

    @SessionState
    private BreadCrumbList breadCrumbList;

    @Property
    private ReserveCollection collection;

    @Inject
    private Locale locale;

    @Inject
    @Service(DBStatService.SERVICE_NAME)
    private StatisticService statisticService;

    @Property
    private List<HighChartsGraphDataSource> dataSources;

    @Property
    private String xCats;

    @Property
    @Persist(PersistenceConstants.SESSION)
    private String from;

    @Property
    @Persist(PersistenceConstants.SESSION)
    private String to;

    @Property
    private StatisticDataSource downloads;

    @Inject
    private ResourceDAO resourceDAO;

    @Component
    private Form statisticsForm;

    @InjectComponent("from")
    private TextField fromfield;

    @InjectComponent("to")
    private TextField tofield;

    @SetupRender
    public void beginRender() {

         if (from == null || from.isEmpty()) {
            Calendar calendar = new GregorianCalendar();
            calendar.add(Calendar.MONTH, -6);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            from = prepareDateForInputBox(calendar.getTime());
        }

        if (to == null || to.isEmpty()) {

            Calendar calendar = new GregorianCalendar();
            calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE));
            to = prepareDateForInputBox(calendar.getTime());
        }

        dataSources = new ArrayList<HighChartsGraphDataSource>();
        breadCrumbList.getLastCrumb().setTitle(messages.get("statistic"));
        log.debug("fetching stats");
        StatisticDataSource visitors = statisticService.getVisitors(collection.getId(),
                prepareDateForQuery(to), prepareDateForQuery(from));
        downloads = statisticService.getDownloads(collection.getId(), prepareDateForQuery(to),prepareDateForQuery(from));
        log.debug("visitor stats "+visitors.getGraphValues());
        visitors.setLegend(messages.get("stats.graph.label.document") + " " + collection.getId());
        downloads.setLegend(messages.get("stats.graph.label.fileentries") + " " + collection.getId());

        dataSources.add(visitors);
        dataSources.add(downloads);
        xCats = StringUtils.join(visitors.getXCats(), ",");
    }

    @OnEvent(EventConstants.ACTIVATE)
    @RequiresActionPermission(ActionDefinition.READ_COLLECTION_STATISTICS)
    Object onPageActivate(int reserveCollectionId) {
        collection = collectionDAO.get(ReserveCollection.class, reserveCollectionId);

        return collection != null
               ? null
               : new HttpError(HttpServletResponse.SC_NOT_FOUND, messages.get("error.msg.collection.not.found"));
    }

    @OnEvent(EventConstants.PASSIVATE)
    Integer onPagePassivate() {
        return collection != null
               ? collection.getId()
               : null;
    }

    @OnEvent(EventConstants.SUCCESS)
    Object afterFormSubmit() {
        log.debug("From=" + from + " to=" + to);

        return this;
    }

    private String prepareDateForInputBox(Date date) {

        String pattern = "dd.MM.yyyy";
        SimpleDateFormat format = new SimpleDateFormat();
        format.applyPattern(pattern);

        String d = format.format(date);

        return d;

    }



    private String prepareDateForQuery(String input) {

        String pattern = "yyyy-MM-dd";
        String fromPattern = "dd.MM.yyyy";
        SimpleDateFormat format = new SimpleDateFormat(fromPattern);

        Date date = null;
        try {
            date = format.parse(input);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        format.applyPattern(pattern);
        String d = format.format(date);

        return d;
    }

    void onValidateFromStatisticsForm() {
        String fromPattern = "dd.MM.yyyy";
        SimpleDateFormat format = new SimpleDateFormat(fromPattern);

        Date fromdate = null;
        Date todate = null;
        try {
            fromdate = format.parse(from);
            todate = format.parse(to);


        if(fromdate.getTime() > todate.getTime()) {
            statisticsForm.recordError(fromfield, messages.get("statistic.error.from"));
            statisticsForm.recordError(tofield, messages.get("statistic.error.to"));
        }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }



}


