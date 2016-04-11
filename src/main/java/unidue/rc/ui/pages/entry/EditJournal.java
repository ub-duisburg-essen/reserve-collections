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



import org.apache.shiro.authz.AuthorizationException;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.ValidationException;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.Service;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.upload.services.UploadedFile;
import org.slf4j.Logger;
import se.unbound.tapestry.breadcrumbs.BreadCrumb;
import unidue.rc.dao.BaseDAO;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.HeadlineDAO;
import unidue.rc.model.*;
import unidue.rc.security.CollectionSecurityService;
import unidue.rc.ui.ProtectedPage;
import unidue.rc.ui.SecurityContextPage;
import unidue.rc.ui.pages.collection.ViewCollection;
import unidue.rc.workflow.EntryService;
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
        copyrightStatus = resource != null ? resource.getCopyrightReviewStatus() : null;

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

    @OnEvent(EventConstants.SUCCESS)
    Object onJournalSubmitted() throws MalformedURLException {

        try {
            if (deleteFile)
                scannableService.deleteFile(journal);

            scannableService.update(journal, fullTextURL);
            if (uploads != null && !uploads.isEmpty()) {
                UploadedFile uploadedFile = uploads.get(0);
                scannableService.update(journal, uploadedFile.getFileName(), uploadedFile.getStream());
                uploads = null;
            }
        } catch (IOException e){
                form.recordError(messages.format("error.msg.could.not.save.file", journal));
        } catch (CommitException e){
                form.recordError(messages.format("error.msg.could.not.commit.journal", journal));
        }

        Resource resource = journal.getResource();
        if (resource != null) {
            resource.setCopyrightReviewStatus(copyrightStatus);
            try {
                resourceService.update(resource);
            } catch (CommitException e) {
                form.recordError(messages.format("error.msg.could.not.commit.resource", resource));
            }
        }
        try {
            if (headline != null)
                headlineDAO.move(journal.getEntry(), headline);
        } catch (CommitException e) {
            form.recordError(messages.format("error.msg.could.not.commit.headline", headline));
        }

        Link returnLink = null;
        if (!form.getHasErrors()) {

            log.info("journalarticle entry for " + collection + " saved");
            returnLink = linkSource.createPageRenderLinkWithContext(ViewCollection.class,
                    collection.getId());

        }
        return returnLink;
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