package unidue.rc.ui.pages.entry;

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


import org.apache.shiro.authz.AuthorizationException;
import org.apache.tapestry5.*;
import org.apache.tapestry5.annotations.*;
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
import unidue.rc.ui.SecurityContextPage;
import unidue.rc.ui.ProtectedPage;
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
 * Within this page a BookChapter can be added to an existing  {@link unidue.rc.model.ReserveCollection}.
 *
 * @author Marcus Koesters
 */
@BreadCrumb(titleKey = "edit.book.chapter")
@ProtectedPage
public class EditBookChapter implements SecurityContextPage {

    @Inject
    private Logger log;

    @InjectPage
    private ViewCollection view;

    @Inject
    @Service(HeadlineDAO.SERVICE_NAME)
    private HeadlineDAO headlineDAO;

    @Property
    private Headline headline;

    @Property
    @Persist
    private List<UploadedFile> uploads;

    @Property
    private boolean deleteFile;

    private String fullTextURL;

    @Property
    private CopyrightReviewStatus copyrightStatus;


    /*
        * The {@link ReserveCollection} edited within this page. The {@link Persist} annotation has to be present
        * here, so
        * the object is page persisted.
        *
        * @see <a href="http://tapestry.apache.org/persistent-page-data.html">Persistent Page Data</a>
        */
    @Property
    @Persist
    private ReserveCollection collection;

    @Inject
    private PageRenderLinkSource linkSource;

    @Inject
    @Service(BaseDAO.SERVICE_NAME)
    private BaseDAO baseDAO;

    @Inject
    private ScannableService scannableService;

    @Inject
    private ResourceService resourceService;

    @Property
    @Persist
    private BookChapter chapter;

    @Component(id = "edit_bookchapter_form")
    private Form form;

    @Inject
    private Messages messages;

    // Screen fields

    @Property(write = false)
    @Persist(PersistenceConstants.FLASH)
    private String message;

    @SetupRender
    void onSetupRender() {
        this.uploads = new ArrayList<>();
    }

    @OnEvent(EventConstants.ACTIVATE)
    void onActivate(Integer entryId) {
        log.info("loading entry " + entryId);

        chapter = baseDAO.get(BookChapter.class, entryId);
        collection = chapter.getReserveCollection();
        headline = chapter.getEntry().getAssignedHeadline();

        Resource resource = chapter.getResource();
        copyrightStatus = resource != null ? resource.getCopyrightReviewStatus() : null;
    }

    /**
     * Called when the link for this page is generated.
     *
     * @return
     * @see {@link org.apache.tapestry5.EventConstants#PASSIVATE}
     */
    @OnEvent(EventConstants.PASSIVATE)
    Object onPassivate() {
        return chapter.getId();
    }

    void onValidateFromChapterurl(String value) throws ValidationException {

        if (value == null) return;
        try {
            URL test = new URL(value);
        } catch (MalformedURLException e) {

            throw new ValidationException(e.getLocalizedMessage());
        }

    }

    @OnEvent(value = EventConstants.SUCCESS, component = "edit_bookchapter_form")
    Object onSuccess() {
        try {
            if (deleteFile)
                scannableService.deleteFile(chapter);

            scannableService.update(chapter, fullTextURL);
            if (uploads != null && !uploads.isEmpty()) {
                UploadedFile uploadedFile = uploads.get(0);
                scannableService.update(chapter, uploadedFile.getFileName(), uploadedFile.getStream());
                uploads = null;
            }
        } catch (IOException e) {
            form.recordError(messages.format("error.msg.could.not.save.file", chapter));
        } catch (CommitException e) {
            form.recordError(messages.format("error.msg.could.not.commit.chapter", chapter));
        }
        Resource resource = chapter.getResource();
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
                headlineDAO.move(chapter.getEntry(), headline);
        } catch (CommitException e) {
            form.recordError(messages.format("error.msg.could.not.commit.chapter", chapter));
        }

        Link returnLink = null;
        if (!form.getHasErrors()) {

            log.info("bookchapter entry for " + collection + " saved");
            returnLink = linkSource.createPageRenderLinkWithContext(ViewCollection.class,
                    collection.getId());

        }
        return returnLink;

    }

    @Override
    public void checkPermission(CollectionSecurityService securityService, EventContext activationContext) throws AuthorizationException {
        Integer chapterID = activationContext.get(Integer.class, 0);
        BookChapter bookChapter = baseDAO.get(BookChapter.class, chapterID);
        securityService.checkPermission(ActionDefinition.EDIT_ENTRIES, bookChapter.getEntry().getReserveCollection().getId());
    }

    public String getFullTextURL() {
        return chapter.getResource() != null ? chapter.getResource().getFullTextURL() : null;
    }

    public void setFullTextURL(String fullTextURL) {
        this.fullTextURL = fullTextURL;
    }
}