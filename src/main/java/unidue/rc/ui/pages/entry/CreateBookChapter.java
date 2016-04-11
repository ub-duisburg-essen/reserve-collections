package unidue.rc.ui.pages.entry;



import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.tapestry5.*;
import org.apache.tapestry5.annotations.*;
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
import unidue.rc.dao.HeadlineDAO;
import unidue.rc.dao.ReserveCollectionDAO;
import unidue.rc.model.*;
import unidue.rc.model.web.BookChapterWizardData;
import unidue.rc.security.CollectionSecurityService;
import unidue.rc.system.OpacFacadeService;
import unidue.rc.ui.ProtectedPage;
import unidue.rc.ui.SecurityContextPage;
import unidue.rc.ui.components.BookChapterFormFragment;
import unidue.rc.ui.pages.collection.ViewCollection;
import unidue.rc.ui.valueencoder.OpacFacadeBookValueEncoder;
import unidue.rc.workflow.ResourceService;
import unidue.rc.workflow.ScannableService;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

@Import(library = {
        "context:vendor/spin.min.js",

        "context:js/create.book.js"
}, stylesheet = {
        "context:css/create.book.css"
})
/**
 * Within this page a BookChapter can be added to an existing  {@link ReserveCollection}.
 *
 * @author Marcus Koesters
 */
@BreadCrumb(titleKey = "new.book.chapter")
@ProtectedPage
public class CreateBookChapter implements SecurityContextPage {

    public static final String WIZARD_CONVERSATION_PREFIX = "wiz";
    public static final String BOOK_CHAPTER_KEY = "BC";

    public enum SourceWizardChoice {
        CATALOGUE,
        FILE,
        URL,
        REFNO;

    }

    public enum Step {
        SELECT, BOOKSEARCH, EDIT, BOOKSELECT, UPLOADFILE, URL, REFNO
    }

    @Inject
    private Logger log;

    @InjectPage
    private ViewCollection view;

    @Property
    private ReserveCollection collection;

    @Inject
    private PageRenderLinkSource linkSource;

    @Inject
    private ScannableService scannableService;

    @Inject
    private ResourceService resourceService;

    @Inject
    private ReserveCollectionDAO collectionDAO;

    @Property
    private Headline headline;

    @Inject
    @Service(HeadlineDAO.SERVICE_NAME)
    private HeadlineDAO headlineDAO;

    @Property
    private OpacFacadeBook book;

    @Component(id = "chapterForm")
    @Property(write = false)
    private Form chapterForm;

    @Component(id = "chapterMeta")
    private BookChapterFormFragment chapterFormFragment;

    @Property
    @Persist(PersistenceConstants.FLASH)
    private SourceWizardChoice source;

    @Inject
    private Request request;

    @Inject
    private OpacFacadeService opacFacade;

    @Property
    @Validate("required")
    private String bookSearch;

    @Property
    private SourceWizardChoice source_catalogue = SourceWizardChoice.CATALOGUE;

    @Property
    private SourceWizardChoice source_file = SourceWizardChoice.FILE;

    @Property
    private SourceWizardChoice source_url = SourceWizardChoice.URL;

    @Property
    private SourceWizardChoice source_refno = SourceWizardChoice.REFNO;

    private String conversationId = null;

    private Step step = null;

    @Property
    @Persist(PersistenceConstants.FLASH)
    private String bookToAdd;

    @Property
    @Persist
    @Validate("required")
    private List<UploadedFile> uploads;

    @Inject
    private ComponentResources resources;

    @Inject
    private Messages messages;

    // Screen fields

    @Property(write = false)
    @Persist(PersistenceConstants.FLASH)
    private String message;

    @Property
    private String position;

    // The conversation and its contents

    @SessionState
    private unidue.rc.model.web.Conversations conversations;

    @Property
    private BookChapterWizardData wizardData;


    // The Loop component will automatically call this for every row on submit.

    @OnEvent(EventConstants.ACTIVATE)
    void onActivate(Integer rcId) {

        if (this.step == null) {
            startConversation();
            this.step = Step.SELECT;

        }
        log.info("Step is " + step);

        log.info("loading reserve collection " + rcId);
        collection = collectionDAO.get(ReserveCollection.class, rcId);

    }


    @OnEvent(EventConstants.ACTIVATE)
    void onActivate(Integer rcId, Step step, String conversationId) {
        this.step = step;
        this.conversationId = conversationId;

        if (this.step == null) {

            startConversation();
            this.step = Step.SELECT;

        }
        log.info("Step is " + step);

        log.info("loading reserve collection " + rcId);
        collection = collectionDAO.get(ReserveCollection.class, rcId);

    }

    void onValidateFromBookurl(String value) throws ValidationException {


        if (value == null) return;
        try {
            URL test = new URL(value);
        } catch (MalformedURLException e) {

            throw new ValidationException(messages.get("error.url.not.valid"));
        }

    }

