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
package unidue.rc.ui.pages.jobs;


import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.tapestry5.Block;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;
import org.slf4j.Logger;
import se.unbound.tapestry.breadcrumbs.BreadCrumb;
import unidue.rc.dao.LibraryLocationDAO;
import unidue.rc.model.ActionDefinition;
import unidue.rc.model.BookJobStatus;
import unidue.rc.model.LibraryLocation;
import unidue.rc.model.solr.SolrBookJobView;
import unidue.rc.search.SolrQueryBuilder;
import unidue.rc.search.SolrResponse;
import unidue.rc.search.SolrService;
import unidue.rc.search.SolrSortField;
import unidue.rc.security.RequiresActionPermission;
import unidue.rc.ui.ProtectedPage;
import unidue.rc.ui.components.Pagination;
import unidue.rc.ui.selectmodel.LibraryLocationSelectModel;
import unidue.rc.ui.valueencoder.LibraryLocationValueEncoder;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

/**
 * @author Nils Verheyen
 * @since 06.12.13 15:39
 */
@Import(library = {
        "context:js/toggle.filter.zone.js"
})
@BreadCrumb(titleKey = "book.jobs.page.title")
@ProtectedPage
public class BookJobs {

    /* sort fields for table */
    @Property(write = false)
    private final String SORT_FIELD_NUMBER = SolrBookJobView.COLLECTION_NUMBER_NUMERIC_PROPERTY;

    @Property(write = false)
    private final String SORT_FIELD_JOB_ID = SolrBookJobView.JOB_ID_NUMERIC_PROPERTY;

    @Property(write = false)
    private final String SORT_FIELD_MODIFIED = SolrBookJobView.MODIFIED_PROPERTY;

    @Property(write = false)
    private final String SORT_FIELD_SIGNATURE = SolrBookJobView.SIGNATURE_PROPERTY;

    @Property(write = false)
    private final String SORT_FIELD_TITLE = SolrBookJobView.TITLE_PROPERTY;

    @Property(write = false)
    private final String SORT_FIELD_STATUS = SolrBookJobView.STATUS_PROPERTY;

    /* filter for table */
    public static enum JobsFilter {
        LOCATION_FILTER,
        QUERY_FILTER,
        JOB_STATUS_FILTER
    }

    private static final Format MODIFIED_OUTPUT_FORMAT = new SimpleDateFormat("dd.MM.yy HH:mm");

    @Inject
    private Logger log;

    @Property
    private SolrBookJobView bookJob;

    @Inject
    private Messages messages;

    @Inject
    private LibraryLocationDAO locationDAO;

    @Inject
    private SolrService solrService;

    @Property(write = false)
    @Inject
    private Block queryFilterFormGroup, locationFilterFormGroup, statusFilterFormGroup;

    @Property(write = false)
    @Persist
    private List<JobsFilter> appliedFilter;

    @Property
    private JobsFilter filter;

    @Property
    @Persist
    private BookJobStatus bookJobStatusFilter;

    @Property
    @Persist
    private LibraryLocation locationFilter;

    @Persist
    private Vector<SolrSortField> sortStack;

    @Property
    @Persist
    private String query;

    @Property
    private long openBookJobs;

    @InjectComponent
    private Pagination pagination;

    @InjectComponent
    private Zone filterZone;

    @InjectComponent
    private Zone filterSelectZone;


    @InjectComponent
    private Zone bookJobsZone;

    @Inject
    private Request request;

    @Inject
    private AjaxResponseRenderer ajaxResponseRenderer;

