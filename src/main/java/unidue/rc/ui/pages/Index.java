package unidue.rc.ui.pages;

/*
 * #%L
 * Semesterapparate
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 Universitaet Duisburg Essen
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

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.tapestry5.Block;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import se.unbound.tapestry.breadcrumbs.BreadCrumb;
import se.unbound.tapestry.breadcrumbs.BreadCrumbReset;
import unidue.rc.dao.LibraryLocationDAO;
import unidue.rc.model.ActionDefinition;
import unidue.rc.model.LibraryLocation;
import unidue.rc.model.ReserveCollection;
import unidue.rc.model.ReserveCollectionStatus;
import unidue.rc.model.solr.SolrCollectionView;
import unidue.rc.search.SolrQueryBuilder;
import unidue.rc.search.SolrResponse;
import unidue.rc.search.SolrService;
import unidue.rc.search.SolrSortField;
import unidue.rc.security.CollectionSecurityService;
import unidue.rc.ui.components.Pagination;
import unidue.rc.ui.selectmodel.LibraryLocationSelectModel;
import unidue.rc.ui.valueencoder.LibraryLocationValueEncoder;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Start page of application reserve-collections.
 */
@Import(library = {
        "context:js/toggle.filter.zone.js"
})
@BreadCrumb(titleKey = "html.title")
@BreadCrumbReset
public class Index {

    /* sort fields for table */

    @Property(write = false)
    public static final String SORT_FIELD_LOCATION = SolrCollectionView.LOCATION_PROPERTY;

    @Property(write = false)
    public static final String SORT_FIELD_NUMBER = SolrCollectionView.COLLECTION_NUMBER_NUMERIC_PROPERTY;

    @Property(write = false)
    public static final String SORT_FIELD_TITLE = SolrCollectionView.TITLE_PROPERTY;

    @Property(write = false)
    public static final String SORT_FIELD_EXPIRATION = SolrCollectionView.VALID_TO_PROPERTY;

    /* filter for table */
    public static enum ReserveCollectionFilter {

        LOCATION_FILTER,
        QUERY_FILTER,
        COLLECTION_STATUS_FILTER,
        DISSOLVE_AT_FILTER
    }

    private static final DateTimeFormatter LIST_DATE_FORMATTER = DateTimeFormat.forPattern("d.M.y");

    @Inject
    private Logger log;

    @Inject
    private LibraryLocationDAO locationDAO;

    @Inject
    private CollectionSecurityService securityService;

    @Property
    @Persist
    private ReserveCollectionFilter filter;

    @Property
    @Persist
    private String query;

    @Property
    @Persist
    private LibraryLocation locationFilter;

    @Property(write = false)
    @Persist
    private List<ReserveCollectionFilter> appliedFilter;

    @Property
    private ReserveCollection collection;

    @Property
    private SolrCollectionView collectionView;

    @Property
    private String author;

    @Property
    private long collectionCount;

    @Property
    @Persist
    private ReserveCollectionStatus collectionStatusFilter;

    @Inject
    private SolrService solrService;

    @InjectComponent
    private Zone filterZone;

    @InjectComponent
    private Zone filterSelectZone;

    @InjectComponent
    private Zone reserveCollectionZone;

    @Inject
    private Request request;

    @Inject
    private Messages messages;

    @Inject
    private AjaxResponseRenderer ajaxResponseRenderer;

    @Property(write = false)
    @Inject
    private Block queryFilterFormGroup, collectionStatusFilterFormGroup, locationFilterFormGroup, dissolveAtFilterFormGroup;

    @Persist
    private Vector<SolrSortField> sortStack;

    @InjectComponent()
    private Pagination pagination;

    @SetupRender
    void init() {

        query = null;
        initFilter();
        initSortStack();
    }

    private void initFilter() {
        if (appliedFilter == null) {
            appliedFilter = new ArrayList<>();
            appliedFilter.add(ReserveCollectionFilter.QUERY_FILTER);
        }
    }

    private void initSortStack() {
        sortStack = new Stack<>();
        sortStack.add(new SolrSortField(SORT_FIELD_TITLE));
        sortStack.add(new SolrSortField(SORT_FIELD_LOCATION));
        sortStack.add(new SolrSortField(SORT_FIELD_NUMBER));
        sortStack.add(new SolrSortField(SORT_FIELD_EXPIRATION));
    }

