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
package unidue.rc.ui.pages.admin;

import miless.model.User;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.tapestry5.*;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.corelib.components.EventLink;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.TextArea;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;
import org.apache.tapestry5.services.ajax.JavaScriptCallback;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.apache.tapestry5.upload.services.UploadedFile;
import org.slf4j.Logger;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.LibraryLocationDAO;
import unidue.rc.dao.ScanJobDAO;
import unidue.rc.dao.UserDAO;
import unidue.rc.model.*;
import unidue.rc.model.solr.SolrScanJobView;
import unidue.rc.search.*;
import unidue.rc.security.CollectionSecurityService;
import unidue.rc.security.RequiresActionPermission;
import unidue.rc.ui.ProtectedPage;
import unidue.rc.ui.components.AjaxSortLink;
import unidue.rc.ui.components.Pagination;
import unidue.rc.ui.selectmodel.LibraryLocationSelectModel;
import unidue.rc.ui.valueencoder.LibraryLocationValueEncoder;
import unidue.rc.workflow.ResourceService;
import unidue.rc.workflow.ScanJobService;
import unidue.rc.workflow.ScannableService;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static unidue.rc.ui.components.AjaxSortLink.SortState;

/**
 * Created by nils on 07.04.16.
 */
@Import(library = {
        "context:js/admin.scanjobs.js",
        "context:js/print.page.js"
})
@ProtectedPage
public class ScanJobs {

    private enum BlockDefinition {
        Batch, EditJob
    }

    @Inject
    private Logger log;

    @Inject
    private SolrService solrService;

    @Inject
    private ScanJobDAO scanJobDAO;

    @Inject
    private UserDAO userDAO;

    @Inject
    private LibraryLocationDAO libraryLocationDAO;

    @Inject
    private ScanJobService scanJobService;

    @Inject
    private ScannableService scannableService;

    @Inject
    private ResourceService resourceService;

    @Inject
    private CollectionSecurityService securityService;

    @Inject
    private Messages messages;

    @InjectComponent
    private Zone filterZone, jobsZone, editJobZone, batchZone, paginationZone;

    @Inject
    private PageRenderLinkSource linkSource;

    @Property(write = false)
    @Inject
    private Block editJobBlock, queuedJobsBlock;

    @InjectComponent("editJobLink")
    @Property(write = false)
    private EventLink editJobLink;

    @Inject
    private Request request;

    @Inject
    private AjaxResponseRenderer ajaxRenderer;

    @Property(write = false)
    @Persist(PersistenceConstants.FLASH)
    private BlockDefinition visibleBlock;

    @Property
    private SolrScanJobView scanJobView;

    // pagination
    @InjectComponent
    private Pagination pagination;

    // filter

    @Property
    @Persist(PersistenceConstants.SESSION)
    private String fReviser, fAuthor, fScannableType;

    @Property
    @Persist(PersistenceConstants.SESSION)
    private Integer fNumber;

    @Property
    @Persist(PersistenceConstants.SESSION)
    private LibraryLocation fLocation;

    @Property
    @Persist(PersistenceConstants.SESSION)
    private ScanJobStatus fStatus;

    // sort
    @Persist(PersistenceConstants.SESSION)
    private List<SolrSortField> sortStack;

    // queue

    @Property(write = false)
    @Persist(PersistenceConstants.SESSION)
    private List<SolrScanJobView> batchScanJobs;

    @Property
    private SolrScanJobView batchScanJob;

    // edit scan job form values

    @Property
    private int editingJobID;

    @Property
    private SolrScanJobView editingJobView;

    @Property
    private String signature;

    @Property
    private String pageStart;

    @Property
    private String pageEnd;

    @Property
    private String url;

    @Property
    private String reviser;

    @Property
    private Boolean grab;

    @Property(read = false)
    private ScanJobStatus editingJobStatus;

    @Property
    private CopyrightReviewStatus copyrightStatus;

