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
import unidue.rc.dao.ResourceDAO;
import unidue.rc.model.*;
import unidue.rc.security.CollectionSecurityService;
import unidue.rc.ui.SecurityContextPage;
import unidue.rc.ui.ProtectedPage;
import unidue.rc.ui.pages.collection.ViewCollection;
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

    @Inject
    @Service(HeadlineDAO.SERVICE_NAME)
    private HeadlineDAO headlineDAO;

    @Inject
    @Service(ResourceDAO.SERVICE_NAME)
    private ResourceDAO resourceDAO;

    @Property
    private Headline headline;

    @Property
    @Persist
    private List<UploadedFile> uploads;

    @Property
    private String filename;

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
        if (resource != null) {
            copyrightStatus = resource.getCopyrightReviewStatus();
            filename = resource.getFileName();
        }
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

    void onValidateFromFilename(String value) throws ValidationException {

        Resource resource = chapter.getResource();
        if (resource != null
                && StringUtils.isNotBlank(resource.getFileName())
                && StringUtils.isBlank(value))
            throw new ValidationException(messages.get("filename-required-message"));
    }

    @OnEvent(value = EventConstants.SUCCESS, component = "edit_bookchapter_form")
    Object onSuccess() {
        Resource resource = chapter.getResource();
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
            scannableService.update(chapter, fullTextURL);

            resource = chapter.getResource();
            if (resource != null) {
                resource.setCopyrightReviewStatus(copyrightStatus);
                resourceService.update(resource);
            }
        } catch (CommitException e) {
            form.recordError(messages.format("error.msg.could.not.commit.chapter", chapter));
        }

        try {
            if (headline != null)
                headlineDAO.move(chapter.getEntry(), headline);
        } catch (CommitException e) {
            form.recordError(messages.format("error.msg.could.not.commit.chapter", chapter));
        }

        Link returnLink = null;
        if (!form.getHasErrors()) {
            returnLink = linkSource.createPageRenderLinkWithContext(ViewCollection.class,
                    collection.getId());
            returnLink.setAnchor(chapter.getId().toString());

        }
        return returnLink;

    }

    private void updateResource(Resource resource) {
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
            scannableService.update(chapter, uploadedFile.getFileName(), uploadedFile.getStream());
            uploads = null;
        } catch (IOException e) {
            form.recordError(messages.format("error.msg.could.not.save.file", chapter));
        } catch (CommitException e) {
            form.recordError(messages.format("error.msg.could.not.commit.chapter", chapter));
        }
    }

    private void deleteFile() {

        try {
            scannableService.setFileDeleted(chapter);
        } catch (CommitException e) {
            form.recordError(messages.format("error.msg.could.not.commit.chapter", chapter));
        }
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