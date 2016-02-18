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

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.tapestry5.*;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.corelib.components.*;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.SelectModelFactory;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;
import org.apache.tapestry5.util.AbstractSelectModel;
import org.slf4j.Logger;
import se.unbound.tapestry.breadcrumbs.BreadCrumb;
import unidue.rc.dao.AlephDAO;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.HeadlineDAO;
import unidue.rc.dao.ReserveCollectionDAO;
import unidue.rc.model.*;
import unidue.rc.model.Book;
import unidue.rc.model.Headline;
import unidue.rc.model.web.BookRequest;
import unidue.rc.model.web.Conversations;
import unidue.rc.security.CollectionSecurityService;
import unidue.rc.system.BookUtils;
import unidue.rc.system.OpacFacadeService;
import unidue.rc.ui.ProtectedPage;
import unidue.rc.ui.SecurityContextPage;
import unidue.rc.ui.pages.collection.ViewCollection;
import unidue.rc.ui.valueencoder.BaseValueEncoder;
import unidue.rc.workflow.BookService;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Nils Verheyen
 * @since 16.09.13 14:43
 */
@Import(library = {
        "context:vendor/spin.min.js",
        "context:js/create.book.js"
})
@BreadCrumb(titleKey = "new.book")
@ProtectedPage
public class CreateBook implements SecurityContextPage {

    private static final String WIZARD_CONVERSATION_PREFIX = "bookWizard";
    private static final String BOOK_KEY = "book";

    public enum Step {
        BOOKSEARCH, CHOOSE_BOOK, SELECT_LIBRARY_ITEM
    }

    @Inject
    private Logger log;

    @Inject
    private Request request;

    @Inject
    private OpacFacadeService opacFacade;

    @Inject
    private ReserveCollectionDAO collectionDAO;

    @Inject
    private BookService bookService;

    @Inject
    @Service(HeadlineDAO.SERVICE_NAME)
    private HeadlineDAO headlineDAO;

    @Inject
    @Service(AlephDAO.SERVICE_NAME)
    private AlephDAO alephDAO;

    @Inject
    private AjaxResponseRenderer ajaxResponseRenderer;

    @Inject
    private PageRenderLinkSource linkSource;

    @Inject
    private ComponentResources resources;

    @Inject
    private Messages messages;

    @Inject
    private SelectModelFactory selectModelFactory;

    @Property(write = false)
    @Persist(PersistenceConstants.FLASH)
    private String message;

    @Property
    private ReserveCollection collection;

    @Property
    private BookRequest bookRequest;

    @Property
    private String position;

    private String conversationId = null;

    private Step step = null;

    @SessionState
    private Conversations<BookRequest> conversations;

    @Property
    private String bookSearch;

    @Property
    private OpacFacadeBook book;

    /**
     * Used by the ui to iterate over searched books.
     */

    @Property
    private String chosenBookDocNumber;

    @Property
    private OpacFacadeLibraryDataItem chosenLibraryItem;

    @Property
    private Headline headline;

    // choose library item

    // screen fields

    @Component(id = "bookForm")
    private Form bookForm;

    @Component(id = "bookSearch")
    private TextField bookSearchField;

    @Component(id = "chosenBookDocNumber")
    private RadioGroup chosenBookDocNumberGroup;

    @OnEvent(EventConstants.ACTIVATE)
    void onActivate(Integer rcId) {
        this.collection = collectionDAO.get(ReserveCollection.class, rcId);

        startConversition();
    }


    @OnEvent(EventConstants.ACTIVATE)
    void onActivate(Integer rcId, Step step, String conversationId) {
        this.step = step;
        this.conversationId = conversationId;
        this.collection = collectionDAO.get(ReserveCollection.class, rcId);

        startConversition();
    }

    /**
     * Starts the wizard if no conversation is given
     */
    private void startConversition() {
        if (step == null) {
            step = Step.BOOKSEARCH;

            conversationId = conversations.startConversation(WIZARD_CONVERSATION_PREFIX);
            bookRequest = new BookRequest();
            bookRequest.addBreadcrumb(messages.get("book.breadcrumb.search"));
            saveBookRequestToConversation(bookRequest);
        }
    }

