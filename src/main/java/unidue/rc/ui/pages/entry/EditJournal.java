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
package unidue.rc.ui.pages.entry;



import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.tapestry5.*;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.Service;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.TextField;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.upload.services.UploadedFile;
import org.slf4j.Logger;
import se.unbound.tapestry.breadcrumbs.BreadCrumb;
import unidue.rc.dao.BaseDAO;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.HeadlineDAO;
import unidue.rc.dao.ResourceDAO;
import unidue.rc.model.*;
import unidue.rc.security.CollectionSecurityService;
import unidue.rc.ui.ProtectedPage;
import unidue.rc.ui.SecurityContextPage;
import unidue.rc.ui.pages.collection.ViewCollection;
import unidue.rc.workflow.ResourceService;
import unidue.rc.workflow.ScannableService;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Within this page an existing {@link ReserveCollection} can be edited.
 *
 * @author Marcus Koesters
 */
@BreadCrumb(titleKey = "new.journal.article")
@ProtectedPage
public class EditJournal implements SecurityContextPage {

    @Inject
    private PageRenderLinkSource linkSource;

    @Inject
    private Logger log;

    @Property
    private boolean deleteFile;

    @Property
    @Persist
    private List<UploadedFile> uploads;

    @Property
    private UploadedFile file;

    @Property
    private String filename;

    @Inject
    @Service(ResourceDAO.SERVICE_NAME)
    private ResourceDAO resourceDAO;

    @Inject
    @Service(HeadlineDAO.SERVICE_NAME)
    private HeadlineDAO headlineDAO;

    @Property
    private Headline headline;

    private String fullTextURL;

    @Property
    private CopyrightReviewStatus copyrightStatus;

    /**
     * The {@link ReserveCollection} edited within this page. The {@link Persist} annotation has to be present here, so
     * the object is page persisted.
     *
     * @see <a href="http://tapestry.apache.org/persistent-page-data.html">Persistent Page Data</a>
     */
    @Property
    @Persist
    private ReserveCollection collection;

    @Property
    private JournalArticle journal;

    @Inject
    @Service(BaseDAO.SERVICE_NAME)
    private BaseDAO baseDAO;

    @Inject
    private ScannableService scannableService;

    @Inject
    private ResourceService resourceService;

    @Inject
    private Messages messages;

    @Component(id = "journal_form")
    private Form form;

    @Component(id = "filename")
    private TextField filenameField;

    @SetupRender
    void onSetupRender() {
        uploads = new ArrayList<>();
    }

    @OnEvent(EventConstants.ACTIVATE)
    void onActivate(Integer entryId) {

        log.info("loading entry " + entryId);
        journal = baseDAO.get(JournalArticle.class, entryId);
        collection = journal.getReserveCollection();

        Resource resource = journal.getResource();
        if (resource != null) {
            copyrightStatus = resource.getCopyrightReviewStatus();
            filename = resource.getFileName();
        }

        headline = journal.getEntry().getAssignedHeadline();
    }

    void onValidateFromJournalurl(String value) throws ValidationException {


        if (value == null) return;
        try {
            URL test = new URL(value);
        } catch (MalformedURLException e) {

            throw new ValidationException(e.getLocalizedMessage());
        }

    }

    void onValidateFromFilename(String value) throws ValidationException {

        Resource resource = journal.getResource();
        if (resource != null
                && StringUtils.isNotBlank(resource.getFileName())
                && StringUtils.isBlank(value)) {
            throw new ValidationException(messages.get("filename-required-message"));
        }
    }

    @OnEvent(EventConstants.SUCCESS)
    Object onJournalSubmitted() throws MalformedURLException {
        Resource resource = journal.getResource();
        final int action = uploads != null && !uploads.isEmpty()
                           ? 0x110 // delete file and upload new one
                           : deleteFile
                             ? 0x100 // delete file
                             : resource != null && !StringUtils.endsWith(resource.getFilePath(), filename)
                               ? 0x001 // just rename
                               : 0;
        if ((action & 0x100) == 0x100) {
            deleteFile();
        }
        if ((action & 0x010) == 0x010) {
            uploadFile();
        }
        if ((action & 0x001) == 0x001) {
            updateResource(resource);
        }
        try {
            scannableService.update(journal, fullTextURL);

            resource = journal.getResource();
            if (resource != null) {
                resource.setCopyrightReviewStatus(copyrightStatus);
                resourceService.update(resource);
            }
        } catch (CommitException e){
                form.recordError(messages.format("error.msg.could.not.commit.journal", journal));
        }

        try {
            if (headline != null)
                headlineDAO.move(journal.getEntry(), headline);
        } catch (CommitException e) {
            form.recordError(messages.format("error.msg.could.not.commit.headline", headline));
        }

        Link returnLink = null;
        if (!form.getHasErrors()) {
            returnLink = linkSource.createPageRenderLinkWithContext(ViewCollection.class,
                    collection.getId());
            returnLink.setAnchor(journal.getId().toString());

        }
        return returnLink;
    }

    private void updateResource(Resource resource) {
        resource.setCopyrightReviewStatus(copyrightStatus);
        try {
            if (!resource.getFilePath().endsWith(filename))
                resourceDAO.rename(resource, filename);

            resourceService.update(resource);
        } catch (CommitException e) {
            form.recordError(messages.format("error.msg.could.not.commit.resource", resource));
        } catch (IOException e) {
            form.recordError(messages.format("error.msg.could.not.move.file", resource));
        }
    }

    private void uploadFile() {

        try {
            UploadedFile uploadedFile = uploads.get(0);
            Resource resource = scannableService.update(journal, uploadedFile.getFileName(), uploadedFile.getStream());
            uploads = null;
        } catch (IOException e) {
            form.recordError(messages.format("error.msg.could.not.save.file", journal));
        } catch (CommitException e) {
            form.recordError(messages.format("error.msg.could.not.commit.journal", journal));
        }
    }

    private void deleteFile() {

        try {
            scannableService.setFileDeleted(journal);
        } catch (CommitException e) {
            form.recordError(messages.format("error.msg.could.not.commit.chapter", journal));
        }
    }

    /**
     * Called when the link for this page is generated.
     *
     * @return
     * @see {@link EventConstants#PASSIVATE}
     */
    @OnEvent(EventConstants.PASSIVATE)
    Integer onPassivate() {
        return journal.getId();
    }

    @Override
    public void checkPermission(CollectionSecurityService securityService, EventContext activationContext) throws AuthorizationException {
        Integer journalID = activationContext.get(Integer.class, 0);
        JournalArticle journalArticle = baseDAO.get(JournalArticle.class, journalID);
        securityService.checkPermission(ActionDefinition.EDIT_ENTRIES, journalArticle.getEntry().getReserveCollection().getId());
    }

    public String getFullTextURL() {
        return journal.getResource() != null ? journal.getResource().getFullTextURL() : null;
    }

    public void setFullTextURL(String fullTextURL) {
        this.fullTextURL = fullTextURL;
    }
}