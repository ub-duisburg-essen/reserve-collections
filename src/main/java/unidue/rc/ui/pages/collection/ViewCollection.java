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
package unidue.rc.ui.pages.collection;



import miless.model.User;
import org.apache.cayenne.Persistent;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.tapestry5.Block;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.Service;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.AssetSource;
import org.apache.tapestry5.services.HttpError;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Response;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;
import org.apache.tapestry5.services.ajax.JavaScriptCallback;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.slf4j.Logger;
import se.unbound.tapestry.breadcrumbs.BreadCrumb;
import se.unbound.tapestry.breadcrumbs.BreadCrumbInfo;
import se.unbound.tapestry.breadcrumbs.BreadCrumbList;
import unidue.rc.dao.*;
import unidue.rc.io.InlineStreamResponse;
import unidue.rc.io.OutputStreamResponse;
import unidue.rc.io.ZIPStreamResponse;
import unidue.rc.model.ActionDefinition;
import unidue.rc.model.Book;
import unidue.rc.model.BookChapter;
import unidue.rc.model.Entry;
import unidue.rc.model.File;
import unidue.rc.model.Headline;
import unidue.rc.model.Html;
import unidue.rc.model.IntPrimaryKey;
import unidue.rc.model.JournalArticle;
import unidue.rc.model.Participation;
import unidue.rc.model.Reference;
import unidue.rc.model.ReserveCollection;
import unidue.rc.model.Scannable;
import unidue.rc.model.WebLink;
import unidue.rc.security.CollectionSecurityService;
import unidue.rc.system.SystemConfigurationService;
import unidue.rc.ui.ProtectedPage;
import unidue.rc.ui.pages.entry.DuplicateBookChapter;
import unidue.rc.ui.pages.entry.DuplicateJournalArticle;
import unidue.rc.ui.pages.login.Index;
import unidue.rc.ui.services.MimeService;
import unidue.rc.ui.valueencoder.BaseValueEncoder;
import unidue.rc.workflow.CollectionService;
import unidue.rc.workflow.EntryService;
import unidue.rc.workflow.ResourceService;
import unidue.rc.workflow.ScannableService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Nils Verheyen
 * @since 16.08.13 14:56
 */
@Import(library = {
        "context:js/view.collection.js"
})
@ProtectedPage
@BreadCrumb(titleKey = "login")
public class ViewCollection {

    public static final String WORKFLOW_PARAM = "wf";

    @Inject
    private SystemConfigurationService systemConfigurationService;

    @Inject
    private Logger log;

    @Inject
    private ReserveCollectionDAO collectionDAO;

    @Inject
    private ParticipationDAO participationDAO;

    @Inject
    @Service(EntryDAO.SERVICE_NAME)
    private EntryDAO entryDAO;

    @Inject
    @Service(ResourceDAO.SERVICE_NAME)
    private ResourceDAO resourceDAO;

    @Inject
    private ResourceService resourceService;

    @Inject
    private CollectionService collectionService;

    @Inject
    private EntryService entryService;

    @Inject
    private ScannableService scannableService;

    @Inject
    private CollectionSecurityService securityService;

    @Inject
    private MimeService mimeService;

    @Inject
    private Messages messages;

    @Inject
    private ComponentResources resources;

    @SessionState
    private BreadCrumbList breadCrumbList;

    @Property
    private Headline headline;

    @Property
    private Entry entry;

    @Property
    private String docent;

    @Property(write = false)
    @Persist(PersistenceConstants.FLASH)
    private String workflowMessage;

    /**
     * {@link Block} for specific {@linkplain Entry} shown inside the ui.
     */
    @Inject
    private Block headlineBlock, htmlBlock, bookBlock, bookChapterBlock, fileBlock,
            journalArticleBlock, webLinkBlock, referenceBlock;

    @Property
    @Inject
    private Block entryControlsBlock, cloneableEntryControlsBlock, noEditEntryControlsBlock;

    @Inject
    private Locale locale;

    @InjectComponent
    private Zone entriesZone;

    @InjectComponent
    private Zone tocZone;

    @Inject
    private Request request;

    @Inject
    private Response response;

    @Inject
    private AjaxResponseRenderer ajaxResponseRenderer;

    @Inject
    private AssetSource assetSource;

    @Inject
    private PageRenderLinkSource linkSource;

