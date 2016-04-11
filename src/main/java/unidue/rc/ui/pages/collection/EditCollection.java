package unidue.rc.ui.pages.collection;


import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.PageLoaded;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.Service;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.TextField;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import se.unbound.tapestry.breadcrumbs.BreadCrumb;
import se.unbound.tapestry.breadcrumbs.BreadCrumbList;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.LibraryLocationDAO;
import unidue.rc.dao.ParticipationDAO;
import unidue.rc.dao.ReserveCollectionDAO;
import unidue.rc.dao.ReserveCollectionNumberDAO;
import unidue.rc.dao.RoleDAO;
import unidue.rc.model.ActionDefinition;
import unidue.rc.model.DefaultRole;
import unidue.rc.model.LibraryLocation;
import unidue.rc.model.Participation;
import unidue.rc.model.ReserveCollection;
import unidue.rc.model.ReserveCollectionNumber;
import unidue.rc.model.Role;
import unidue.rc.security.RequiresActionPermission;
import unidue.rc.ui.ProtectedPage;
import unidue.rc.ui.pages.Index;
import unidue.rc.ui.selectmodel.LibraryLocationSelectModel;
import unidue.rc.ui.valueencoder.LibraryLocationValueEncoder;
import unidue.rc.workflow.CollectionService;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Within this page an existing {@link ReserveCollection} can be edited.
 *
 * @author Nils Verheyen
 */
@Import(library = {
        "context:js/apply.datepicker.js","context:js/jquery.ui.datepicker.de.js"
})
@BreadCrumb(titleKey = "edit.collection")
@ProtectedPage
public class EditCollection {

    private static final String DATE_FORMAT = "dd.MM.yyyy";
    private static final String DATE_PICKER_FORMAT = "[0-9]{2}\\.[0-9]{2}\\.[0-9]{4}";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern(DATE_FORMAT);

    @Inject
    private Logger log;

    /**
     * The {@link ReserveCollection} edited within this page. The {@link Persist} annotation has to be present here, so
     * the object is page persisted.
     *
     * @see <a href="http://tapestry.apache.org/persistent-page-data.html">Persistent Page Data</a>
     */
    @Property
    @Persist
    private ReserveCollection collection;

    @Inject
    private PageRenderLinkSource linkSource;

    @Property
    private SelectModel libraryLocationSelectModel;

    @InjectPage
    private Index index;

    @Property
    @Validate("required")
    private String expiry;

    @Property
    private String dissolveAt;

    @Property
    @Validate("required")
    private String readKey;

    @Property
    private String writeKey;

    @Property
    @Validate("required")
    private String title;

    @Property
    @Validate("required")
    private String alephSystemId;

    @Property
    @Validate("required")
    private String alephUserId;

    @Property
    @Validate("required")
    private String comment;

    @Property
    private String docent;

    /**
     * Contains the new number, that should be set for this {@link #collection}.
     */
    @Property
    @Validate("required")
    private Integer newNumberValue;

    private ReserveCollectionNumber newNumber;

    @Property
    @Validate("required")
    private LibraryLocation libraryLocation;

    @Component(id = "reserve_collection_form")
    private Form form;

    @Component(id = "number")
    private TextField newNumberField;

    @Component(id = "expiry")
    private TextField expiryField;

    @Component(id = "dissolveAt")
    private TextField dissolveAtField;

    @Component(id = "readKey")
    private TextField readKeyField;

    @Inject
    private LibraryLocationDAO libraryLocationDAO;

    @Inject
    @Service(ReserveCollectionDAO.SERVICE_NAME)
    private ReserveCollectionDAO collectionDAO;

    @Inject
    private CollectionService collectionService;

    @Inject
    private ReserveCollectionNumberDAO numberDAO;

    @Inject
    @Service(RoleDAO.SERVICE_NAME)
    private RoleDAO roleDAO;

    @Inject
    @Service(ParticipationDAO.SERVICE_NAME)
    private ParticipationDAO participationDAO;

    @Inject
    private ComponentResources resources;

    @Inject
    private Messages messages;

    @SessionState
    private BreadCrumbList breadCrumbList;

    @PageLoaded
    void onPageLoaded() {
        this.libraryLocationSelectModel = new LibraryLocationSelectModel(libraryLocationDAO);
    }

