package unidue.rc.ui.pages.collection;

import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.slf4j.Logger;
import se.unbound.tapestry.breadcrumbs.BreadCrumb;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.ReserveCollectionDAO;
import unidue.rc.dao.ReserveCollectionNumberDAO;
import unidue.rc.model.ActionDefinition;
import unidue.rc.model.ReserveCollection;
import unidue.rc.security.RequiresActionPermission;
import unidue.rc.ui.ProtectedPage;
import unidue.rc.workflow.CollectionService;

import java.util.Collection;

@BreadCrumb(titleKey = "reserve-collection.edit.number")
@ProtectedPage
public class EditNumber {

    @Inject
    private Logger log;

    @Inject
    private CollectionService collectionService;

    @Inject
    private ReserveCollectionDAO collectionDAO;

    @Inject
    private ReserveCollectionNumberDAO numberDAO;

    @Inject
    private Messages messages;

    @Inject
    private PageRenderLinkSource linkSource;

    @Component(id = "choose_number_form")
    private Form numberForm;

    @Property
    private ReserveCollection collection;

    @Property
    @Validate("required")
    @Persist(PersistenceConstants.FLASH)
    private Integer from;

    @Property
    @Validate("required")
    @Persist(PersistenceConstants.FLASH)
    private Integer to;

    @Property(write = false)
    @Persist(PersistenceConstants.FLASH)
    private Collection<Integer> freeNumbers;

    @Property(write = false)
    @Persist(PersistenceConstants.FLASH)
    private String errorMessage;
    @OnEvent(EventConstants.ACTIVATE)
    @RequiresActionPermission(ActionDefinition.ACTIVATE_RESERVE_COLLECTION)
    void onActivate(Integer collectionID) {
        this.collection = collectionDAO.get(ReserveCollection.class, collectionID);
        this.from = from != null
                    ? from
                    : 1;
        this.to = to != null
                  ? to
                  : 100;
    }

    @OnEvent(EventConstants.ACTIVATE)
    void onActivate(Integer collectionID, Integer from, Integer to) {
        this.collection = collectionDAO.get(ReserveCollection.class, collectionID);
        this.from = from;
        this.to = to;
    }

    @OnEvent(EventConstants.PASSIVATE)
    Object[] onPassivate() {
        return from == null || to == null
               ? new Object[]{collection.getId()}
               : new Object[]{collection.getId(), from, to};
    }

    @OnEvent(value = EventConstants.VALIDATE, component = "choose_number_form")
    void onValidateFromChooseNumberForm() {
        if (from > to)
            numberForm.recordError(messages.get("error.msg.from.larger.than.to"));

        freeNumbers = numberDAO.getFreeNumbers(from, to, collection.getLibraryLocation());
    }


    @OnEvent("number_chosen")
    Object onNumberChosen(Integer chosenNumber) {
        try {
            collectionService.setNumber(chosenNumber, collection);

            Link viewCollectionLink = linkSource.createPageRenderLinkWithContext(ViewCollection.class, collection.getId());
            return viewCollectionLink;
        } catch (CommitException e) {
            log.error("could not set number " + chosenNumber + " to collection " + collection.getObjectId());
            errorMessage = messages.get("error.msg.could.not.update.collection");
        }
        return null;
    }
}
