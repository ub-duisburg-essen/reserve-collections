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


import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.Service;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.slf4j.Logger;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.EntryDAO;
import unidue.rc.dao.HeadlineDAO;
import unidue.rc.dao.ReserveCollectionDAO;
import unidue.rc.model.ActionDefinition;
import unidue.rc.model.File;
import unidue.rc.model.Headline;
import unidue.rc.model.ReserveCollection;
import unidue.rc.security.RequiresActionPermission;
import unidue.rc.ui.ProtectedPage;
import unidue.rc.ui.pages.collection.ViewCollection;
import unidue.rc.ui.valueencoder.BaseValueEncoder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Nils Verheyen
 * @since 26.11.13 13:59
 */
@ProtectedPage
public class EditFiles {

    @Inject
    private Logger log;

    // Screen fields
    @Inject
    @Service(HeadlineDAO.SERVICE_NAME)
    private HeadlineDAO headlineDAO;

    @Property
    private Headline headline;

    @Persist(PersistenceConstants.FLASH)
    private List<File> files;

    private File file;

    // Work fields

    private boolean inFormSubmission;

    private List<File> submittedFiles;

    // components and daos

    @Inject
    @Service(EntryDAO.SERVICE_NAME)
    private EntryDAO entryDAO;

    @Inject
    private ReserveCollectionDAO collectionDAO;

    @Inject
    private Messages messages;

    @Property
    private ReserveCollection collection;

    @Component(id = "files_form")
    private Form filesForm;

    @Inject
    private PageRenderLinkSource linkSource;

    private Integer collectionId;

    @OnEvent(EventConstants.ACTIVATE)
    @RequiresActionPermission(ActionDefinition.EDIT_ENTRIES)
    void onPageActivation(Integer collectionId) {
        inFormSubmission = false;
        this.collectionId = collectionId;
        this.collection = collectionDAO.get(ReserveCollection.class, collectionId);

        // refresh file as they are only kept in flash storage
        this.files = new ArrayList<>(files);
    }

    @OnEvent(EventConstants.PASSIVATE)
    Object onPagePassivate() {
        return collectionId;
    }

    @OnEvent(EventConstants.PREPARE_FOR_SUBMIT)
    void onPrepareForSubmit() {
        inFormSubmission = true;
        submittedFiles = new ArrayList<>();
    }

    @OnEvent(value = EventConstants.SUCCESS, component = "files_form")
    Object onSaveSubmittedFiles() {

        if (files == null) {
            filesForm.recordError(messages.get("error.msg.could.not.commit.file.descriptions"));
            return null;
        }

        for (File file : files) {
            log.debug("submitted file " + file);
            try {
                file.setModified(new Date());
                entryDAO.update(file);

            } catch (CommitException e) {
                log.error("could not commit file " + e.getMessage());
                filesForm.recordError(messages.format("error.msg.could.not.commit.file.description",
                        file.getResource().getFileName()));
            }
        }

        // go back to the view of a reserve collection if no errors occured
        if (filesForm.getHasErrors()) {
            setFiles(files);
            return null;
        } else {
            Link viewCollectionLink = linkSource.createPageRenderLinkWithContext(ViewCollection.class,
                    collection.getId());
            return viewCollectionLink;
        }
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;

        if (inFormSubmission)
            submittedFiles.add(file);
    }

    public BaseValueEncoder getEntryValueEncoder() {
        return new BaseValueEncoder(File.class, entryDAO);
    }

    public List<File> getFiles() {
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }

    public void setCollectionId(Integer collectionId) {
        this.collectionId = collectionId;
    }
}