    public List<String> getDocents() {
        return collectionService.getDocents(collection);
    }

    /**
     * On activation of this page, the reservecollection for the given ID is loaded
     *
     * @param rcId
     */

    @OnEvent(EventConstants.ACTIVATE)
    @RequiresActionPermission(ActionDefinition.EDIT_RESERVE_COLLECTION)
    void onActivate(Integer rcId) {

        log.info("loading reserve collection " + rcId);

        collection = collectionDAO.get(ReserveCollection.class, rcId);
        newNumberValue = collection != null && collection.getNumber() != null
                ? collection.getNumber().getNumber()
                : -1;
        libraryLocation = collection.getLibraryLocation();

        expiry = new DateTime(collection.getValidTo()).toString(DATE_FORMATTER);
        if (collection.getDissolveAt() != null)
            dissolveAt = new DateTime(collection.getDissolveAt()).toString(DATE_FORMATTER);
    }

    /**
     * Validates the form fields
     */
    @OnEvent(value = EventConstants.VALIDATE, component = "reserve_collection_form")
    void onFormValidate() {

        if (!newNumberValue.equals(collection.getNumber().getNumber())) {
            Role docentRole = roleDAO.getRole(DefaultRole.DOCENT);
            List<Participation> participations = participationDAO.getActiveParticipations(docentRole, collection);

            // number has changed
            Optional<Participation> p = participations.stream()
                    .filter(participation -> numberDAO.isNumberFree(newNumberValue, collection.getLibraryLocation()))
                    .findAny();
            if (!p.isPresent()) {
                // record error if number has changed and new number is not free for target location

                form.recordError(newNumberField, messages.get("use.number.error"));
            } else {
                createOrUpdateNewNumber();
            }
        } else {

            // new number = old number if value has not changed
            newNumber = collection.getNumber();
        }

        if (!expiry.matches(DATE_PICKER_FORMAT))
            form.recordError(expiryField, messages.get("invalid.date.format"));
        if (!StringUtils.isBlank(dissolveAt) && !dissolveAt.matches(DATE_PICKER_FORMAT))
            form.recordError(dissolveAtField, messages.get("invalid.date.format"));
        if (collection.getReadKey().equals(collection.getWriteKey()))
            form.recordError(readKeyField, messages.get("error.msg.identical.keys"));

    }

    /**
     * Sets this {@link #newNumber} by this {@link #newNumberValue}. This method must be used on validation events of
     * tapestry and the {@link #newNumberValue} must be available.
     */
    private void createOrUpdateNewNumber() {

        newNumber = numberDAO.getNumber(newNumberValue);
        if (newNumber == null) {

            try {
                newNumber = numberDAO.create(newNumberValue);
            } catch (CommitException e) {
                log.error("could not create number " + e.getMessage());
                form.recordError(messages.get("msg.could.not.create.number"));
            }
        }
    }

    /**
     * Called after form was submitted. Updates the reservecollection with the new values.
     *
     * @return
     */
    @OnEvent(EventConstants.SUCCESS)
    Object afterFormSubmit() {
        collection.setNumber(newNumber);
        collection.setLibraryLocation(libraryLocation);
        collection.setValidTo(DateTime.parse(expiry, DATE_FORMATTER).toDate());

        Date dissolveAtDate = StringUtils.isBlank(dissolveAt)
                ? null
                : DateTime.parse(dissolveAt, DATE_FORMATTER).toDate();
        collection.setDissolveAt(dissolveAtDate);

        try {
            collectionService.update(collection);
            Link viewCollectionLink = linkSource.createPageRenderLinkWithContext(ViewCollection.class,
                    collection.getId());
            return viewCollectionLink;
        } catch (CommitException e) {
            form.recordError(messages.format("error.msg.could.not.commit.collection", collection.getTitle()));
            return this;
        }

    }

    public ValueEncoder<LibraryLocation> getLibraryLocationEncoder() {
        return new LibraryLocationValueEncoder(libraryLocationDAO);
    }

    /**
     * Called when the link for this page is generated.
     *
     * @return
     * @see EventConstants#PASSIVATE
     */
    @OnEvent(EventConstants.PASSIVATE)
    Integer onPassivate() {
        return collection.getId();
    }


}