    @Property
    private String docent;

    @Property
    private JobComment comment;

    @Property
    @Persist
    private List<UploadedFile> uploads;

    @Property
    private boolean deleteFile;

    @Property
    private String newComment;

    @Property(write = false)
    private Resource editingResource;

    @Property(write = false)
    private ScanJob editingJob;

    @InjectComponent("edit_scan_job_form")
    private Form editScanJobForm;

    @InjectComponent("newComment")
    private TextArea newCommentField;

    @SetupRender
    void onSetupRender() {
        if (batchScanJobs == null)
            batchScanJobs = new ArrayList<>();

        if (sortStack == null)
            sortStack = new LinkedList<>();
    }

    @RequiresActionPermission(value = ActionDefinition.VIEW_ADMIN_PANEL)
    @OnEvent(EventConstants.ACTIVATE)
    public void onActivate() {
    }

    @OnEvent(value = "editJob")
    void onEditJob(int scanJobID) {
        try {

            loadEditJobData(scanJobID);
            visibleBlock = BlockDefinition.EditJob;

            addAjaxRender(batchZone, editJobZone);

        } catch (SolrServerException e) {
            log.error("could not get scan job from solr", e);
        }
    }

    @OnEvent(value = "enqueueJob")
    void onEnqueueJob(Integer scanJobID) {

        SolrScanJobView scanJobView = getViewByID(scanJobID);
        if (scanJobView != null)
            batchScanJobs.add(scanJobView);

        visibleBlock = BlockDefinition.Batch;

        addAjaxRender(batchZone, editJobZone);
    }

    @OnEvent(value = "dequeueBatch")
    void onDequeueJob(int scanJobID) {

        batchScanJobs = batchScanJobs.stream()
                .filter(job -> job.getJobID() != scanJobID)
                .collect(Collectors.toList());

        visibleBlock = BlockDefinition.Batch;

        addAjaxRender(batchZone, editJobZone);
    }

    @OnEvent(value = "clearBatchList")
    void onClearBatchJobs() {
        batchScanJobs.clear();
        visibleBlock = BlockDefinition.Batch;

        addAjaxRender(batchZone, editJobZone);
    }

    @OnEvent(value = "printBatchList")
    void onPrintBatchJobs() {
        Integer[] scanJobIDs = batchScanJobs.stream()
                .map(job -> job.getJobID())
                .toArray(Integer[]::new);
        Link printPageLink = linkSource.createPageRenderLinkWithContext(unidue.rc.ui.pages.print.ScanJobs.class, scanJobIDs);
        ajaxRenderer.addCallback(new JavaScriptCallback() {
            @Override
            public void run(JavaScriptSupport javascriptSupport) {

                String printLink = printPageLink.toAbsoluteURI();
                String printPageID = javascriptSupport.allocateClientId("printPage");
                javascriptSupport.addScript("print('%s', '%s')", printLink, printPageID);
            }
        });
    }

    @OnEvent(value = "show_batch_queue")
    void onShowBatchQueue() {
        visibleBlock = BlockDefinition.Batch;

        addAjaxRender(batchZone, editJobZone);
    }

    private void loadEditJobData(int scanJobID) throws SolrServerException {

        editingJobID = scanJobID;
        editingJobView = solrService.getById(SolrScanJobView.class, Integer.toString(scanJobID));
        editingJob = scanJobDAO.get(ScanJob.class, scanJobID);
        editingResource = editingJob.getScannable().getResource();

        reviser = editingJobView.getReviser();
        signature = editingJobView.getSignature();
        pageStart = editingJobView.getPageStart();
        pageEnd = editingJobView.getPageEnd();
        editingJobStatus = ScanJobStatus.get(editingJobView.getStatus());
        grab = Boolean.FALSE;

        if (editingResource != null) {
            copyrightStatus = editingResource.getCopyrightReviewStatus();
            url = editingResource.getFullTextURL();
        }
    }