    @OnEvent(EventConstants.PASSIVATE)
    Object[] onPassivate() {
        return new Object[]{collection.getId(), step, conversationId};
    }

    @OnEvent(component = "new_search")
    Link onNewSearch() {
        endConversation();
        return linkSource.createPageRenderLinkWithContext(CreateBook.class, collection.getId());
    }

    @OnEvent(EventConstants.PREPARE)
    void onPrepare() {
        if (bookRequest == null) {
            // Get objects for the form fields to overlay.
            bookRequest = restoreCreditRequestFromConversation();
        }
    }

    @OnEvent(value = EventConstants.VALIDATE, component = "bookForm")
    Object onValidateFromBookForm() {
        if (bookForm.getHasErrors())
            return this;

        saveBookRequestToConversation(bookRequest);

        switch (step) {
            case BOOKSEARCH:
                onBookSearch();
                break;
            case CHOOSE_BOOK:
                onChooseBook();
                break;
            case SELECT_LIBRARY_ITEM:
                return onSelectLibraryItem();
        }
        return this;
    }

    private void onBookSearch() {
        // validate
        if (StringUtils.isEmpty(bookSearch)) {
            bookForm.recordError(bookSearchField, messages.get("search-required-message"));
            return;
        }

        // success
        bookRequest.setSearchWord(bookSearch);

        // load books an put them to session for later submission
        OpacFacadeFind searchResult = opacFacade.search(bookSearch);
        if (searchResult != null && searchResult.containsBooks()) {
            bookRequest.setAvailableBooks(searchResult.getBooks());
            step = Step.CHOOSE_BOOK;
            bookRequest.addBreadcrumb(messages.get("book.breadcrumb.choose.book"));
        } else {
            bookForm.recordError(messages.get("error.msg.search.no.results"));
        }
    }

    private void onChooseBook() {
        // validate
        if (StringUtils.isEmpty(chosenBookDocNumber)) {
            bookForm.recordError(chosenBookDocNumberGroup, messages.get("error.msg.book.required"));
            return;
        }

        // create mapping of opac facade book to library data items
        bookRequest.setChosenBook(opacFacade.getDetails(chosenBookDocNumber));
        OpacFacadeLibraryData libraryData = opacFacade.getLibraryData(chosenBookDocNumber);
        if (libraryData != null) {

            bookRequest.setLibraryItems(libraryData.getItems());
            bookRequest.reduceItemsByLocation();
        }

        step = Step.SELECT_LIBRARY_ITEM;
        bookRequest.addBreadcrumb(messages.get("book.breadcrumb.choose.item"));
    }

    private Link onSelectLibraryItem() {

        if (chosenLibraryItem == null) {
            bookForm.recordError(messages.get("error.msg.book.item.required"));
            return null;
        }
        bookRequest.setChosenItem(chosenLibraryItem);

        Book book = createBook(bookRequest.getChosenItem());
        endConversation();
        return getBookLink(book);
    }

    /**
     * Returns the link to target book inside the view of a location.
     *
     * @param book target {@link Book} to show, may be null
     * @return book entry link if given book is not null, <code>null</code> otherwise
     */
    private Link getBookLink(Book book) {
        if (book != null) {

            bookRequest.setAvailableBooks(null);
            bookRequest.setLibraryItems(null);

            Link viewCollectionLink = linkSource.createPageRenderLinkWithContext(ViewCollection.class,
                    collection.getId());
            viewCollectionLink.setAnchor(book.getEntry().getId().toString());
            return viewCollectionLink;
        }
        return null;
    }

