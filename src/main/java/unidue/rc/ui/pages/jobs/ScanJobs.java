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
import unidue.rc.model.LibraryLocation;
import unidue.rc.model.ScanJobStatus;
import unidue.rc.model.solr.SolrScanJobView;
import unidue.rc.search.SolrQueryBuilder;
import unidue.rc.search.SolrResponse;
import unidue.rc.search.SolrService;
import unidue.rc.search.SolrSortField;
import unidue.rc.security.RequiresActionPermission;
import unidue.rc.ui.ProtectedPage;
import unidue.rc.ui.components.Pagination;
import unidue.rc.ui.selectmodel.LibraryLocationListSelectModel;
import unidue.rc.ui.selectmodel.LibraryLocationSelectModel;
import unidue.rc.ui.valueencoder.LibraryLocationValueEncoder;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

/**
 * @author Nils Verheyen
 * @since 12.12.13 11:24
 */
@Import(library = {
        "context:js/toggle.filter.zone.js"
})
@BreadCrumb(titleKey = "scan.jobs.page.title")
@ProtectedPage
public class ScanJobs {

    /* sort fields for table */
    @Property(write = false)
    private final String SORT_FIELD_NUMBER = SolrScanJobView.COLLECTION_NUMBER_NUMERIC_PROPERTY;

    @Property(write = false)
    private final String SORT_FIELD_ENTRY_ID = SolrScanJobView.ENTRY_ID_NUMERIC_PROPERTY;

    @Property(write = false)
    private final String SORT_FIELD_MODIFIED = SolrScanJobView.SCANNABLE_MODIFIED_PROPERTY;

    @Property(write = false)
    private final String SORT_FIELD_SIGNATURE = SolrScanJobView.JOURNAL_SIGNATURE_PROPERTY;

    @Property(write = false)
    private final String SORT_FIELD_TITLE = SolrScanJobView.JOURNAL_TITLE_PROPERTY;

    @Property(write = false)
    private final String SORT_FIELD_STATUS = SolrScanJobView.JOB_STATUS_PROPERTY;

    /* filter for table */
    public enum ScanJobsFilter {
        LOCATION_FILTER,
        REVISER_FILTER,
        QUERY_FILTER,
        JOB_STATUS_FILTER
    }

    private static final Format MODIFIED_OUTPUT_FORMAT = new SimpleDateFormat("dd.MM.yy HH:mm");

    @Inject
    private Logger log;

    @Property
    private SolrScanJobView scanJob;

    @Inject
    private SolrService solrService;

    @Inject
    private Messages messages;

    @Inject
    private LibraryLocationDAO locationDAO;

    @Property(write = false)
    @Inject
    private Block queryFilterFormGroup, locationFilterFormGroup, reviserFilterFormGroup, statusFilterFormGroup;

    @Property(write = false)
    @Persist
    private List<ScanJobsFilter> appliedFilter;

    @Property
    private ScanJobsFilter filter;

    @Property
    @Persist
    private ScanJobStatus scanJobStatusFilter;

    @Property
    @Persist
    private LibraryLocation locationFilter;

    @Property
    @Persist
    private LibraryLocation reviserFilter;

    @Property
    @Persist
    private String query;

    @Property
    private Long openScanJobs;

    @InjectComponent
    private Zone filterZone;

    @InjectComponent
    private Zone filterSelectZone;

    @InjectComponent
    private Pagination pagination;

    @InjectComponent
    private Zone scanJobsZone;

    @Inject
    private Request request;

    @Inject
    private AjaxResponseRenderer ajaxResponseRenderer;

    @Persist
    private Vector<SolrSortField> sortStack;

    @SetupRender
    @RequiresActionPermission(ActionDefinition.EDIT_SCAN_JOBS)
    void init() {
        query = null;
        if (appliedFilter == null) {

            appliedFilter = new ArrayList<>();
            appliedFilter.add(ScanJobsFilter.QUERY_FILTER);
        }

        sortStack = new Vector<>();
        sortStack.add(new SolrSortField(SORT_FIELD_NUMBER));
        sortStack.add(new SolrSortField(SORT_FIELD_ENTRY_ID));
        sortStack.add(new SolrSortField(SORT_FIELD_MODIFIED));
        sortStack.add(new SolrSortField(SORT_FIELD_SIGNATURE));
        sortStack.add(new SolrSortField(SORT_FIELD_STATUS));
        sortStack.add(new SolrSortField(SORT_FIELD_TITLE));

        pagination.resetCurrentPage();
    }

    @OnEvent(EventConstants.ACTIVATE)
    void onActivate() {

        SolrQuery query = new SolrQueryBuilder()
                .qEqual(SolrScanJobView.JOB_STATUS_PROPERTY, ScanJobStatus.NEW.getValue().toString())
                .build();
        // run query
        try {
            SolrResponse<SolrScanJobView> jobs = solrService.query(SolrScanJobView.class, query);
            openScanJobs = jobs.getCount();
        } catch (SolrServerException e) {
            log.error("could not query solr server", e);
        }
    }

    @OnEvent(value = "sort")
    public Object onSort(String columnName) {
        applyNextSortOrder(getSortParameter(columnName));
        pagination.resetCurrentPage();
        return request.isXHR() ? scanJobsZone.getBody() : null;
    }


    private SolrSortField getSortParameter(String name) {

        for (SolrSortField sortField : sortStack) {
            if (sortField.getFieldName().equals(name))
                return sortField;
        }
        return null;
    }

    public String getClassForSort(String fieldName) {
        SolrSortField sortField = getSortField(fieldName);
        if (sortField != null) {
            SolrQuery.ORDER order = sortField.getOrder();

            if (SolrQuery.ORDER.asc.equals(order))
                return "sort-icon-asc";
            else if (SolrQuery.ORDER.desc.equals(order))
                return "sort-icon-desc";
        }
        return StringUtils.EMPTY;
    }