    @OnEvent(value = EventConstants.VALIDATE, component = "edit_scan_job_form")
    void onValidateEditingJob() {
        ScanJob scanJob = scanJobDAO.get(ScanJob.class, editingJobID);

        try {

            // update resource
            updateResource(scanJob, uploads, url, copyrightStatus);

            // update meta of scannable, scanjob and resource
            updateScannable(scanJob.getScannable(), signature, pageStart, pageEnd);
            updateScanJob(scanJob, grab, editingJobStatus);

            // create comment
            if (StringUtils.isNotBlank(newComment)) {
                createComment(scanJob, newComment);
            }
        } catch (CommitException e) {
            log.error("validation of edif job " + editingJobID + " failed", e);
        }
    }

    private void updateScannable(Scannable scannable, String signature, String pageStart, String pageEnd) throws CommitException {

        scannable.setSignature(signature);
        scannable.setPageStart(pageStart);
        scannable.setPageEnd(pageEnd);

        scannableService.update(scannable);
    }

    private void updateScanJob(ScanJob scanJob, Boolean grab, ScanJobStatus status) throws CommitException {
        scanJob.setStatus(status);
        if (grab) {
            User currentUser = securityService.getCurrentUser();
            scanJob.setReviserID(currentUser.getId());
        }
        scanJobService.update(scanJob);
    }

    private void updateResource(ScanJob scanJob, List<UploadedFile> uploads, String url, CopyrightReviewStatus copyrightStatus) throws CommitException {
        Scannable scannable = scanJob.getScannable();
        UploadedFile file = !uploads.isEmpty()
                            ? uploads.get(0)
                            : null;

        try {
            if (deleteFile)
                scannableService.setFileDeleted(scannable);

            scannableService.update(scannable, url);
            if (file != null) {
                scannableService.update(scannable, file.getFileName(), file.getStream());
                uploads.clear();
            }
            Resource resource = scannable.getResource();
            resource.setCopyrightReviewStatus(copyrightStatus);
            resourceService.update(resource);
        } catch (IOException e) {
            editScanJobForm.recordError(messages.format("error.msg.could.not.save.file", scannable));
        } catch (CommitException e) {
            editScanJobForm.recordError(messages.get("error.msg.could.not.commit.data"));
        }
    }

    private void createComment(ScanJob scanJob, String commentValue) {
        User loggedInUser = securityService.getCurrentUser();
        JobComment comment = new JobComment();
        comment.setComment(commentValue);
        comment.setAuthorId(loggedInUser.getUserid());
        comment.setDate(new Date());
        try {
            scanJob.addToComments(comment);
            scanJobDAO.update(scanJob);
        } catch (CommitException e) {
            editScanJobForm.recordError(newCommentField, messages.format("error.msg.could.not.commit.comment",
                    comment));
        }
    }

    @OnEvent(value = EventConstants.SUCCESS, component = "edit_scan_job_form")
    void onEditScanJobSucceeded() {
        String toastrCallback = "toastrSuccess('" + messages.get("success.msg.scan.job.updated") + "');";
        try {
            loadEditJobData(editingJobID);
        } catch (SolrServerException e) {
            log.error("could not load editing job " + editingJobID, e);
        }
        if (request.isXHR()) {
            ajaxRenderer.addCallback((JavaScriptCallback) js -> js.addScript(toastrCallback));
            addAjaxRender(jobsZone, editJobZone);
        }
    }

    @OnEvent(value = "filterNumberChanged")
    Object onFilterNumberChanged() {
        String param = request.getParameter("param");
        fNumber = NumberUtils.isNumber(param)
                  ? NumberUtils.toInt(param)
                  : null;

        return onFilterChange();
    }

    @OnEvent(value = EventConstants.VALUE_CHANGED, component = "locationFilter")
    Object onValueChangedFromLocationFilter(LibraryLocation location) {
        fLocation = location;

        return onFilterChange();
    }

