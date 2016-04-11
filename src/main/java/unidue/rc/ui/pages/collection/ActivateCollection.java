package unidue.rc.ui.pages.collection;


import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.slf4j.Logger;
import se.unbound.tapestry.breadcrumbs.BreadCrumb;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.NumberAssignedException;
import unidue.rc.dao.ReserveCollectionDAO;
import unidue.rc.dao.ReserveCollectionNumberDAO;
import unidue.rc.model.ActionDefinition;
import unidue.rc.model.ReserveCollection;
import unidue.rc.model.ReserveCollectionStatus;
import unidue.rc.security.RequiresActionPermission;
import unidue.rc.ui.ProtectedPage;
import unidue.rc.workflow.CollectionService;

import java.util.Collection;

/**
 * Created by nils on 29.07.15.
 */
@BreadCrumb(titleKey = "reserve-collection.activate")
@ProtectedPage
public class ActivateCollection {

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
    private Form chooseNumberForm;

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

    @Property
    private Integer number;

    @Property(write = false)
    @Persist(PersistenceConstants.FLASH)
    private String errorMessage;

    @OnEvent(EventConstants.ACTIVATE)
    @RequiresActionPermission(ActionDefinition.ACTIVATE_RESERVE_COLLECTION)
    void onActivate(Integer collectionID) {
        this.collection = collectionDAO.get(ReserveCollection.class, collectionID);
        this.from = from != null ? from : 1;
        this.to = to != null ? to : 100;
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
            chooseNumberForm.recordError(messages.get("error.msg.from.larger.than.to"));
    }

    @OnEvent(value = EventConstants.SUCCESS, component = "choose_number_form")
    void onSuccessFromChooseNumberForm() {
        freeNumbers = numberDAO.getFreeNumbers(from, to, collection.getLibraryLocation());
    }

    @OnEvent("number_chosen")
    Object onNumberChosen(Integer number) {
        try {
            collectionService.activate(collection, number);

            Link viewCollectionLink = linkSource.createPageRenderLinkWithContext(ViewCollection.class, collection.getId());
            viewCollectionLink.addParameter(ViewCollection.WORKFLOW_PARAM, ReserveCollectionStatus.ACTIVE.name());
            return viewCollectionLink;
        } catch (CommitException e) {
            log.error("could not activate collection " + collection.getObjectId());
            errorMessage = messages.get("error.msg.could.not.activate.collection");
        } catch (NumberAssignedException e) {
            errorMessage = messages.format("error.msg.activate.collection.number.in.use", collection.getTitle(), number);
        }
        return null;
    }

}