    public void onAddFilter(ReserveCollectionFilter filter) {

        appliedFilter.add(filter);
        pagination.resetCurrentPage();
        if (request.isXHR())
            ajaxResponseRenderer.addRender(filterZone)
                    .addRender(filterSelectZone)
                    .addRender(reserveCollectionZone);
    }

    public void onRemoveFilter(ReserveCollectionFilter filter) {

        appliedFilter.remove(filter);
        pagination.resetCurrentPage();
        if (request.isXHR())
            ajaxResponseRenderer.addRender(filterZone)
                    .addRender(filterSelectZone)
                    .addRender(reserveCollectionZone);

    }

    public Block getBlockForFilter() {
        Block result = null;
        if (filter != null) {
            switch (filter) {
                case LOCATION_FILTER:
                    result = locationFilterFormGroup;
                    break;
                case QUERY_FILTER:
                    result = queryFilterFormGroup;
                    break;
                case COLLECTION_STATUS_FILTER:
                    result = collectionStatusFilterFormGroup;
                    break;
                case DISSOLVE_AT_FILTER:
                    result = dissolveAtFilterFormGroup;
                    break;
            }
        }
        return result;
    }

    Object onQueryChanged() {
        query = request.getParameter("param");
        return request.isXHR() && appliedFilter != null
               ? reserveCollectionZone.getBody()
               : this;
    }

    @OnEvent(value = EventConstants.VALUE_CHANGED, component = "locationFilter")
    Object onValueChangedFromLocationFilter(LibraryLocation location) {
        locationFilter = location;
        return request.isXHR() && appliedFilter != null
               ? reserveCollectionZone.getBody()
               : this;
    }

    @OnEvent(value = EventConstants.VALUE_CHANGED, component = "collectionStatusFilter")
    Object onValueChangedFromCollectionStatusFilter(ReserveCollectionStatus status) {
        collectionStatusFilter = status;
        return request.isXHR() && appliedFilter != null
               ? reserveCollectionZone.getBody()
               : this;
    }

    @OnEvent("sort")
    public Block onSort(String field) {
        applyNextSortOrder(getSortParameter(field));

        return request.isXHR() ? reserveCollectionZone.getBody() : null;
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
        for (SolrSortField field : sortStack) {
            field.resetSortOrder();
        }

        // get next order parameter to provide new parameter
        sortField.applyNextSortOrder();

        // offer new parameter which is put on top of all orderings
        sortStack.add(0, sortField);
    }

    public String getClassForSortByLocation() {
        return getClassForSortParameter(getSortParameter(SORT_FIELD_LOCATION));
    }

    public String getClassForSortByNumber() {
        return getClassForSortParameter(getSortParameter(SORT_FIELD_NUMBER));
    }

    public String getClassForSortByExpiration() {
        return getClassForSortParameter(getSortParameter(SORT_FIELD_EXPIRATION));
    }

    public String getClassForSortByTitle() {
        return getClassForSortParameter(getSortParameter(SORT_FIELD_TITLE));
    }

    private SolrSortField getSortParameter(String fieldName) {

        if (sortStack != null) {
            for (SolrSortField field : sortStack) {
                if (field.getFieldName().equals(fieldName))
                    return field;
            }
        }
        return null;
    }

    private String getClassForSortParameter(SolrSortField sortField) {

        if (sortField == null)
            return StringUtils.EMPTY;

        final SolrQuery.ORDER order = sortField.getOrder();
        if (order == null) {
            return StringUtils.EMPTY;
        } else if (order == SolrQuery.ORDER.asc) {
            return "sort-icon-asc";
        } else {
            return "sort-icon-desc";
        }
    }

    public List<ReserveCollectionFilter> getAvailableFilter() {

        return Arrays.asList(ReserveCollectionFilter.values())
                .stream()
                .filter(filter -> !appliedFilter.contains(filter))
                .filter(filter -> {
                    if (filter.equals(ReserveCollectionFilter.COLLECTION_STATUS_FILTER))
                        return securityService.isPermitted(ActionDefinition.FILTER_COLLECTION_LIST_BY_STATUS);
                    return true;
                })
                .collect(Collectors.toList());
    }