    @SetupRender
    @RequiresActionPermission(ActionDefinition.EDIT_BOOK_JOBS)
    void init() {
        query = null;
        if (appliedFilter == null) {

            appliedFilter = new ArrayList<>();
            appliedFilter.add(JobsFilter.QUERY_FILTER);
        }
        sortStack = new Vector<>();
        sortStack.add(new SolrSortField(SORT_FIELD_NUMBER));
        sortStack.add(new SolrSortField(SORT_FIELD_JOB_ID));
        sortStack.add(new SolrSortField(SORT_FIELD_MODIFIED));
        sortStack.add(new SolrSortField(SORT_FIELD_SIGNATURE));
        sortStack.add(new SolrSortField(SORT_FIELD_TITLE));
        sortStack.add(new SolrSortField(SORT_FIELD_STATUS));
    }

    @OnEvent(EventConstants.ACTIVATE)
    void onActivate() {

        SolrQuery query = new SolrQueryBuilder()
                .qEqual(SolrBookJobView.STATUS_PROPERTY, BookJobStatus.NEW.getValue().toString())
                .build();
        // run query
        try {
            SolrResponse<SolrBookJobView> jobs = solrService.query(SolrBookJobView.class, query);
            openBookJobs = jobs.getCount();
        } catch (SolrServerException e) {
            log.error("could not query solr server", e);
        }
    }

    @OnEvent(value = "sort")
    public Object onSort(String name) {
        applyNextSortOrder(getSortParameter(name));
        pagination.resetCurrentPage();
        return request.isXHR() ? bookJobsZone.getBody() : null;
    }

    /**
     * Removes target sort parameter from this sort stack, applies the next order and pushes it back on top of the
     * stack
     *
     * @param sortField
     */
    private void applyNextSortOrder(SolrSortField sortField) {

        // remove old sortStack parameter from queue
        sortStack.remove(sortField);

        // reset all other parameter to default
        for (SolrSortField c : sortStack) {
            c.resetSortOrder();
        }

        // get next order parameter to provide new parameter
        sortField.applyNextSortOrder();

        // offer new parameter which is put on top of all orderings
        sortStack.add(0, sortField);

        pagination.resetCurrentPage();
    }

    private SolrSortField getSortParameter(String name) {

        for (SolrSortField sortField : sortStack) {
            if (sortField.getFieldName().equals(name))
                return sortField;
        }
        return null;
    }

    public String getClassForSortByNumber() {
        return getClassForSortParameter(getSortParameter(SORT_FIELD_NUMBER));
    }

    public String getClassForSortByJobID() {
        return getClassForSortParameter(getSortParameter(SORT_FIELD_JOB_ID));
    }

    public String getClassForSortByModified() {
        return getClassForSortParameter(getSortParameter(SORT_FIELD_MODIFIED));
    }

    public String getClassForSortBySignature() {
        return getClassForSortParameter(getSortParameter(SORT_FIELD_SIGNATURE));
    }

    public String getClassForSortByTitle() {
        return getClassForSortParameter(getSortParameter(SORT_FIELD_TITLE));
    }

    public String getClassForSortByStatus() {
        return getClassForSortParameter(getSortParameter(SORT_FIELD_STATUS));
    }

    private String getClassForSortParameter(SolrSortField sortField) {

        SolrQuery.ORDER order = sortField.getOrder();

        if (SolrQuery.ORDER.asc.equals(order))
            return "sort-icon-asc";
        else if (SolrQuery.ORDER.desc.equals(order))
            return "sort-icon-desc";
        return StringUtils.EMPTY;
    }

    public List<JobsFilter> getAvailableFilter() {
        List<JobsFilter> filter = new ArrayList<>(Arrays.asList(JobsFilter.values()));
        filter.removeAll(appliedFilter);
        return filter;
    }

    public String getNameForFilter() {
        return messages.get(filter.name());
    }

    void onAddFilter(JobsFilter filter) {
        pagination.resetCurrentPage();
        appliedFilter.add(filter);

        if (request.isXHR())
            ajaxResponseRenderer.addRender(filterZone)
                    .addRender(filterSelectZone)
                    .addRender(bookJobsZone);
    }

