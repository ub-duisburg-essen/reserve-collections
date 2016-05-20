package unidue.rc.ui.pages.admin;

/*
 * #%L
 * Semesterapparate
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 - 2016 Universitaet Duisburg Essen
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

import miless.model.User;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.tapestry5.*;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.corelib.components.ActionLink;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.TextArea;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;
import org.apache.tapestry5.services.ajax.JavaScriptCallback;
import org.apache.tapestry5.upload.services.UploadedFile;
import org.slf4j.Logger;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.LibraryLocationDAO;
import unidue.rc.dao.ScanJobDAO;
import unidue.rc.dao.UserDAO;
import unidue.rc.model.*;
import unidue.rc.model.solr.SolrScanJobView;
import unidue.rc.search.SolrResponse;
import unidue.rc.search.SolrService;
import unidue.rc.security.CollectionSecurityService;
import unidue.rc.security.RequiresActionPermission;
import unidue.rc.ui.ProtectedPage;
import unidue.rc.ui.selectmodel.LibraryLocationSelectModel;
import unidue.rc.ui.valueencoder.LibraryLocationValueEncoder;
import unidue.rc.workflow.ResourceService;
import unidue.rc.workflow.ScanJobService;
import unidue.rc.workflow.ScannableService;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by nils on 07.04.16.
 */
