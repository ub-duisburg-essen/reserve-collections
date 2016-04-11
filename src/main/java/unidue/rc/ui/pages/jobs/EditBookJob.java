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
package unidue.rc.ui.pages.jobs;


import miless.model.User;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.Service;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.TextArea;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.slf4j.Logger;
import se.unbound.tapestry.breadcrumbs.BreadCrumb;
import se.unbound.tapestry.breadcrumbs.BreadCrumbList;
import unidue.rc.dao.BookJobDAO;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.DeleteException;
import unidue.rc.dao.EntryDAO;
import unidue.rc.dao.LibraryLocationDAO;
import unidue.rc.dao.UserDAO;
import unidue.rc.model.ActionDefinition;
import unidue.rc.model.BookJob;
import unidue.rc.model.JobComment;
import unidue.rc.model.LibraryLocation;
import unidue.rc.security.CollectionSecurityService;
import unidue.rc.security.RequiresActionPermission;
import unidue.rc.ui.ProtectedPage;
import unidue.rc.ui.selectmodel.LibraryLocationListSelectModel;
import unidue.rc.ui.valueencoder.LibraryLocationValueEncoder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Nils Verheyen
 * @since 18.12.13 13:20
 */
@BreadCrumb(titleKey = "edit.book.job")
@ProtectedPage
public class EditBookJob {

    private static final DateFormat MODIFICATION_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm");

    @Inject
    private Logger log;

    @Inject
    private BookJobDAO bookJobDAO;

    @Inject
    @Service(EntryDAO.SERVICE_NAME)
    private EntryDAO entryDAO;

    @Inject
    private UserDAO userDAO;

    @Inject
    private Messages messages;

    @Property
    private BookJob bookJob;

    @Property
    private String comment;

    private List<User> annotators;

    @SessionState
    private BreadCrumbList breadCrumbList;

    @Inject
    private LibraryLocationDAO locationDAO;

    @Inject
    private PageRenderLinkSource linkSource;

    @Component(id = "book_job_form")
    private Form bookJobForm;

    @Component(id = "comment")
    private TextArea commentField;

    @Inject
    private CollectionSecurityService securityService;

    @SetupRender
    @RequiresActionPermission(ActionDefinition.EDIT_BOOK_JOBS)
    public void beginRender() {
        breadCrumbList.getLastCrumb().setTitle(messages.format("edit.book.job", bookJob.getId()));
    }

    @OnEvent(value = EventConstants.ACTIVATE)
    void onActivate(Integer bookJobID) {
        this.bookJob = bookJobDAO.getJob(bookJobID);

        List<Integer> annotatorIDs = new ArrayList<>(bookJob.getComments().size());
        for (JobComment comment : bookJob.getComments())
            annotatorIDs.add(comment.getAuthorId());
        this.annotators = userDAO.getUsers(annotatorIDs);
    }

    @OnEvent(value = EventConstants.PASSIVATE)
    Integer onPassivate() {
        return bookJob.getId();
    }

    @OnEvent(value = EventConstants.VALIDATE, component = "book_job_form")
    void onValidateArticleBookJob() {

        User loggedInUser = securityService.getCurrentUser();
        JobComment newComment = null;
        if (comment != null && !comment.isEmpty()) {
            newComment = new JobComment();
            newComment.setComment(comment);
            newComment.setAuthorId(loggedInUser.getUserid());
            newComment.setDate(new Date());
            try {
                entryDAO.create(newComment);
            } catch (CommitException e) {
                bookJobForm.recordError(commentField, messages.format("error.msg.could.not.commit.comment",
                        comment));
            }
        }

        try {
            if (newComment != null)
                bookJob.addToComments(newComment);
            bookJobDAO.update(bookJob);
        } catch (CommitException e) {
            bookJobForm.recordError(messages.format("error.msg.could.not.commit.bookjob",
                    bookJob.getBook().getTitle()));
        }
        try {
            entryDAO.update(bookJob.getBook());
        } catch (CommitException e) {
            bookJobForm.recordError(messages.format("error.msg.could.not.commit.journal",
                    bookJob.getBook().getTitle()));
        }
    }

    @OnEvent(value = EventConstants.SUCCESS)
    Object onSuccess() {
        return linkSource.createPageRenderLinkWithContext(ViewBookJob.class, bookJob.getId());
    }

    @OnEvent(component = "deleteBookJob")
    Object onDeleteBookJob() {
        try {
            bookJobDAO.delete(bookJob);
            return BookJobs.class;
        } catch (DeleteException e) {
            return this;
        }
    }

    public SelectModel getLibraryLocationSelectModel() {
        return new LibraryLocationListSelectModel(locationDAO);
    }

    public ValueEncoder<LibraryLocation> getLibraryLocationEncoder() {
        return new LibraryLocationValueEncoder(locationDAO);
    }

    public String getModified() {
        return MODIFICATION_DATE_FORMAT.format(bookJob.getModified());
    }


}