    void onRemoveFilter(JobsFilter filter) {
        pagination.resetCurrentPage();
        appliedFilter.remove(filter);

        if (request.isXHR())
            ajaxResponseRenderer.addRender(filterZone)
                    .addRender(filterSelectZone)
                    .addRender(bookJobsZone);

    }

    Object onQueryChanged() {
        pagination.resetCurrentPage();
        query = request.getParameter("param");
        return request.isXHR() ? bookJobsZone.getBody() : null;
    }

    @OnEvent(value = EventConstants.VALUE_CHANGED, component = "locationFilter")
    Object onValueChangedFromLocationFilter(LibraryLocation location) {
        pagination.resetCurrentPage();
        locationFilter = location;
        return request.isXHR() ? bookJobsZone.getBody() : null;
    }

    @OnEvent(value = EventConstants.VALUE_CHANGED, component = "bookJobStatusFilter")
    Object onValueChangedFromCollectionStatusFilter(BookJobStatus status) {
        pagination.resetCurrentPage();
        bookJobStatusFilter = status;
        return request.isXHR() ? bookJobsZone.getBody() : null;
    }

    public SolrResponse getBookJobs() {

        try {
            SolrQueryBuilder queryBuilder = solrService.createQueryBuilder();
            if (query != null && appliedFilter.contains(JobsFilter.QUERY_FILTER)) {
                queryBuilder.singleCondition(SolrBookJobView.SEARCH_FIELD_PROPERTY, this.query);
            }

            if (locationFilter != null && appliedFilter.contains(JobsFilter.LOCATION_FILTER)) {
                queryBuilder.singleEqualCondition(SolrBookJobView.LOCATION_PROPERTY, locationFilter.getName());
            }

            if (bookJobStatusFilter != null && appliedFilter.contains(JobsFilter.JOB_STATUS_FILTER)) {
                queryBuilder.singleEqualCondition(SolrBookJobView.STATUS_PROPERTY, bookJobStatusFilter.getDatabaseValue().toString());
            }

            for (SolrSortField field : sortStack) {
                SolrQuery.ORDER order = field.getOrder();
                if (order != null)
                    queryBuilder.addSortField(field.getFieldName(), order);
            }

            SolrQuery query = queryBuilder.setOffset(
                    (pagination.getCurrentPageNumber()-1)
                            *pagination.getMaxRowsPerPage())
                    .setCount(pagination.getMaxRowsPerPage())
                    .build();

            return solrService.query(SolrBookJobView.class, query);
        } catch (SolrServerException e) {
            log.error("could not query solr", e);
        }
        return new SolrResponse();
    }

    public SelectModel getLibraryLocationSelectModel() {
        return new LibraryLocationSelectModel(locationDAO);
    }

    public ValueEncoder<LibraryLocation> getLibraryLocationEncoder() {
        return new LibraryLocationValueEncoder(locationDAO);
    }

    public Block getBlockForFilter() {
        Block result = null;
        if (filter != null) {
            switch (filter) {
                case JOB_STATUS_FILTER:
                    result = statusFilterFormGroup;
                    break;
                case LOCATION_FILTER:
                    result = locationFilterFormGroup;
                    break;
                case QUERY_FILTER:
                    result = queryFilterFormGroup;
                    break;
            }
        }
        return result;
    }

    public String getStatusLabel() {
        return messages.get(BookJobStatus.getName(bookJob.getStatus()));
    }

    public Format getModifiedOutputFormat() {
        return MODIFIED_OUTPUT_FORMAT;
    }


    @OnEvent(component = "pagination", value = "change")
    void onValueChanged() {
        if(request.isXHR())
            ajaxResponseRenderer.addRender(bookJobsZone);
    }

    void onUpdateZones() {
        log.debug("UpdateZones Event is triggered. Current Page ="+pagination.getCurrentPageNumber()+" max rows= "+pagination.getMaxRowsPerPage());
        if(request.isXHR())
            ajaxResponseRenderer.addRender(bookJobsZone);
    }
}
