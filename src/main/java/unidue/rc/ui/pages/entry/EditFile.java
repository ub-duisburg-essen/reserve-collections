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
import org.apache.tapestry5.*;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.Service;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.SelectModelFactory;
import org.apache.tapestry5.upload.services.UploadedFile;
import org.slf4j.Logger;
import se.unbound.tapestry.breadcrumbs.BreadCrumb;
import se.unbound.tapestry.breadcrumbs.BreadCrumbList;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.HeadlineDAO;
import unidue.rc.dao.ResourceDAO;
import unidue.rc.model.*;
import unidue.rc.security.CollectionSecurityService;
import unidue.rc.ui.ProtectedPage;
import unidue.rc.ui.SecurityContextPage;
import unidue.rc.ui.pages.collection.ViewCollection;
import unidue.rc.workflow.EntryService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Within this page an existing {@link unidue.rc.model.File} can be edited.
 *
 * @author Marcus Koesters
 */
@BreadCrumb(titleKey = "edit.file")
@ProtectedPage
public class EditFile implements SecurityContextPage {

    @Inject
    private Logger log;

    @Inject
    private PageRenderLinkSource linkSource;

    @Property
    private unidue.rc.model.File file;

    @Property
    private ReserveCollection collection;

    @Property
    @Persist
    private List<UploadedFile> uploads;

    @Inject
    @Service(ResourceDAO.SERVICE_NAME)
    private ResourceDAO resourceDAO;

    @Inject
    @Service(HeadlineDAO.SERVICE_NAME)
    private HeadlineDAO headlineDAO;

    @Property
    private Headline headline;

    @Property
    private CopyrightReviewStatus copyrightStatus;

    @Inject
    private Messages messages;

    @Inject
    private ComponentResources resources;

    @Component(id = "file_form")
    private Form form;

    @SessionState
    private BreadCrumbList breadCrumbList;

    @SetupRender
    public void beginRender() {
        breadCrumbList.getLastCrumb().setTitle(messages.format("edit.file", file.getResource().getFileName()));
        uploads = new ArrayList<>();
    }

    @OnEvent(EventConstants.ACTIVATE)
    void onActivate(Integer entryId) {
        log.info("loading entry " + entryId);

        file = resourceDAO.get(unidue.rc.model.File.class, entryId);
        collection = file.getEntry().getReserveCollection();
        copyrightStatus = file.getResource().getCopyrightReviewStatus();
        headline = file.getEntry().getAssignedHeadline();

    }

    @OnEvent(EventConstants.SUCCESS)
    Object onFileSubmitted() {

        try {

            // replace file if necessary
            if (uploads != null && uploads.size() == 1)
                replaceFile();

            Resource resource = file.getResource();
            resource.setCopyrightReviewStatus(copyrightStatus);
            resourceDAO.update(resource);

            resourceDAO.update(file);
            log.info("file entry for " + collection + " saved");
            if (headline != null)
                headlineDAO.move(file.getEntry(), headline);
            Link viewCollectionLink = linkSource.createPageRenderLinkWithContext(ViewCollection.class,
                    collection.getId());

            return viewCollectionLink;
        } catch (CommitException | IOException e) {
            log.debug(e.getMessage());
            form.recordError(messages.format("error.msg.could.not.commit.file", file));
            return null;

        } finally {

            uploads = null;
            resources.discardPersistentFieldChanges();
        }
    }

    private void replaceFile() throws IOException, CommitException {

        UploadedFile uploadedFile = uploads.get(0);

        // write new file
        File outputFile = resourceDAO.createOutputFile(collection.getId(), file.getId(), uploadedFile.getFileName());
        uploadedFile.write(outputFile);
        log.debug("file " + uploadedFile.getFileName() + " written to " + outputFile.getAbsolutePath());

        // delete old file
        resourceDAO.deleteFile(file.getResource());

        // update entry with mime and path
        Resource resource = file.getResource();
        resource.setMimeType(resourceDAO.detectMimeType(outputFile));
        resource.setFilePath(resourceDAO.buildFilePath(collection.getId(), file.getId(), uploadedFile.getFileName()));
        resourceDAO.update(resource);

    }

    /**
     * Called when the link for this page is generated.
     *
     * @return
     * @see {@link org.apache.tapestry5.EventConstants#PASSIVATE}
     */
    @OnEvent(EventConstants.PASSIVATE)
    Integer onPassivate() {
        return file.getId();
    }


    @Override
    public void checkPermission(CollectionSecurityService securityService, EventContext activationContext) throws AuthorizationException {
        Integer fileID = activationContext.get(Integer.class, 0);
        unidue.rc.model.File file = resourceDAO.get(unidue.rc.model.File.class, fileID);
        securityService.checkPermission(ActionDefinition.EDIT_ENTRIES, file.getEntry().getReserveCollection().getId());
    }

}