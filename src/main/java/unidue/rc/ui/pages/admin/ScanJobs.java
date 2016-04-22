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

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;
import org.slf4j.Logger;
import unidue.rc.dao.LibraryLocationDAO;
import unidue.rc.dao.ScanJobDAO;
import unidue.rc.model.ActionDefinition;
import unidue.rc.model.LibraryLocation;
import unidue.rc.model.ScanJob;
import unidue.rc.model.ScanJobStatus;
import unidue.rc.model.solr.SolrScanJobView;
import unidue.rc.search.SolrResponse;
import unidue.rc.search.SolrService;
import unidue.rc.security.RequiresActionPermission;
import unidue.rc.ui.ProtectedPage;
import unidue.rc.ui.selectmodel.LibraryLocationSelectModel;
import unidue.rc.workflow.ScanJobService;

/**
 * Created by nils on 07.04.16.
 */
@ProtectedPage
public class ScanJobs {

    @Inject
    private Logger log;

    @Inject
    private SolrService solrService;

    @Inject
    private ScanJobDAO scanJobDAO;

    @Inject
    private LibraryLocationDAO libraryLocationDAO;

    @Inject
    private ScanJobService scanJobService;

    @Inject
    private Messages messages;

    @InjectComponent
    private Zone jobsZone;

    @InjectComponent
    private Zone editJobZone;

    @Inject
    private Request request;

    @Inject
    private AjaxResponseRenderer ajaxResponseRenderer;

    @Property
    private SolrScanJobView scanJob;

    @Property
    private SolrScanJobView editingJob;

    @Property
    private LibraryLocation reviser;

    @Property
    private String docent;

    private boolean inFormSubmission;

    private int rowNum;

    @RequiresActionPermission(value = ActionDefinition.VIEW_ADMIN_PANEL)
    public void onActivate() {
    }

    @OnEvent(value = "editJob")
    void onEditJob(int scanJobID) {
        log.debug("editing " + scanJobID);
        try {
            editingJob = solrService.getById(SolrScanJobView.class, Integer.toString(scanJobID));

            if (editingJob.getReviser() != null) {
                reviser = libraryLocationDAO.getLocationById(editingJob.getReviserID());
            }

            if (request.isXHR())
                ajaxResponseRenderer.addRender(editJobZone);

        } catch (SolrServerException e) {
            log.error("could not get scan job from solr", e);
        }

    }

    public SolrResponse<SolrScanJobView> getScanJobs() {

        SolrResponse<SolrScanJobView> scanJobs = null;
        try {
            scanJobs = solrService.query(SolrScanJobView.class, new SolrQuery("*:*"));
        } catch (SolrServerException e) {
            log.error("could not query solr server", e);
        }
        return scanJobs == null
               ? new SolrResponse<>()
               : scanJobs;
    }

    public String getStatusLabel() {
        return messages.get(ScanJobStatus.getName(scanJob.getStatus()));
    }

    public String getStatusColor() {
        ScanJobStatus scanJobStatus = ScanJobStatus.get(scanJob.getStatus());
        return scanJobStatus != null
               ? "#" + scanJobStatus.getColor()
               : "transparent";
    }

    public String getScannableTypeLabel() {
        return messages.get(scanJob.getScannableType());
    }

    public SelectModel getReviserModel() {
        return new LibraryLocationSelectModel(libraryLocationDAO);
    }

    public ScanJobStatus getEditingJobStatus() {
        if (editingJob != null) {
            ScanJob scanJob = scanJobDAO.get(ScanJob.class, editingJob.getJobID());
            return scanJob != null
                   ? scanJob.getStatus()
                   : null;
        }
        return null;
    }

    public boolean isAction() {
        return false;
    }

    public void setAction(boolean action) {
        if (inFormSubmission) {
            rowNum++;
        }
    }
}
