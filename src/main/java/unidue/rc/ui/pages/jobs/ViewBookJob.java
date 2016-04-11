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
import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;
import se.unbound.tapestry.breadcrumbs.BreadCrumb;
import se.unbound.tapestry.breadcrumbs.BreadCrumbList;
import unidue.rc.dao.BookJobDAO;
import unidue.rc.dao.OriginDAO;
import unidue.rc.dao.UserDAO;
import unidue.rc.model.ActionDefinition;
import unidue.rc.model.Book;
import unidue.rc.model.BookJob;
import unidue.rc.model.JobComment;
import unidue.rc.security.RequiresActionPermission;
import unidue.rc.ui.ProtectedPage;
import unidue.rc.workflow.CollectionService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * @author Nils Verheyen
 * @since 17.12.13 08:11
 */
@BreadCrumb(titleKey = "view.scan.job.page.title")
@ProtectedPage
public class ViewBookJob {

    private static final DateFormat COMMENT_DATE_FORMAT = new SimpleDateFormat("dd.MM.yy HH:mm");

    @Inject
    private Logger log;

    @Inject
    private BookJobDAO bookJobDAO;

    @Inject
    private UserDAO userDAO;

    @Inject
    private OriginDAO originDAO;

    @Inject
    private CollectionService collectionService;

    @Inject
    private Messages messages;

    @Inject
    private Locale locale;

    @Property
    private BookJob bookJob;

    @Property
    private JobComment comment;

    @Property
    private String docent;

    private List<User> annotators;

    @SessionState
    private BreadCrumbList breadCrumbList;

    @SetupRender
    @RequiresActionPermission(ActionDefinition.EDIT_BOOK_JOBS)
    public void beginRender() {
        breadCrumbList.getLastCrumb().setTitle(messages.format("view.book.job", bookJob.getId()));
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

    public String getAnnotatorNameForComment() {
        Optional<User> annotator = annotators.stream()
                .filter(user -> user.getUserid().equals(comment.getAuthorId()))
                .findAny();
        return annotator.isPresent() ? annotator.get().getRealname() : StringUtils.EMPTY;
    }

    public String getCommentDate() {
        return COMMENT_DATE_FORMAT.format(comment.getDate());
    }

    public Book getBook() {
        return bookJob.getBook();
    }

    public List<String> getDocents() {
        return collectionService.getDocents(bookJob.getBook().getReserveCollection());
    }

    public String getOriginLabel() {
        return originDAO.getOriginLabel(locale, bookJob.getBook().getReserveCollection().getOriginId());
    }

    public String getJobModificationDate() {
        return COMMENT_DATE_FORMAT.format(bookJob.getModified());
    }

    public String getLocalizedStatus() {
        return messages.get(bookJob.getStatus().name());
    }
}