    private Book createBook(OpacFacadeLibraryDataItem libraryItem) {

        String signature = libraryItem.getSignature();
        Book newBook = new Book();
        newBook.setThumbnailURL(bookRequest.getChosenBook().getThumbnailURL());

        // store book in backend
        try {
            alephDAO.setBookData(newBook, signature);
            bookService.create(newBook, collection);
            if (headline != null)
                headlineDAO.move(newBook.getEntry(), headline);
            return newBook;
        } catch (CommitException e) {
            log.error("could not commit book " + newBook, e);
            bookForm.recordError(messages.format("error.msg.could.not.commit.book", newBook));
        } catch (SQLException e) {
            log.error("could not set book data of library item " + libraryItem, e);
            bookForm.recordError(messages.format("error.msg.could.not.commit.book", newBook));
        }
        return null;
    }

    private void saveBookRequestToConversation(BookRequest bookRequest) {
        conversations.saveToConversation(conversationId, BOOK_KEY, bookRequest);
    }

    private BookRequest restoreCreditRequestFromConversation() {
        return conversations.restoreFromConversation(conversationId, BOOK_KEY);
    }

    private void endConversation() {
        step = null;
        conversations.endConversation(conversationId);

        // If conversations SSO is now empty then remove it from the session
        if (conversations.isEmpty())
            conversations = null;
    }

    public ValueEncoder<OpacFacadeBook> getOpacBookEncoder() {
        return new ValueEncoder<OpacFacadeBook>() {
            @Override
            public String toClient(OpacFacadeBook value) {
                return value.getDocNumber();
            }

            @Override
            public OpacFacadeBook toValue(String docNumber) {
                List<OpacFacadeBook> books = bookRequest.getAvailableBooks();
                Optional<OpacFacadeBook> optionalBook = books.stream()
                        .filter(book -> docNumber.equals(book.getDocNumber()))
                        .findFirst();
                return optionalBook.isPresent() ? optionalBook.get() : null;
            }
        };
    }

    public ValueEncoder<OpacFacadeLibraryDataItem> getLibraryItemEncoder() {
        return new ValueEncoder<OpacFacadeLibraryDataItem>() {
            @Override
            public String toClient(OpacFacadeLibraryDataItem value) {
                return value.getBarcode();
            }

            @Override
            public OpacFacadeLibraryDataItem toValue(String clientValue) {
                List<OpacFacadeLibraryDataItem> libraryItems = bookRequest.getLibraryItems();
                Optional<OpacFacadeLibraryDataItem> item = libraryItems.stream()
                        .filter(libraryItem -> clientValue.equals(libraryItem.getBarcode()))
                        .findAny();
                return item.isPresent() ? item.get() : null;
            }
        };
    }

    public ValueEncoder<Headline> getHeadlineEncoder() {
        return new BaseValueEncoder<>(Headline.class, headlineDAO);
    }

    public SelectModel getLibraryItemModel() {
        List<OpacFacadeLibraryDataItem> libraryItems = bookRequest.getLibraryItems();
        return libraryItems == null || libraryItems.isEmpty()
                ? selectModelFactory.create(Collections.EMPTY_LIST, "")
                : new LibraryItemSelectModel(libraryItems);
    }

    private static class LibraryItemSelectModel extends AbstractSelectModel {

        private final List<OpacFacadeLibraryDataItem> items;

        public LibraryItemSelectModel(List<OpacFacadeLibraryDataItem> items) {
            this.items = items;
        }

        @Override
        public List<OptionGroupModel> getOptionGroups() {
            return null;
        }

        @Override
        public List<OptionModel> getOptions() {
            return items.stream()
                    .map(item -> new OptionModelImpl(getLabel(item), item))
                    .collect(Collectors.toList());
        }

        private String getLabel(OpacFacadeLibraryDataItem item) {
            return String.format("%s / %s", item.getLocation(), item.getSignature());
        }
    }

    public boolean isInSearch() {
        return step == Step.BOOKSEARCH;
    }

    public boolean isInChooseBook() {
        return step == Step.CHOOSE_BOOK;
    }

    public boolean isInSelectItem() {
        return step == Step.SELECT_LIBRARY_ITEM;
    }

    @Override
    public void checkPermission(CollectionSecurityService securityService, EventContext activationContext) throws AuthorizationException {
        Integer rcID = activationContext.get(Integer.class, 0);
        securityService.checkPermission(ActionDefinition.EDIT_ENTRIES, rcID);
    }
}
