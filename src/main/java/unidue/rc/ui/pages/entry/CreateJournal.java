package unidue.rc.ui.pages.entry;



import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.upload.services.UploadedFile;
import org.slf4j.Logger;
import se.unbound.tapestry.breadcrumbs.BreadCrumb;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.HeadlineDAO;
import unidue.rc.dao.ReserveCollectionDAO;
import unidue.rc.model.*;
import unidue.rc.security.RequiresActionPermission;
import unidue.rc.ui.ProtectedPage;
import unidue.rc.ui.components.JournalArticleFormFragment;
import unidue.rc.ui.pages.collection.ViewCollection;
import unidue.rc.workflow.ResourceService;
import unidue.rc.workflow.ScannableService;

import java.io.IOException;
import java.util.List;

/**
 * Within this page an existing {@link ReserveCollection} can be edited.
 *
 * @author Marcus Koesters
 */
@BreadCrumb(titleKey = "new.journal.article")
@ProtectedPage
public class CreateJournal {

    @Inject
    private Logger log;

    @Inject
    private PageRenderLinkSource linkSource;

    @Inject
    private ScannableService scannableService;

    @Inject
    private ResourceService resourceService;

    @Property
    private ReserveCollection collection;

    @Inject
    private ReserveCollectionDAO collectionDAO;

    @Inject
    @Service(HeadlineDAO.SERVICE_NAME)
    private HeadlineDAO headlineDAO;

    @Inject
    private Messages messages;

    @Component(id = "article_form")
    private Form form;

    @Component(id = "articleFragment")
    private JournalArticleFormFragment articleFormFragment;

    @Property
    private JournalArticle article;

    @Property
    private Headline headline;

    @OnEvent(EventConstants.ACTIVATE)
    @RequiresActionPermission(ActionDefinition.EDIT_ENTRIES)
    void onActivate(Integer rcId) {
        log.info("loading reserve collection " + rcId);
        collection = collectionDAO.get(ReserveCollection.class, rcId);
        article = new JournalArticle();
    }

    @OnEvent(value = EventConstants.VALIDATE, component = "article_form")
    void onValidateFromArticle() {

        articleFormFragment.validate(form);
        if (form.getHasErrors())
            return;

        Headline headline = articleFormFragment.getHeadline();
        try {

            Resource resource = createResource();

            scannableService.create(article, collection, resource);

            log.info("Journal entry for " + collection + " saved");
            if (article != null && headline != null)
                headlineDAO.move(article.getEntry(), headline);
        } catch (IOException | CommitException e) {
            log.info("could not commit article " + article);
            form.recordError(messages.format("error.msg.could.not.commit.article", article));
        }
    }

    private Resource createResource() throws IOException, CommitException {

        // save files if necessary
        List<UploadedFile> uploadedFiles = articleFormFragment.getUploads();
        Resource resource = null;
        if (uploadedFiles != null && !uploadedFiles.isEmpty()) {
            UploadedFile uploadedFile = uploadedFiles.get(0);
            resource = resourceService.upload(collection, uploadedFile.getFileName(), uploadedFile.getStream());
            uploadedFiles.clear();
        }
        // set full text url if necessary
        String fullTextURL = articleFormFragment.getUrl();
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
    Object afterFormSubmit() {
        return linkSource.createPageRenderLinkWithContext(ViewCollection.class, collection.getId());
    }

    /**
     * Called when the link for this page is generated.
     *
     * @return
     * @see {@link EventConstants#PASSIVATE}
     */
    @OnEvent(EventConstants.PASSIVATE)
    Integer onPassivate() {
        return collection.getId();
    }


}