package unidue.rc.ui.pages.entry;



import org.apache.shiro.authz.AuthorizationException;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.Link;
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
import unidue.rc.dao.CommitException;
import unidue.rc.dao.HeadlineDAO;
import unidue.rc.model.ActionDefinition;
import unidue.rc.model.Entry;
import unidue.rc.model.Headline;
import unidue.rc.model.ReserveCollection;
import unidue.rc.security.CollectionSecurityService;
import unidue.rc.ui.ProtectedPage;
import unidue.rc.ui.SecurityContextPage;
import unidue.rc.ui.pages.collection.ViewCollection;
import unidue.rc.workflow.EntryService;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Within this page an existing {@link unidue.rc.model.ReserveCollection} can be edited.
 *
 * @author Marcus Koesters
 */
@BreadCrumb(titleKey = "edit.headline")
@ProtectedPage
public class EditHeadline implements SecurityContextPage {

    @Inject
    private Logger log;

    @Inject
    @Service(HeadlineDAO.SERVICE_NAME)
    private HeadlineDAO headlineDAO;

    @Property
    private Headline headline;

    @Property
    private Headline arrangingHeadline;

    @Property
    private boolean moveContent;

    @Property
    private ReserveCollection collection;

    @Inject
    private Messages messages;

    @Inject
    private PageRenderLinkSource linkSource;

    @Component(id = "edit_headline_form")
    private Form form;

    public String getCollectionTitle() {
        return headline.getEntry().getReserveCollection().getTitle();
    }


    @OnEvent(EventConstants.ACTIVATE)
    void onActivate(Integer entryId) {
        log.info("loading entry " + entryId);
        headline = headlineDAO.get(Headline.class, entryId);
        log.info("entry is  " + headline);
        collection = headline.getEntry().getReserveCollection();
        arrangingHeadline = headline.getEntry().getAssignedHeadline();
    }

    @OnEvent(EventConstants.SUCCESS)
    Object onHeadlineSubmitted() {

        try {

            if (arrangingHeadline != null)
                move();

            headlineDAO.update(headline);
            log.info("headline entry for " + headline + " updated");

            Link viewCollectionLink = linkSource.createPageRenderLinkWithContext(ViewCollection.class,
                    collection.getId());

            return viewCollectionLink;
        } catch (CommitException e) {
            form.recordError(messages.format("error.msg.could.not.commit.headline", headline));
            return null;
        }
    }

    private void move() throws CommitException {

        List<Entry> successors = Collections.EMPTY_LIST;
        boolean moveContent = this.moveContent;
        if (moveContent) {

            // if content under target headline should be moved get next headline
            Headline nextHeadline = headlineDAO.getNextHeadline(headline);
            Predicate<Entry> filter = nextHeadline != null
                    // if there is a next headline get all entries between this headline and next headline
                    ? getBetweenPredicate(headline.getEntry(), nextHeadline.getEntry())
                    // otherwise just get all following entries
                    : entry -> entry.getPosition() > headline.getEntry().getPosition();

            successors = collection.getEntries().stream()
                    .filter(filter)
                    .collect(Collectors.toList());
        }

        // move headline below the last entry of arranging headline
        headlineDAO.move(headline.getEntry(), arrangingHeadline);

        if (moveContent) {
            // after headline is moved all entries can be moved along
            for (Entry successor : successors)
                headlineDAO.move(successor, headline);
        }
    }

    private static Predicate<Entry> getBetweenPredicate(Entry predecessor, Entry successor) {
        return entry -> entry.getPosition() > predecessor.getPosition()
                && entry.getPosition() < successor.getPosition();
    }

    /**
     * Called when the link for this page is generated.
     *
     * @return
     * @see {@link org.apache.tapestry5.EventConstants#PASSIVATE}
     */
    @OnEvent(EventConstants.PASSIVATE)
    Integer onPassivate() {
        return headline.getId();
    }

    @Override
    public void checkPermission(CollectionSecurityService securityService, EventContext activationContext) throws AuthorizationException {
        Integer headlineID = activationContext.get(Integer.class, 0);
        Headline headline = headlineDAO.get(Headline.class, headlineID);
        securityService.checkPermission(ActionDefinition.EDIT_ENTRIES, headline.getEntry().getReserveCollection().getId());
    }
}