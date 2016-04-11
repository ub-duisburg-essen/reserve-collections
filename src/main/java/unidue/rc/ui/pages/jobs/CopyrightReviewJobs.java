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
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.AssetSource;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;
import org.slf4j.Logger;
import se.unbound.tapestry.breadcrumbs.BreadCrumb;
import unidue.rc.dao.ResourceDAO;
import unidue.rc.model.CopyrightReviewStatus;
import unidue.rc.model.solr.SolrCopyrightView;
import unidue.rc.search.SolrQueryBuilder;
import unidue.rc.search.SolrResponse;
import unidue.rc.search.SolrService;
import unidue.rc.search.SolrSortField;
import unidue.rc.ui.components.Pagination;
import unidue.rc.ui.services.MimeService;

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
@BreadCrumb(titleKey = "copyright.review.jobs.page.title")
public class CopyrightReviewJobs {

    /* sort fields for table */
    @Property(write = false)
    private final String SORT_FIELD_NUMBER = SolrCopyrightView.COLLECTION_NUMBER_NUMERIC_PROPERTY;

    @Property(write = false)
    private final String SORT_FIELD_ENTRY_ID = SolrCopyrightView.ENTRY_ID_NUMERIC_PROPERTY;

    @Property(write = false)
    private final String SORT_FIELD_MODIFIED = SolrCopyrightView.MODIFIED_PROPERTY;

    @Property(write = false)
    private final String SORT_FIELD_FILENAME = SolrCopyrightView.FILE_NAME_PROPERTY;

    @Property(write = false)
    private final String SORT_FIELD_MIME_TYPE = SolrCopyrightView.MIME_TYPE_PROPERTY;

    @Property(write = false)
    private final String SORT_FIELD_STATUS = SolrCopyrightView.REVIEW_STATUS_PROPERTY;

    /* filter for table */
    public enum CopyrightFilter {
        QUERY_FILTER,
        STATUS_FILTER,
        MIME_FILTER
    }

    private static final Format MODIFIED_OUTPUT_FORMAT = new SimpleDateFormat("dd.MM.yy HH:mm");

    @Inject
    private Logger log;


    @Property
    private SolrCopyrightView review;

    @Inject
    private SolrService solrService;

    @Inject
    private ResourceDAO resourceDAO;

    @Inject
    private Messages messages;

    @Property(write = false)
    @Inject
    private Block queryFilterFormGroup, mimeFilterFormGroup, statusFilterFormGroup;

    @Property(write = false)
    @Persist
    private List<CopyrightFilter> appliedFilter;

    @Property
    private CopyrightFilter filter;

    @Property
    private CopyrightReviewStatus reviewStatusFilter;

    @Property
    @Persist
    private String mimeFilter;

    @Property
    @Persist
    private String query;

    @InjectComponent
    private Zone filterZone;

    @InjectComponent
    private Zone filterSelectZone;

    @InjectComponent
    private Zone copyrightZone;

    @Inject
    private Request request;

    @Inject
    private AjaxResponseRenderer ajaxResponseRenderer;

    @Persist
    private Vector<SolrSortField> sortStack;

    @Inject
    private AssetSource assetSource;

    @Inject
    private MimeService mimeService;

    @InjectComponent()
    private Pagination pagination;


    @SetupRender
    void init() {
        query = null;
        if (appliedFilter == null) {

            appliedFilter = new ArrayList<>();
            appliedFilter.add(CopyrightFilter.QUERY_FILTER);
        }

        sortStack = new Vector<>();
        sortStack.add(new SolrSortField(SORT_FIELD_NUMBER));
        sortStack.add(new SolrSortField(SORT_FIELD_ENTRY_ID));
        sortStack.add(new SolrSortField(SORT_FIELD_MODIFIED));
        sortStack.add(new SolrSortField(SORT_FIELD_MIME_TYPE));
        sortStack.add(new SolrSortField(SORT_FIELD_STATUS));
        sortStack.add(new SolrSortField(SORT_FIELD_FILENAME));

    }