@Import(library = {
        "context:js/admin.scanjobs.js"
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
    private Zone jobsZone;

    @InjectComponent
    private Zone editJobZone;

    @InjectComponent
    private Zone batchZone;

    @Inject
    private PageRenderLinkSource linkSource;

    @Property(write = false)
    @Inject
    private Block editJobBlock, queuedJobsBlock;

    @Inject
    private Request request;

    @Inject
    private AjaxResponseRenderer ajaxRenderer;

    @Property(write = false)
    @Persist(PersistenceConstants.FLASH)
    private BlockDefinition visibleBlock;

    @Property
    @Persist(PersistenceConstants.FLASH)
    private SolrResponse<SolrScanJobView> scanJobs;

    @Property
    private SolrScanJobView scanJobView;

    // filter

    @Property
    private String fNumber;

    @Property
    private LibraryLocation fLocation;

    @Property
    private String fReviser;

    @Property
    private String fAuthor;

    @Property
    private ScanJobStatus fStatus;

    @Property
    private String fScannableType;

    // queue

    @Property(write = false)
    @Persist(PersistenceConstants.SESSION)
    private List<Integer> batchScanJobIDs;

    @Property
    private Integer batchScanJobID;

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
        batchScanJobIDs = new ArrayList<>();
    }

    @RequiresActionPermission(value = ActionDefinition.VIEW_ADMIN_PANEL)
    @OnEvent(EventConstants.ACTIVATE)
    public void onActivate() {
        loadScanJobs();
    }

    @OnEvent(value = "editJob")
    void onEditJob(int scanJobID) {
        try {

            loadScanJobs();
            loadEditJobData(scanJobID);
            visibleBlock = BlockDefinition.EditJob;

            if (request.isXHR()) {

                ajaxRenderer.addRender(batchZone)
                        .addRender(editJobZone);
            }


        } catch (SolrServerException e) {
            log.error("could not get scan job from solr", e);
        }
    }

    @OnEvent(value = "enqueueJob")
    void onEnqueueJob(Integer scanJobID) {

        loadScanJobs();
        batchScanJobIDs.add(scanJobID);
        visibleBlock = BlockDefinition.Batch;

        if (request.isXHR())
            ajaxRenderer.addRender(batchZone)
                    .addRender(editJobZone);
    }

    @OnEvent(value = "dequeueBatch")
    void onDequeueJob(Integer scanJobID) {

        loadScanJobs();
        batchScanJobIDs.remove(scanJobID);
        visibleBlock = BlockDefinition.Batch;

        if (request.isXHR())
            ajaxRenderer.addRender(batchZone)
                    .addRender(editJobZone);
    }

    @OnEvent(component = "clearBatchList")
    void onClearBatchJobs() {
        batchScanJobIDs.clear();
        visibleBlock = BlockDefinition.Batch;

        if (request.isXHR())
            ajaxRenderer.addRender(batchZone)
                    .addRender(editJobZone);
    }

    @OnEvent(component = "printBatchList")
    void onPrintBatchJobs() {

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
        log.debug("         id: " + editingJobID);
        log.debug("  signature: " + signature);
        log.debug(" page start: " + pageStart);
        log.debug("   page end: " + pageEnd);
        log.debug("       grab: " + grab);
        log.debug("     status: " + editingJobStatus);
        log.debug("        url: " + url);
        log.debug("new comment: " + newComment);
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
                scannableService.deleteFile(scannable);

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
            ajaxRenderer.addRender(jobsZone)
                    .addRender(editJobZone);
        }
    }

    @OnEvent(value = "filterNumberChanged")
    Object onFilterNumberChanged() {
        fNumber = request.getParameter("param");
        log.debug("filter by number: " + fNumber);

        return request.isXHR()
               ? jobsZone.getBody()
               : null;
    }

    @OnEvent(value = EventConstants.VALUE_CHANGED, component = "locationFilter")
    Object onValueChangedFromLocationFilter(LibraryLocation location) {
        fLocation = location;
        log.debug("filter by location: " + location);
        return request.isXHR()
               ? jobsZone.getBody()
               : this;
    }

    @OnEvent(value = "filterReviserChanged")
    Object onValueChangedFromReviser() {
        fReviser = request.getParameter("param");
        log.debug("filter by reviser: " + fReviser);

        return request.isXHR()
               ? jobsZone.getBody()
               : null;
    }

    @OnEvent(value = "filterAuthorChanged")
    Object onValueChangedFromAuthor() {
        fAuthor = request.getParameter("param");
        log.debug("filter by author: " + fAuthor);

        return request.isXHR()
               ? jobsZone.getBody()
               : null;
    }

    @OnEvent(value = EventConstants.VALUE_CHANGED, component = "fStatus")
    Object onValueChangedFromStatus(ScanJobStatus status) {
        fStatus = status;
        log.debug("filter by status: " + status);

        return request.isXHR()
               ? jobsZone.getBody()
               : this;
    }

    public void loadScanJobs() {

        try {
            scanJobs = solrService.query(SolrScanJobView.class, new SolrQuery("*:*"));
        } catch (SolrServerException e) {
            log.error("could not query solr server", e);
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

        Optional<SolrScanJobView> jobViewOptional = scanJobs.getItems().stream()
                .filter(jv -> jv.getJobID() == batchScanJobID)
                .findFirst();
        String result = StringUtils.EMPTY;

        if (jobViewOptional.isPresent()) {

            SolrScanJobView jobView = jobViewOptional.get();

            List<String> docents = jobView.getDocents();
            docents = docents == null
                      ? Collections.EMPTY_LIST
                      : docents;

            String location = jobView.getLocation();
            Integer collectionNumber = jobView.getCollectionNumber();
            Integer entryID = jobView.getEntryID();
            String type = messages.get(jobView.getScannableType());
            String docentsValue = String.join(", ", docents);

            result = String.format("%s - %d - %d: %s [%s]", location, collectionNumber, entryID, type, docentsValue);
        }
        return result;
    }

    public String getBatchScanJobColor() {

        Optional<SolrScanJobView> jobViewOptional = scanJobs.getItems().stream()
                .filter(jv -> jv.getJobID() == batchScanJobID)
                .findFirst();
        return jobViewOptional.isPresent()
               ? getStatusColor(jobViewOptional.get())
               : "#transparent";
    }

    public SelectModel getLibraryLocationSelectModel() {
        return new LibraryLocationSelectModel(libraryLocationDAO);
    }

    public ValueEncoder<LibraryLocation> getLibraryLocationEncoder() {
        return new LibraryLocationValueEncoder(libraryLocationDAO);
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
}
