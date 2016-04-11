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



import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.Service;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.upload.services.UploadedFile;
import org.slf4j.Logger;
import se.unbound.tapestry.breadcrumbs.BreadCrumb;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.EntryDAO;
import unidue.rc.dao.HeadlineDAO;
import unidue.rc.dao.ReserveCollectionDAO;
import unidue.rc.model.ActionDefinition;
import unidue.rc.model.Headline;
import unidue.rc.model.ReserveCollection;
import unidue.rc.security.RequiresActionPermission;
import unidue.rc.ui.ProtectedPage;
import unidue.rc.ui.pages.collection.ViewCollection;
import unidue.rc.workflow.ResourceService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Nils Verheyen
 */
@BreadCrumb(titleKey = "new.file")
@ProtectedPage
public class CreateFile {

    @Inject
    private Logger log;

    @Inject
    private PageRenderLinkSource linkSource;

    @Inject
    private Request request;

    @Property
    @Persist
    @Validate("required")
    private List<UploadedFile> uploads;

    @Property
    private Headline headline;

    @Property
    private String description;

    /**
     * The {@link ReserveCollection} edited within this page. The {@link Persist} annotation has to be present here, so
     * the object is page persisted.
     *
     * @see <a href="http://tapestry.apache.org/persistent-page-data.html">Persistent Page Data</a>
     */
    @Property
    private ReserveCollection collection;

    @Inject
    @Service(EntryDAO.SERVICE_NAME)
    private EntryDAO entryDAO;

    @Inject
    private ResourceService resourceService;

    @Inject
    @Service(HeadlineDAO.SERVICE_NAME)
    private HeadlineDAO headlineDAO;

    @Inject
    private ReserveCollectionDAO collectionDAO;

    @Inject
    private Messages messages;

    @Inject
    private ComponentResources resources;

    @Component(id = "fileForm")
    private Form fileForm;

    @InjectPage
    private EditFiles editFilesPage;

    public String getTitle() {
        return collection.getTitle();
    }

    @OnEvent(EventConstants.ACTIVATE)
    @RequiresActionPermission(ActionDefinition.EDIT_ENTRIES)
    void onActivate(Integer rcId) {
        this.collection = collectionDAO.get(ReserveCollection.class, rcId);
    }

    @OnEvent(value = EventConstants.VALIDATE, component = "fileForm")
    Object onFileValidate() {
        if (fileForm.getHasErrors())
            return linkSource.createPageRenderLinkWithContext(CreateFile.class, collection.getId());
        return null;
    }

    @OnEvent(value = "gotoEdit")
    Object onGotoEdit() {
        submitFiles();

        // redirect to edit files page if everything went well
        return fileForm.getHasErrors()
                ? null
                : editFilesPage;
    }

    @OnEvent(value = EventConstants.SUCCESS, component = "fileForm")
    Object onFileSubmitted() {
        submitFiles();

        return fileForm.getHasErrors()
                ? null
                : linkSource.createPageRenderLinkWithContext(ViewCollection.class, collection.getId());
    }

    /**
     * Stores submitted files from {@link #fileForm} on storage device and creates new {@link unidue.rc.model.File}
     * entry in backend. Therefore this method should only be used after the file form was submitted.
     */
    private void submitFiles() {

        List<unidue.rc.model.File> createdFiles = new ArrayList<>();
        for (UploadedFile uploadedFile : uploads) {
            log.info("uploaded file " + uploadedFile.getFilePath());


            try {
                // createResource file entry
                unidue.rc.model.File file = new unidue.rc.model.File();
                entryDAO.createEntry(file, collection);

                resourceService.create(uploadedFile.getFileName(), uploadedFile.getStream(), file);
                log.debug("created file " + file);

                if (headline != null)
                    headlineDAO.move(file.getEntry(), headline);

                createdFiles.add(file);
            } catch (IOException | CommitException e) {
                fileForm.recordError(messages.format("error.msg.could.not.commit.file", uploadedFile.getFileName()));
            }
        }

        if (!fileForm.getHasErrors()) {
            editFilesPage.setFiles(createdFiles);
            editFilesPage.setCollectionId(collection.getId());
            resources.discardPersistentFieldChanges();
        }

        uploads = null;
    }

    /**
     * Called when the link for this page is generated.
     *
     * @return
     * @see {@link EventConstants#PASSIVATE}
     */
    @OnEvent(EventConstants.PASSIVATE)
    Object onPassivate() {
        return collection.getId();
    }
}