    public String getNameForFilter() {
        return messages.get(filter.name());
    }


    public String getExpirationDate() {
        return LIST_DATE_FORMATTER.print(collectionView.getValidTo().getTime());
    }


    public String getDissolutionDate() {
        return LIST_DATE_FORMATTER.print(collectionView.getDissolveAt().getTime());
    }

    /**
     * Returns the {@link SelectModel} that is used inside html select box to set the {@link
     * unidue.rc.model.LibraryLocation} for the new reserve collection.
     *
     * @return the select model
     */
    public SelectModel getLibraryLocationSelectModel() {
        return new LibraryLocationSelectModel(locationDAO);
    }

    /**
     * Returns the {@link org.apache.tapestry5.ValueEncoder} to encode/decode {@link LibraryLocation} objects to use
     * them inside html pages.
     *
     * @return the value encoder
     */
    public ValueEncoder<LibraryLocation> getLibraryLocationEncoder() {
        return new LibraryLocationValueEncoder(locationDAO);
    }

    /**
     * Available as property inside Index.tml to iterate over all reserve collections.
     *
     * @return A list of all {@link ReserveCollection} objects
     */
    public SolrResponse getReserveCollections() {
        try {
            SolrQueryBuilder queryBuilder = solrService.createQueryBuilder();
            if (query != null && appliedFilter.contains(ReserveCollectionFilter.QUERY_FILTER)) {
                queryBuilder.singleCondition(SolrCollectionView.SEARCH_FIELD_PROPERTY, this.query);
            }

            if (locationFilter != null && appliedFilter.contains(ReserveCollectionFilter.LOCATION_FILTER)) {
                queryBuilder.singleEqualCondition(SolrCollectionView.LOCATION_PROPERTY, locationFilter.getName());
            }

            if (collectionStatusFilter != null && appliedFilter.contains(ReserveCollectionFilter.COLLECTION_STATUS_FILTER)) {
                queryBuilder.singleEqualCondition(SolrCollectionView.STATUS_PROPERTY, collectionStatusFilter.getDatabaseValue().toString());
            } else {
                queryBuilder.singleEqualCondition(SolrCollectionView.STATUS_PROPERTY, ReserveCollectionStatus.ACTIVE
                        .getDatabaseValue().toString());
            }

            if (appliedFilter.contains(ReserveCollectionFilter.DISSOLVE_AT_FILTER)) {
                queryBuilder.singleEqualCondition(SolrCollectionView.DISSOLVE_AT_PROPERTY, "*");
            }

            boolean isOrderingSelected = sortStack != null
                    && sortStack.stream().anyMatch(field -> field.getOrder() != null);
            if (!isOrderingSelected) {
                queryBuilder.addSortField(SORT_FIELD_LOCATION, SolrQuery.ORDER.asc);
                queryBuilder.addSortField(SORT_FIELD_NUMBER, SolrQuery.ORDER.asc);
            } else {

                for (SolrSortField field : sortStack) {
                    SolrQuery.ORDER order = field.getOrder();
                    if (order != null)
                        queryBuilder.addSortField(field.getFieldName(), order);
                }
            }

            SolrQuery query = queryBuilder.setOffset((pagination.getCurrentPageNumber() - 1) * pagination.getMaxRowsPerPage())
                    .setCount(pagination.getMaxRowsPerPage())
                    .build();


            SolrResponse<SolrCollectionView> result = solrService.query(SolrCollectionView.class, query);
            collectionCount = result.getCount();
            return result;
        } catch (SolrServerException e) {
            log.error("could not query solr", e);
        }
        return new SolrResponse<SolrCollectionView>();
    }

    /*
        Called when the number of results per page is changed in pagination-component
     */
    @OnEvent(component = "pagination", value = "change")
    void onValueChanged() {
        if(request.isXHR())
            ajaxResponseRenderer.addRender(reserveCollectionZone);
    }

    /*
    is called when user selects another page in pagination
     */
     void onUpdateZones() {
         if(request.isXHR())
            ajaxResponseRenderer.addRender(reserveCollectionZone);
    }

}