    private SolrSortField getSortField(String fieldName) {
        for (SolrSortField field : sortStack) {
            if (field.getFieldName().equals(fieldName))
                return field;
        }
        return null;
    }

    public List<ScanJobsFilter> getAvailableFilter() {
        return Arrays.stream(ScanJobsFilter.values())
                .filter(filter ->!appliedFilter.contains(filter))
                .collect(Collectors.toList());
    }

    public String getNameForFilter() {
        return messages.get(filter.name());
    }

    void onAddFilter(ScanJobsFilter filter) {

        appliedFilter.add(filter);
        pagination.resetCurrentPage();

        if (request.isXHR())
            ajaxResponseRenderer.addRender(filterZone)
                    .addRender(filterSelectZone)
                    .addRender(scanJobsZone);
    }

    void onRemoveFilter(ScanJobsFilter filter) {

        appliedFilter.remove(filter);
        pagination.resetCurrentPage();

        if (request.isXHR())
            ajaxResponseRenderer.addRender(filterZone)
                    .addRender(filterSelectZone)
                    .addRender(scanJobsZone);

    }

    Object onQueryChanged(@RequestParameter(value = "param", allowBlank = true) String param) {
        query = param;
        pagination.resetCurrentPage();
        return request.isXHR() ? scanJobsZone.getBody() : null;
    }

    @OnEvent(value = EventConstants.VALUE_CHANGED, component = "locationFilter")
    Object onValueChangedFromLocationFilter(LibraryLocation location) {
        locationFilter = location;
        pagination.resetCurrentPage();
        return request.isXHR() ? scanJobsZone.getBody() : null;
    }

    @OnEvent(value = EventConstants.VALUE_CHANGED, component = "reviserFilter")
    Object onValueChangedFromReviserFilter(LibraryLocation reviser) {
        reviserFilter = reviser;
        pagination.resetCurrentPage();
        return request.isXHR() ? scanJobsZone.getBody() : null;
    }

    @OnEvent(value = EventConstants.VALUE_CHANGED, component = "scanJobStatusFilter")
    Object onValueChangedFromCollectionStatusFilter(ScanJobStatus status) {
        scanJobStatusFilter = status;
        pagination.resetCurrentPage();
        return request.isXHR() ? scanJobsZone.getBody() : null;
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
    }

    public SolrResponse getScanJobs() {
        try {
            // filter by query string
            SolrQueryBuilder queryBuilder = solrService.createQueryBuilder();
            if (query != null && appliedFilter.contains(ScanJobsFilter.QUERY_FILTER)) {
                queryBuilder.singleCondition(SolrScanJobView.SEARCH_FIELD_PROPERTY, this.query);
            }

            // apply select filters
            if (locationFilter != null && appliedFilter.contains(ScanJobsFilter.LOCATION_FILTER)) {
                queryBuilder.singleEqualCondition(SolrScanJobView.LOCATION_PROPERTY, locationFilter.getName());
            }
            if (reviserFilter != null && appliedFilter.contains(ScanJobsFilter.REVISER_FILTER)) {
                queryBuilder.singleEqualCondition(SolrScanJobView.REVISER_PROPERTY, reviserFilter.getName());
            }
            if (scanJobStatusFilter != null && appliedFilter.contains(ScanJobsFilter.JOB_STATUS_FILTER)) {
                queryBuilder.singleEqualCondition(SolrScanJobView.JOB_STATUS_PROPERTY, scanJobStatusFilter.getDatabaseValue().toString());
            }

            // apply sort to query
            for (SolrSortField field : sortStack) {
                SolrQuery.ORDER order = field.getOrder();
                if (order != null)
                    queryBuilder.addSortField(field.getFieldName(), order);
            }

            // run query
            SolrQuery query = queryBuilder
                    .setOffset((pagination.getCurrentPageNumber() - 1) * pagination.getMaxRowsPerPage())
                    .setCount(pagination.getMaxRowsPerPage())
                    .build();
            return solrService.query(SolrScanJobView.class, query);

        } catch (SolrServerException e) {
            log.error("could not query solr", e);
            return new SolrResponse<SolrScanJobView>();
        }
    }

    public SelectModel getLibraryLocationSelectModel() {
        return new LibraryLocationSelectModel(locationDAO);
    }

    public SelectModel getReviserSelectModel() {
        return new LibraryLocationListSelectModel(locationDAO);
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
                case REVISER_FILTER:
                    result = reviserFilterFormGroup;
                    break;
                case QUERY_FILTER:
                    result = queryFilterFormGroup;
                    break;
            }
        }
        return result;
    }

    public String getStatusLabel() {
        return messages.get(ScanJobStatus.getName(scanJob.getStatus()));
    }

    public String getStatusColor() {
        ScanJobStatus scanJobStatus = ScanJobStatus.get(scanJob.getStatus());
        return scanJobStatus != null ? "#" + scanJobStatus.getColor() : "transparent";
    }

    public Format getModifiedOutputFormat() {
        return MODIFIED_OUTPUT_FORMAT;
    }


    @OnEvent(component = "pagination", value = "change")
    void onValueChanged() {
        if(request.isXHR())
            ajaxResponseRenderer.addRender(scanJobsZone);
    }

    void onUpdateZones() {
        log.debug("UpdateZones Event is triggered. Current Page ="+pagination.getCurrentPageNumber()+" max rows= "+pagination.getMaxRowsPerPage());
        if(request.isXHR())
            ajaxResponseRenderer.addRender(scanJobsZone);
    }

}
