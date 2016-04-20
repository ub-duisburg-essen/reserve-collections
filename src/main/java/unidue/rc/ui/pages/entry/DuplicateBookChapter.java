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
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.Service;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.slf4j.Logger;
import se.unbound.tapestry.breadcrumbs.BreadCrumb;
import unidue.rc.dao.BaseDAO;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.HeadlineDAO;
import unidue.rc.model.*;
import unidue.rc.security.CollectionSecurityService;
import unidue.rc.ui.ProtectedPage;
import unidue.rc.ui.SecurityContextPage;
import unidue.rc.ui.components.BookChapterFormFragment;
import unidue.rc.ui.pages.collection.ViewCollection;
import unidue.rc.workflow.ScannableService;

/**
 * Created by nils on 31.07.15.
 */
@ProtectedPage
@BreadCrumb(titleKey = "duplicate.book.chapter")
public class DuplicateBookChapter implements SecurityContextPage {

    @Inject
    private Logger log;

    @Inject
    private ScannableService scannableService;

    @Inject
    @Service(BaseDAO.SERVICE_NAME)
    private BaseDAO baseDAO;

    @Inject
    @Service(HeadlineDAO.SERVICE_NAME)
    private HeadlineDAO headlineDAO;

    @Inject
    private PageRenderLinkSource linkSource;

    @Inject
    private Messages messages;

    @Property
    @Component(id = "chapterForm")
    private Form chapterForm;

    @Property
    @Component(id = "chapterMeta")
    private BookChapterFormFragment chapterFormFragment;

    private BookChapter source;

    @Property(write = false)
    private ReserveCollection collection;

    @Property
    private Headline headline;

    @Property(write = false)
    private BookChapter duplicate;

    @OnEvent(EventConstants.ACTIVATE)
    void onActivate(Integer chapterID) {
        source = baseDAO.get(BookChapter.class, chapterID);
        collection = source.getReserveCollection();
        duplicate = scannableService.duplicate(this.source, collection);
    }

    @OnEvent(EventConstants.PASSIVATE)
    Integer onPassivate() {
        return source.getId();
    }

    @OnEvent(value = EventConstants.VALIDATE, component = "chapterForm")
    void onValidate() {
        chapterFormFragment.validate();
        if (chapterForm.getHasErrors())
            return;

        try {
            Resource resource = source.getResource();
            if (resource != null && resource.getFullTextURL() != null) {
                scannableService.create(duplicate, collection, resource.getFullTextURL());
            } else {
                scannableService.create(duplicate, collection);
            }
        } catch (CommitException e) {
            log.error("could not create chapter " + duplicate, e);
            chapterForm.recordError(messages.format("error.msg.could.not.commit.chapter", duplicate.getWorkTitle()));
        }
        Headline headline = chapterFormFragment.getHeadline();
        if (headline != null)
            try {
                headlineDAO.move(duplicate.getEntry(), headline);
            } catch (CommitException e) {
                log.error("could not move chapter " + duplicate, e);
                chapterForm.recordError(messages.format("error.msg.could.not.commit.chapter", duplicate.getWorkTitle()));
            }

    }

    @OnEvent(EventConstants.SUCCESS)
    Object onSuccess() {
        return linkSource.createPageRenderLinkWithContext(ViewCollection.class, collection.getId());
    }

    @Override
    public void checkPermission(CollectionSecurityService securityService, EventContext activationContext) throws AuthorizationException {
        Integer chapterID = activationContext.get(Integer.class, 0);
        BookChapter bookChapter = baseDAO.get(BookChapter.class, chapterID);
        securityService.checkPermission(ActionDefinition.EDIT_ENTRIES, bookChapter.getEntry().getReserveCollection().getId());
    }
}