    @Property
    private ReserveCollection collection;

    private List<Headline> headlines;

    @Environmental
    private JavaScriptSupport javaScriptSupport;

    @Property
    private Participation participation;


     @SetupRender
    public void beginRender() {
        BreadCrumbInfo breadCrumbInfo = new BreadCrumbInfo("reserve.collection",
                linkSource.createPageRenderLinkWithContext(ViewCollection.class, collection.getId()),
                resources.getPageName());
        breadCrumbList.add(breadCrumbInfo);
        breadCrumbList.getLastCrumb().setTitle(collection.getTitle());


        Collections.sort(collection.getEntries());

        String workflowParam = request.getParameter(WORKFLOW_PARAM);
        if (workflowParam != null)
            workflowMessage = messages.format("info.message.status.changed", messages.get(workflowParam));
    }



    @OnEvent(EventConstants.ACTIVATE)
    Object onPageActivate(int reserveCollectionId) throws IOException {
        collection = collectionDAO.get(ReserveCollection.class, reserveCollectionId);
        User currentUser = securityService.getCurrentUser();
        if (currentUser != null)
            participation = participationDAO.getActiveParticipation(currentUser, collection);

        // load resources
        if (collection == null)
            return new HttpError(HttpServletResponse.SC_NOT_FOUND, messages.get("error.msg.collection.not.found"));

        try {
            securityService.checkPermission(ActionDefinition.VIEW_RESERVE_COLLECTION, reserveCollectionId);
            BreadCrumbInfo breadCrumb = new BreadCrumbInfo("reserve.collection",
                    linkSource.createPageRenderLinkWithContext(ViewCollection.class),
                    resources.getPageName());
            breadCrumbList.add(breadCrumb);
        } catch (AuthorizationException e) {
            return currentUser == null
                    // return to login page if not logged in
                    ? linkSource.createPageRenderLinkWithContext(Index.class)
                    // otherwise the user has to enter the access key
                    : linkSource.createPageRenderLinkWithContext(EnterAccessKey.class, reserveCollectionId);
        }

        return null;
    }

    @OnEvent(EventConstants.PASSIVATE)
    Integer onPagePassivate() {
        return collection != null ? collection.getId() : null;
    }

    @OnEvent(value = EventConstants.SUBMIT, component = "entriesForm")
    void onEntriesSubmitted() {

        // check security
        securityService.checkPermission(ActionDefinition.EDIT_ENTRIES, collection.getId());

        for (Entry entry : collection.getEntries()) {
            try {
                entryService.update(entry);
            } catch (CommitException e) {
                log.error("could not update entry " + entry, e);
            }
        }
        Collections.sort(collection.getEntries());


        // look at view.collection.js
        ajaxResponseRenderer.addCallback(new JavaScriptCallback() {
            @Override
            public void run(JavaScriptSupport javascriptSupport) {
                javascriptSupport.addScript("bindSortable();");
            }
        });
        if (request.isXHR()) {
            ajaxResponseRenderer
                    .addRender(entriesZone)
                    .addRender(tocZone);
        }
    }


    @OnEvent(value = "download")
    Object onDownload(int resourceID) {

        unidue.rc.model.Resource resource = resourceDAO.get(unidue.rc.model.Resource.class, resourceID);
        Class<?> mimePage = mimeService.getPage(resource);

        // if page exists the can display given response as inline content return the page
        if (mimePage != null) {
            return linkSource.createPageRenderLinkWithContext(mimePage, resourceID);
        } else {
            // otherwise let the browser choose what should be done with the resource
            java.io.File file = resourceService.download(resource);

            InlineStreamResponse response = new InlineStreamResponse(file, resource);
            return response;
        }

    }

    @OnEvent("zip")
    OutputStreamResponse onZipReserveCollection() {
        List<java.io.File> files = collectionService.getFiles(collection);
        String zipFilename = String.join("_",
                StringUtils.replace(collection.getLibraryLocation().getName(), "/", "_"),
                Integer.toString(collection.getNumber().getNumber()),
                collection.getTitle());
        return new ZIPStreamResponse(zipFilename, zipFilename, files);
    }

    public List<String> getDocents() {
        return collectionService.getDocents(collection);
    }

    public String getLocalizedStatus() {
        return messages.get(collection.getStatus().name());
    }

