package unidue.rc.ui.pages.jobs;

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
import unidue.rc.dao.ScanJobDAO;
import unidue.rc.dao.UserDAO;
import unidue.rc.model.*;
import unidue.rc.security.RequiresActionPermission;
import unidue.rc.ui.ProtectedPage;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Nils Verheyen
 * @since 17.12.13 08:11
 */
@BreadCrumb(titleKey = "view.scan.job.page.title")
@ProtectedPage
public class ViewScanJob {

    private static final DateFormat COMMENT_DATE_FORMAT = new SimpleDateFormat("dd.MM.yy HH:mm");

    @Inject
    private Logger log;

    @Inject
    private ScanJobDAO scanJobDAO;

    @Inject
    private UserDAO userDAO;

    @Inject
    private Messages messages;

    @Property
    private ScanJob scanJob;

    @Property
    private JobComment comment;

    private List<User> annotators;

    @SessionState
    private BreadCrumbList breadCrumbList;

    @Property
    private LibraryItem libraryItem;

    @SetupRender
    @RequiresActionPermission(ActionDefinition.EDIT_SCAN_JOBS)
    public void beginRender() {
        breadCrumbList.getLastCrumb().setTitle(messages.format("view.scan.job", scanJob.getId()));
    }

    @OnEvent(value = EventConstants.ACTIVATE)
    void onActivate(Integer scanJobID) {
        this.scanJob = scanJobDAO.get(ScanJob.class, scanJobID);
        this.libraryItem = scanJobDAO.getLibraryItem(scanJob);

        List<Integer> annotatorIDs = new ArrayList<>(scanJob.getComments().size());
        for (JobComment comment : scanJob.getComments())
            annotatorIDs.add(comment.getAuthorId());
        this.annotators = userDAO.getUsers(annotatorIDs);
    }

    @OnEvent(value = EventConstants.PASSIVATE)
    Integer onPassivate() {
        return scanJob.getId();
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

    public String getBarcodeContent() {
        return libraryItem.getReserveCollection().getId() + "_" + libraryItem.getId();
    }

    public Boolean getIsJournalArticle() {
        return libraryItem instanceof JournalArticle;
    }

    public Boolean getIsBookChapter() {
        return libraryItem instanceof BookChapter;
    }

    public BookChapter getBookChapter() {
        return (BookChapter) libraryItem;
    }

    public JournalArticle getJournalArticle() {
        return (JournalArticle) libraryItem;
    }

}