    @OnEvent(value = "filterReviserChanged")
    Object onValueChangedFromReviser() {
        fReviser = request.getParameter("param");

        return onFilterChange();
    }

    @OnEvent(value = "filterAuthorChanged")
    Object onValueChangedFromAuthor() {
        fAuthor = request.getParameter("param");

        return onFilterChange();
    }

    @OnEvent(value = EventConstants.VALUE_CHANGED, component = "fStatus")
    Object onValueChangedFromStatus(ScanJobStatus status) {
        fStatus = status;

        return onFilterChange();
    }

    private Object onFilterChange() {

        pagination.resetCurrentPage();

        if (request.isXHR()) {
            ajaxRenderer.addRender(paginationZone);
        }

        return request.isXHR()
               ? jobsZone.getBody()
               : this;
    }

    @OnEvent(value = "sort")
    void onSort(String column, AjaxSortLink.SortState newSortState) {

        SolrQuery.ORDER solrOrder = getSolrOrder(newSortState);
        Optional<SolrSortField> sortItem = sortStack.stream()
                .filter(item -> item.getFieldName().equals(column))
                .findAny();


        // if the user has already sorted after column
        if (sortItem.isPresent()) {
            SolrSortField sortField = sortItem.get();
            // apply next order if given
            if (solrOrder != null)
                sortField.setOrder(solrOrder);

                // otherwise remove ordering (do not just set to null!)
            else
                sortStack.remove(sortField);
        }

        // else if sort is asc or desc add the field
        else if (solrOrder != null) {
            SolrSortField sortField = new SolrSortField(column);
            sortField.setOrder(solrOrder);
            sortStack.add(sortField);
        }

        addAjaxRender(jobsZone, filterZone);
    }

    /**
     * Called when the number of results per page is changed in pagination-component
     */
    @OnEvent(component = "pagination", value = "change")
    void onValueChanged() {
        if(request.isXHR())
            ajaxRenderer.addRender(jobsZone)
                    .addRender(paginationZone);
    }

    /**
     * is called when user selects another page in pagination
     */
    void onUpdateZones() {
        if(request.isXHR())
            ajaxRenderer.addRender(jobsZone)
                    .addRender(paginationZone);
    }

    public SolrResponse getScanJobs() {

        sortStack = sortStack == null
                    ? Collections.EMPTY_LIST
                    : sortStack;
        try {
            SolrQueryBuilder queryBuilder = solrService.createQueryBuilder();

            if (sortStack != null)
                sortStack.forEach(sortField -> queryBuilder.addSortField(sortField.getFieldName(), sortField.getOrder()));

            List<SolrQueryField> filter = buildFilterParams();
            filter.forEach(param -> queryBuilder.and(param));

            SolrQuery query = queryBuilder.setOffset((pagination.getCurrentPageNumber() - 1) * pagination.getMaxRowsPerPage())
                    .setCount(pagination.getMaxRowsPerPage())
                    .build();

            return solrService.query(SolrScanJobView.class, query);

        } catch (SolrServerException e) {
            log.error("could not query solr server", e);
            return null;
        }
    }

    public String getStatusLabel() {
        return messages.get(ScanJobStatus.getName(scanJobView.getStatus()));
    }

    public String getStatusColor() {
        return getStatusColor(scanJobView);
    }

    public String getScannableTypeLabel() {
        return messages.get(scanJobView.getScannableType());
    }

    public ScanJobStatus getEditingJobStatus() {
        return editingJob != null
               ? editingJob.getStatus()
               : null;
    }

    public String getScannableCreated() {
        return DateFormatUtils.format(scanJobView.getScannableCreated(), "dd.MM.yy");
    }

    public List<JobComment> getComments() {
        List<JobComment> result = Collections.EMPTY_LIST;
        if (editingJob != null) {
            result = editingJob.getComments().stream()
                    .sorted((c1, c2) -> c2.getDate().compareTo(c1.getDate()))
                    .collect(Collectors.toList());
        }
        return result;
    }