    public String getValidTo() {
        return getFormattedDate(collection.getValidTo());
    }

    public String getDissolveAt() {
        return getFormattedDate(collection.getDissolveAt());
    }

    private String getFormattedDate(Date date) {

        /*
         * date format should be created inside getter, because pages are no longer
         * pooled and date format is not thread safe.
         * see http://tapestry.apache.org/release-notes-52.html#ReleaseNotes5.2-Tap5.2.0
         */
        DateFormat format = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
        return date != null
                ? format.format(date)
                : StringUtils.EMPTY;
    }

    public boolean isExpiring() {
        return collectionService.isCollectionExpiring(collection)
                && collection.getProlongUsed() == null
                && collection.getDissolveAt() == null;
    }

    public boolean isProlongable() {
        return collectionService.isCollectionProlongable(collection);
    }

    public List<Headline> getHeadlines() {
        headlines = collection.getHeadlines().stream()
                .sorted()
                .collect(Collectors.toList());
        return headlines;
    }

    public int getHeadlinePageNumber() {
        return headlines != null && headlines.contains(headline)
                ? headlines.indexOf(headline) + 1
                : -1;
    }

    /**
     * Returns the {@link Block} element for a specific {@linkplain Entry}. There is a block for each subclass of
     * {@linkplain Entry} that displays the content of it. If a new subclass of {@linkplain Entry} is created and should
     * be shown here a new block must be created and added to this method.
     *
     * @return  the block that the current entry targets
     */
    public Block getBlockForEntry() {

        Object value = entry.getValue();
        if (value instanceof Headline)
            return headlineBlock;
        else if (value instanceof Html)
            return htmlBlock;
        else if (value instanceof Book)
            return bookBlock;
        else if (value instanceof BookChapter)
            return bookChapterBlock;
        else if (value instanceof File)
            return fileBlock;
        else if (value instanceof JournalArticle)
            return journalArticleBlock;
        else if (value instanceof WebLink)
            return webLinkBlock;
        else if (value instanceof Reference)
            return referenceBlock;

        return null;
    }


    public ValueEncoder<? extends IntPrimaryKey> getEntryEncoder() {
        return new BaseValueEncoder(Entry.class, entryDAO);
    }

    /*
    Inside the tml file to this page there is a loop that iterates over all entry given in the reserve collection.
    Unluckily tapestry is not able to handle the subclasses of "Entry". This leads to exceptions, that a specific
    property could not be found. Each block defined inside the tml uses one of the following property methods to
    cast and retrieve the specific entry.
     */

    public Headline getEntryAsHeadline() {
        return (Headline) entry.getValue();
    }

    public Html getEntryAsHtml() {
        return (Html) entry.getValue();
    }

    public Book getEntryAsBook() {
        return (Book) entry.getValue();
    }

    public BookChapter getEntryAsBookChapter() {
        return (BookChapter) entry.getValue();
    }

    public File getEntryAsFile() {

        return (File) entry.getValue();
    }

    public JournalArticle getEntryAsJournalArticle() {
        return (JournalArticle) entry.getValue();
    }

    public WebLink getEntryAsWebLink() {
        return (WebLink) entry.getValue();
    }

    public Reference getEntryAsReference() {
        return (Reference) entry.getValue();
    }

    @OnEvent(value = "deleteEntry")
    Object onDeleteEntry(Integer entryID) {
        log.info("delete of entry " + entryID + " requested");
        Entry entry = entryDAO.get(Entry.class, entryID);
        try {
            entryService.delete(entry);
        } catch (CommitException e) {
            log.error("could not delete entry " + entry, e);
        }
        return this;
    }

    @OnEvent(value = "cloneEntry")
    Object onCloneEntry(Integer entryID) {

        Entry entry = entryDAO.get(Entry.class, entryID);
        Persistent entryValue = entry.getValue();
        if (entryValue instanceof Scannable) {

            if (entryValue instanceof JournalArticle) {
                return linkSource.createPageRenderLinkWithContext(DuplicateJournalArticle.class, ((JournalArticle) entryValue).getId());
            } else if (entryValue instanceof BookChapter) {
                return linkSource.createPageRenderLinkWithContext(DuplicateBookChapter.class, ((BookChapter) entryValue).getId());
            }
        }
        return this;
    }
}