    @OnEvent(EventConstants.PREPARE)
    void onPrepare() {
        if (wizardData == null) {
            // Get objects for the chapterForm fields to overlay.
            wizardData = restoreChapterWizardDataFromConversation();

            if (wizardData.getBookToAdd() != null && step == Step.EDIT) {
                OpacFacadeBook book = wizardData.getBookToAdd();
                OpacFacadeBook details = opacFacade.getDetails(book.getDocNumber());

                BookChapter chapter = wizardData.getChapter();

                chapter.setBookAuthors(details.getAuthor());
                chapter.setIsbn(details.getIsbn());
                chapter.setBookTitle(details.getTitle());
                OpacFacadeLibraryData libraryData = opacFacade.getLibraryData(book.getDocNumber());
                if (libraryData != null && libraryData.getTotalItemCount() > 0) {
                    Optional<String> signature = libraryData.getItems().stream()
                            .filter(item -> !StringUtils.isEmpty(item.getSignature()))
                            .map(item -> item.getSignature())
                            .findFirst();
                    if (signature.isPresent())
                        chapter.setSignature(signature.get());
                }
                chapter.setPublisher(details.getPublisher());
                chapter.setPlaceOfPublication(details.getPublishingLocation());
                chapter.setEditor(details.getEditor());
                chapter.setEdition(details.getEdition());
                if (NumberUtils.isDigits(details.getYear()))
                    chapter.setYearOfPublication(Integer.valueOf(details.getYear()));
            }
        }
    }

    public void set(Step step, String conversationId) {
        this.step = step;
        this.conversationId = conversationId;
    }

    public void startConversation() {

        conversationId = conversations.startConversation(WIZARD_CONVERSATION_PREFIX);
        wizardData = new BookChapterWizardData();
        wizardData.getBreadcrumbs().add(messages.get("create.bookchapter.breadcrumb.source"));
        saveChapterWizardDataToConversation(wizardData);
    }

    private void saveChapterWizardDataToConversation(BookChapterWizardData bookChapterWizardData) {
        conversations.saveToConversation(conversationId, BOOK_CHAPTER_KEY, bookChapterWizardData);
    }

    private BookChapterWizardData restoreChapterWizardDataFromConversation() {

        Object conversation = conversations.restoreFromConversation(conversationId, BOOK_CHAPTER_KEY);
        return (BookChapterWizardData) conversation;
    }

    private void endConversation() {
        conversations.endConversation(conversationId);

        // If conversations SSO is now empty then remove it from the session

        if (conversations.isEmpty()) {
            conversations = null;
        }
    }

    /**
     * Called when the link for this page is generated.
     *
     * @see {@link EventConstants#PASSIVATE}
     */
    @OnEvent(EventConstants.PASSIVATE)
    Object onPassivate() {
        return new Object[]{collection.getId(), step, conversationId};
    }