    public String getCommentAuthor() {
        User user = userDAO.getUserById(comment.getAuthorId());
        return user != null
               ? user.getRealname()
               : StringUtils.EMPTY;
    }

    public String getCommentDate() {
        return DateFormatUtils.format(comment.getDate(), "dd.MM.yy HH:mm");
    }

    public String getScanJobShortDetails() {

        String result = StringUtils.EMPTY;

        if (batchScanJob != null) {

            List<String> docents = batchScanJob.getDocents();
            docents = docents == null
                      ? Collections.EMPTY_LIST
                      : docents;

            String location = batchScanJob.getLocation();
            Integer collectionNumber = batchScanJob.getCollectionNumber();
            Integer entryID = batchScanJob.getEntryID();
            String type = messages.get(batchScanJob.getScannableType());
            String docentsValue = String.join(", ", docents);

            result = String.format("%s - %d - %d: %s [%s]", location, collectionNumber, entryID, type, docentsValue);
        }
        return result;
    }

    public String getBatchScanJobColor() {

        return batchScanJob != null
               ? getStatusColor(batchScanJob)
               : "#transparent";
    }

    public SelectModel getLibraryLocationSelectModel() {
        return new LibraryLocationSelectModel(libraryLocationDAO);
    }

    public ValueEncoder<LibraryLocation> getLibraryLocationEncoder() {
        return new LibraryLocationValueEncoder(libraryLocationDAO);
    }

    private List<SolrQueryField> buildFilterParams() {
        List<SolrQueryField> result = new ArrayList<>();
        if (fStatus != null) {
            result.add(new SolrNumberQueryField(SolrScanJobView.JOB_STATUS_PROPERTY, fStatus.getValue()));
        }
        if (StringUtils.isNotBlank(fAuthor)) {
            result.add(new SolrTextQueryField(SolrScanJobView.DOCENTS_PROPERTY, fAuthor));
        }
        if (StringUtils.isNotBlank(fReviser)) {
            result.add(new SolrTextQueryField(SolrScanJobView.REVISER_PROPERTY, fReviser));
        }
        if (fNumber != null) {
            result.add(new SolrNumberQueryField(SolrScanJobView.COLLECTION_NUMBER_PROPERTY, fNumber));
        }
        if (fLocation != null) {
            result.add(new SolrNumberQueryField(SolrScanJobView.LOCATION_ID_PROPERTY, fLocation.getId()));
        }
        return result;
    }

    private String getStatusColor(SolrScanJobView scanJobView) {
        ScanJobStatus scanJobStatus = ScanJobStatus.get(scanJobView.getStatus());
        return scanJobStatus != null
               ? "#" + scanJobStatus.getColor()
               : "transparent";
    }

    public boolean isBlockVisible(String name) {
        Optional<BlockDefinition> block = Arrays.stream(BlockDefinition.values())
                .filter(d -> d.name().equals(name))
                .findFirst();
        return block.isPresent() && block.get().equals(visibleBlock);
    }

    private SolrScanJobView getViewByID(Integer scanJobViewID) {

        try {
            return solrService.getById(SolrScanJobView.class, Integer.toString(scanJobViewID));
        } catch (SolrServerException e) {
            log.error("could not get scan job view with id " + scanJobViewID, e);
            return null;
        }
    }

    private void addAjaxRender(ClientBodyElement... elements) {
        if (request.isXHR())
            Arrays.stream(elements).forEach(e -> ajaxRenderer.addRender(e));
    }

    private static SolrQuery.ORDER getSolrOrder(SortState sortState) {
        switch (sortState) {
            case Ascending:
                return SolrQuery.ORDER.asc;
            case Descending:
                return SolrQuery.ORDER.desc;
            case NoSort:
            default:
                return null;
        }
    }
}
