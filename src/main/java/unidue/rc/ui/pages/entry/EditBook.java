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
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.annotations.*;
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
import unidue.rc.ui.pages.collection.ViewCollection;
import unidue.rc.workflow.BookService;
import unidue.rc.workflow.EntryService;

/**
 * Created by nils on 29.07.15.
 */
@BreadCrumb(titleKey = "edit.book")
@ProtectedPage
public class EditBook implements SecurityContextPage {

    @Inject
    private Logger log;

    @Inject
    @Service(BaseDAO.SERVICE_NAME)
    private BaseDAO baseDAO;

    @Inject
    @Service(HeadlineDAO.SERVICE_NAME)
    private HeadlineDAO headlineDAO;

    @Inject
    private BookService bookService;

    @Inject
    private CollectionSecurityService securityService;

    @Inject
    private PageRenderLinkSource linkSource;

    @Inject
    private Messages messages;

    @Component(id = "book_form")
    private Form form;

    @Property
    private ReserveCollection collection;

    @Property
    private Book book;

    @Property
    private String fullTextURL;

    @Property
    private Headline headline;

    @Property
    @Persist(PersistenceConstants.FLASH)
    private boolean isReadOnly;

    @OnEvent(EventConstants.ACTIVATE)
    void onActivate(Integer entryId) {

        log.info("loading entry " + entryId);
        book = baseDAO.get(Book.class, entryId);
        collection = book.getEntry().getReserveCollection();
        headline = book.getEntry().getAssignedHeadline();
        fullTextURL = book.getResource() != null ? book.getResource().getFullTextURL() : null;

        isReadOnly = !securityService.isPermitted(ActionDefinition.EDIT_BOOK_META_DATA);
    }

    @OnEvent(EventConstants.PASSIVATE)
    Integer onPassivate() {
        return book.getId();
    }

    @OnEvent(EventConstants.SUCCESS)
    Object onBookSubmitted() {

        try {
            bookService.update(book, fullTextURL);
            if (headline != null)
                headlineDAO.move(book.getEntry(), headline);
            Link viewCollectionLink = linkSource.createPageRenderLinkWithContext(ViewCollection.class,
                    collection.getId());

            return viewCollectionLink;
        } catch (CommitException e) {
            form.recordError(messages.format("error.msg.could.not.commit.book", book));
            return null;
        }
    }

    @Override
    public void checkPermission(CollectionSecurityService securityService, EventContext activationContext) throws AuthorizationException {
        Integer bookID = activationContext.get(Integer.class, 0);
        Book book = baseDAO.get(Book.class, bookID);
        securityService.checkPermission(ActionDefinition.EDIT_ENTRIES, book.getEntry().getReserveCollection().getId());
    }
}