    @OnEvent(value = "sort")
    public Object onSort(String columnName) {
        applyNextSortOrder(getSortParameter(columnName));
        return request.isXHR() ? copyrightZone.getBody() : null;
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

    public List<CopyrightFilter> getAvailableFilter() {
        return Arrays.stream(CopyrightFilter.values())
                .filter(filter -> !appliedFilter.contains(filter))
                .collect(Collectors.toList());
    }

    public String getNameForFilter() {
        return messages.get(filter.name());
    }

    void onAddFilter(CopyrightFilter filter) {

        appliedFilter.add(filter);
        pagination.resetCurrentPage();
        if (request.isXHR())
            ajaxResponseRenderer.addRender(filterZone)
                    .addRender(filterSelectZone)
                    .addRender(copyrightZone);
    }

    void onRemoveFilter(CopyrightFilter filter) {

        appliedFilter.remove(filter);
        getCopyrightReviews();
        pagination.resetCurrentPage();
        if (request.isXHR())
            ajaxResponseRenderer.addRender(filterZone)
                    .addRender(filterSelectZone)
                    .addRender(copyrightZone);

    }

    Object onQueryChanged(@RequestParameter(value = "param", allowBlank = true) String param) {
        query = param;
        pagination.resetCurrentPage();
        return request.isXHR() ? copyrightZone.getBody() : null;
    }

    @OnEvent(value = EventConstants.VALUE_CHANGED, component = "mimeFilter")
    Object onValueChangedFromMimeFilter(String mime) {
        mimeFilter = mime;
        pagination.resetCurrentPage();
        return request.isXHR() ? copyrightZone.getBody() : null;
    }

    @OnEvent(value = EventConstants.VALUE_CHANGED, component = "reviewStatusFilter")
    Object onValueChangedFromReviewStatusFilter(CopyrightReviewStatus status) {
        reviewStatusFilter = status;
        pagination.resetCurrentPage();

         return request.isXHR() ? copyrightZone.getBody() : null;
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

    public SolrResponse getCopyrightReviews() {
        try {
            // filter by query string
            SolrQueryBuilder queryBuilder = solrService.createQueryBuilder();
            if (query != null && appliedFilter.contains(CopyrightFilter.QUERY_FILTER)) {
                queryBuilder.singleCondition(SolrCopyrightView.SEARCH_FIELD_PROPERTY, this.query);
            }

            // apply select filters
            if (mimeFilter != null && appliedFilter.contains(CopyrightFilter.MIME_FILTER))
                queryBuilder.singleCondition(SolrCopyrightView.MIME_TYPE_PROPERTY, mimeFilter);
            if (reviewStatusFilter != null && appliedFilter.contains(CopyrightFilter.STATUS_FILTER))
                queryBuilder.singleEqualCondition(SolrCopyrightView.REVIEW_STATUS_PROPERTY, reviewStatusFilter.getDatabaseValue().toString());

            // apply sort to query
            for (SolrSortField field : sortStack) {
                SolrQuery.ORDER order = field.getOrder();
                if (order != null)
                    queryBuilder.addSortField(field.getFieldName(), order);
            }

            // run query
            SolrQuery query = queryBuilder.setOffset((pagination.getCurrentPageNumber()-1) * pagination.getMaxRowsPerPage())
                    .setCount(pagination.getMaxRowsPerPage())
                    .build();
            SolrResponse<SolrCopyrightView> response= solrService.query(SolrCopyrightView.class,query);
            log.debug("Response has "+response.getCount()+" items");
            return response;

        } catch (SolrServerException e) {
            log.error("could not query solr", e);
            return new SolrResponse<SolrCopyrightView>();

        }
    }

    public Block getBlockForFilter() {
        Block result = null;
        if (filter != null) {
            switch (filter) {
                case STATUS_FILTER:
                    result = statusFilterFormGroup;
                    break;
                case MIME_FILTER:
                    result = mimeFilterFormGroup;
                    break;
                case QUERY_FILTER:
                    result = queryFilterFormGroup;
                    break;
            }
        }
        return result;
    }

    public String getStatusLabel() {
        return messages.get(CopyrightReviewStatus.getName(review.getReviewStatus()));
    }

    public String getStatusColor() {
        CopyrightReviewStatus reviewStatus = CopyrightReviewStatus.get(review.getReviewStatus());
        return reviewStatus != null ? "#" + reviewStatus.getColor() : "transparent";
    }

    public Format getModifiedOutputFormat() {
        return MODIFIED_OUTPUT_FORMAT;
    }

    public String getMimeTypeIcon() {
        if (review.getMimeType() == null)
            return null;

        String mimeType = review.getMimeType();
        String iconFileName;
        if (mimeType.startsWith("video"))
            iconFileName = "media-video.png";
        else if (mimeType.startsWith("audio"))
            iconFileName = "media-audio.png";
        else if (mimeType.startsWith("image"))
            iconFileName = "media-image.png";
        else if (mimeType.startsWith("text"))
            iconFileName = "text-plain.png";
        else
            iconFileName = "unknown.png";
        Resource asset = assetSource.resourceForPath("context:img/mimetypes/" + iconFileName);
        return asset.getFile();
    }

    @OnEvent(component = "pagination", value = "change")
    void onValueChanged() {
        if(request.isXHR())
            ajaxResponseRenderer.addRender(copyrightZone);
    }

    void onUpdateZones() {
        log.debug("UpdateZones Event is triggered. Current Page ="+pagination.getCurrentPageNumber()+" max rows= "+pagination.getMaxRowsPerPage());
        if(request.isXHR())
            ajaxResponseRenderer.addRender(copyrightZone);
    }

}