    @OnEvent(value = EventConstants.VALIDATE, component = "chapterForm")
    Object onValidateFromChapterForm() {
        wizardData.getBreadcrumbs().clear();
        wizardData.getBreadcrumbs().add(messages.get("create.bookchapter.breadcrumb.source"));

        saveChapterWizardDataToConversation(wizardData);

        switch (step) {
            case SELECT:
                /*
                * Triggered after submitting the select window of the wizard.
                 */
                switch (source) {

                    /**
                     * Depending on the decision which source to use for the creation of a book chapter entry,
                     * the step is set either to initiate a book-search, show an upload file dialog or ask the user
                     * for url or reference number
                     */
                    case CATALOGUE:
                        this.step = Step.BOOKSEARCH;
                        break;
                    case FILE:
                        this.step = Step.UPLOADFILE;
                        break;
                    case URL:
                        this.step = Step.URL;
                        break;
                    case REFNO:
                        this.step = Step.REFNO;
                        break;
                }
                wizardData.getBreadcrumbs().add(messages.get("create.bookchapter.breadcrumb." + source.name().toLowerCase()));
                log.info("proceeding to = " + this.step);
                break;
            case UPLOADFILE:
                this.step = Step.EDIT;
                wizardData.setUploads(uploads);
                wizardData.getBreadcrumbs().add(messages.get("create.bookchapter.breadcrumb.edit"));
                log.info("proceeding to = " + this.step + " files=" + wizardData.getUploads());
                break;
            case URL:
                this.step = Step.EDIT;
                wizardData.getBreadcrumbs().add(messages.get("create.bookchapter.breadcrumb.edit"));
                log.info("proceeding to = " + this.step + " url=" + wizardData.getUrl());
                break;
            case REFNO:
                this.step = Step.EDIT;
                wizardData.getBreadcrumbs().add(messages.get("create.bookchapter.breadcrumb.edit"));
                log.info("proceeding to = " + this.step + " refno=" + wizardData.getRefNo());
                break;
            case BOOKSEARCH:
                this.step = Step.BOOKSELECT;
                wizardData.getBreadcrumbs().add(messages.get("create.bookchapter.breadcrumb.catalogue"));
                wizardData.getBreadcrumbs().add(messages.get("create.bookchapter.breadcrumb.select"));
                log.info("booksearch  = " + bookSearch);
                OpacFacadeFind searchResult = opacFacade.search(bookSearch);

                if (searchResult != null) {
                    wizardData.setBooks(searchResult.getBooks());
                }
                log.info("books  = " + wizardData.getBooks());
                message = (wizardData.getBooks() == null || wizardData.getBooks().isEmpty())
                        ? messages.get("error.msg.search.no.results")
                        : null;


                log.info("proceeding to = " + this.step);

                break;


            case BOOKSELECT:
                this.step = Step.EDIT;

                wizardData.getBreadcrumbs().add(messages.get("create.bookchapter.breadcrumb.catalogue"));
                wizardData.getBreadcrumbs().add(messages.get("create.bookchapter.breadcrumb.select"));
                wizardData.getBreadcrumbs().add(messages.get("create.bookchapter.breadcrumb.edit"));

                OpacFacadeBook book = getOpacBookEncoder().toValue(bookToAdd);
                wizardData.setBookToAdd(book);
                log.info("proceeding to = " + this.step);
                break;

            /**
             * Editing and supplying the last information is finished and page is submitted.
             * Now the Bookchapter will be created with the given information.
             */
            case EDIT:
                chapterFormFragment.validate();
                if (chapterForm.getHasErrors())
                    return this;

                BookChapter chapter = wizardData.getChapter();
                if (wizardData.getRefNo() != null)
                    chapter.setReferenceNumber(wizardData.getRefNo());

                // upload files if present
                Resource resource = null;
                try {
                    resource = createResource();
                } catch (IOException | CommitException e) {
                    chapterForm.recordError(messages.format("error.msg.could.not.commit.chapter", chapter));
                }

                try {
                    // create chapter
                    scannableService.create(chapter, collection, resource);


                    Headline headline = chapterFormFragment.getHeadline();
                    if (headline != null)
                        headlineDAO.move(chapter.getEntry(), headline);
                } catch (CommitException e) {
                    chapterForm.recordError(messages.format("error.msg.could.not.commit.chapter", chapter));
                }
                if (!chapterForm.getHasErrors()) {
                    log.info("headline entry for " + collection + " saved");
                    Link viewCollectionLink = linkSource.createPageRenderLinkWithContext(ViewCollection.class,
                            collection.getId());
                    endConversation();
                    return viewCollectionLink;
                }
                break;
            default:
                throw new IllegalStateException("Should not get here. step = " + step);

        }
        return this;
    }

    private Resource createResource() throws IOException, CommitException {
        List<UploadedFile> uploadedFiles = wizardData.getUploads();
        Resource resource = null;
        if (uploadedFiles != null && !uploadedFiles.isEmpty()) {
            UploadedFile uploadedFile = uploads.get(0);
            resource = resourceService.upload(collection, uploadedFile.getFileName(), uploadedFile.getStream());
            uploads = null;
        }
        String fullTextURL = wizardData.getUrl();
        if (!StringUtils.isEmpty(fullTextURL)) {
            if (resource != null) {
                resource.setFullTextURL(fullTextURL);
            } else {
                resource = resourceService.create(fullTextURL);
            }
        }
        return resource;
    }

    @OnEvent(EventConstants.SUCCESS)
    public Link onSuccess() {
        return linkSource.createPageRenderLinkWithContext(ViewCollection.class, collection.getId());
    }

    public boolean isInSelect() {
        return step == Step.SELECT;
    }

    public boolean isInRefNo() {
        return step == Step.REFNO;
    }

    public boolean isInBookSearch() {
        return step == Step.BOOKSEARCH;
    }

    public boolean isInBookSelect() {
        return step == Step.BOOKSELECT;
    }

    public boolean isInUrl() {
        return step == Step.URL;
    }

    public boolean isInEdit() {
        return step == Step.EDIT;
    }

    public boolean isInUploadFile() {
        return step == Step.UPLOADFILE;
    }

    public OpacFacadeBookValueEncoder getOpacBookEncoder() {
        return new OpacFacadeBookValueEncoder(opacFacade) {
            @Override
            public OpacFacadeBook toValue(String docNumber) {
                OpacFacadeBook book = get(wizardData.getBooks(), docNumber);
                return book != null
                        ? book
                        : super.toValue(docNumber);
            }

            OpacFacadeBook get(List<OpacFacadeBook> books, String docNumber) {

                for (OpacFacadeBook book : books) {
                    if (docNumber.equals(book.getDocNumber()))
                        return book;
                }
                return null;
            }
        };
    }

    @Override
    public void checkPermission(CollectionSecurityService securityService, EventContext activationContext) throws AuthorizationException {
        Integer rcID = activationContext.get(Integer.class, 0);
        securityService.checkPermission(ActionDefinition.EDIT_ENTRIES, rcID);
    }
}