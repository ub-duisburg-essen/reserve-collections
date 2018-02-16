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


import miless.model.MCRCategory;
import miless.model.User;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.TextField;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.SelectModelFactory;
import org.slf4j.Logger;
import se.unbound.tapestry.breadcrumbs.BreadCrumb;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.LibraryLocationDAO;
import unidue.rc.dao.MCRCategoryDAO;
import unidue.rc.dao.OriginDAO;
import unidue.rc.dao.ReserveCollectionNumberDAO;
import unidue.rc.dao.RoleDAO;
import unidue.rc.model.*;
import unidue.rc.security.CollectionSecurityService;
import unidue.rc.security.RequiresActionPermission;
import unidue.rc.ui.ProtectedPage;
import unidue.rc.ui.selectmodel.ExpirySelectModel;
import unidue.rc.ui.selectmodel.LibraryLocationSelectModel;
import unidue.rc.ui.selectmodel.OriginSelectModel;
import unidue.rc.ui.valueencoder.LibraryLocationValueEncoder;
import unidue.rc.ui.valueencoder.MCRCategoryValueEncoder;
import unidue.rc.ui.valueencoder.TimeValueEncoder;
import unidue.rc.workflow.CollectionService;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Within this page a new {@link ReserveCollection} can be created and persisted to backend.
 *
 * @author Nils Verheyen
 */
@Import(library = {
        "context:js/messages.js", // will be localized when the page is created
        "context:vendor/jquery-chosen/chosen/chosen.jquery.js",
        "context:js/apply.jquery.chosen.js"
}, stylesheet = {
        "context:vendor/jquery-chosen/chosen/chosen.css"
})
@BreadCrumb(titleKey = "new.collection")
@ProtectedPage
public class CreateCollection {

    @Inject
    private Logger log;

    @Inject
    private PageRenderLinkSource linkSource;

    /**
     * New {@link ReserveCollection} which is created through tapestry {@link Property} annotation.
     */
    @Property
    private ReserveCollection collection;

    @Inject
    private Locale locale;

    @Inject
    private LibraryLocationDAO libraryLocationDao;

    @Inject
    private CollectionService collectionService;

    @Inject
    private OriginDAO originDao;

    @Inject
    private MCRCategoryDAO mcrcategoryDao;

    @Inject
    private RoleDAO roleDAO;

    @Inject
    private ReserveCollectionNumberDAO numberDAO;

    @Inject
    private CollectionSecurityService securityService;

    @Inject
    private SelectModelFactory selectModelFactory;

    @Inject
    private Messages messages;

    @Component(id = "reserve_collection_form")
    private Form form;

    /* FORMULAR VALUES */

    /**
     * Contains the {@link LibraryLocation} with which the new {@link ReserveCollection} should be associated to.
     */
    @Property
    @Validate("required")
    private LibraryLocation libraryLocation;

    @Property
    @Validate("required")
    private MCRCategory origin;

    @Property
    @Validate("required")
    private String title;

    @Property
    private String alephSystemId;

    @Property
    private String alephUserId;

    @Property
    private String comment;

    @Property
    @Validate("required")
    private Calendar expiry;

    @Property
    private boolean mediaDownloadAllowed;

    @Property
    @Validate("required")
    private String readKey;

    @Property
    private String writeKey;
    
    /* END FORMULAR VALUES */

    @Component(id = "authorNameField")
    private TextField authorNameField;

    @Component(id = "readKey")
    private TextField readKeyField;

    private ReserveCollectionNumber number;

    @SetupRender
    @RequiresActionPermission(ActionDefinition.CREATE_RESERVE_COLLECTION)
    public void onSetupRender() {
        mediaDownloadAllowed = true;
    }


    /**
     * Returns the author name of the current user authenticated by this application.
     *
     * @return the author name
     */
    public String getAuthorName() {
        User loggedInUser = securityService.getCurrentUser();
        return loggedInUser != null
                ? loggedInUser.getRealname()
                : "";
    }

    /**
     * Dummy-method
     *
     * @param name dummy
     */
    public void setAuthorName(String name) {
        // do not set author name
    }


    /**
     * Triggered on Validation of the Form Elements
     */
    @OnEvent(value = EventConstants.VALIDATE, component = "reserve_collection_form")
    void onFormValidate() {
        User loggedInUser = securityService.getCurrentUser();

        log.debug("creating number for location " + libraryLocation + " and user " + loggedInUser);
        if (loggedInUser == null) {
            form.recordError(authorNameField, messages.get("error.msg.login.create.collection"));
            return;
        }

        if (libraryLocation != null) {

            try {
                number = collectionService.buildNumber(libraryLocation);
            } catch (CommitException e) {
                log.error("could not create number; cause: " + e.getMessage());
                form.recordError(messages.get("msg.could.not.create.number"));
            }
        }

        readKey = StringUtils.isNotBlank(readKey)
                ? readKey.trim()
                : readKey;
        writeKey = writeKey != null
                ? writeKey.trim()
                : StringUtils.EMPTY;
        if (StringUtils.equals(readKey, writeKey)) {
            form.recordError(readKeyField, messages.get("error.msg.identical.keys"));
        }
    }

    /**
     * Called after form to create new reserve collection was submitted. here, the reservecollection is finally created
     *
     * @return Page to show after submit.
     * @throws ConfigurationException
     */
    @OnEvent(EventConstants.SUCCESS)
    Object afterFormSubmit() throws ConfigurationException {

        collection = new ReserveCollection();
        collection.setCreated(new Date());
        collection.setModified(new Date());
        collection.setLibraryLocation(libraryLocation);
        collection.setOriginId(origin.getInternalId());
        collection.setTitle(title);
        collection.setValidTo(expiry.getTime());
        collection.setAlephSystemId(alephSystemId);
        collection.setAlephUserId(alephUserId);
        collection.setComment(comment);
        collection.setReadKey(readKey);
        collection.setWriteKey(writeKey);
        collection.setStatus(ReserveCollectionStatus.NEW);
        collection.setNumber(number);
        collection.setMediaDownloadAllowed(mediaDownloadAllowed);

        try {
            collectionService.create(collection);
            log.info("reserve collection " + collection + " saved");

            Link viewCollectionLink = linkSource.createPageRenderLinkWithContext(ViewCollection.class,
                    collection.getId());
            return viewCollectionLink;

        } catch (CommitException e) {
            form.recordError(messages.format("error.msg.could.not.commit.collection", collection.getTitle()));
            return this;
        }
    }

    /**
     * Returns the {@link SelectModel} that is used inside html select box to set the {@link LibraryLocation} for the
     * new reserve collection.
     *
     * @return the select model
     */
    public SelectModel getLibraryLocationSelectModel() {
        return new LibraryLocationSelectModel(libraryLocationDao);
    }

    /**
     * Returns the {@link SelectModel} that is used inside html select box to set the origin as {@link MCRCategory} for
     * the new reserve collection.
     *
     * @return the select model
     */
    public SelectModel getOriginSelectModel() {
        return new OriginSelectModel(originDao, locale);
    }

    /**
     * Returns the {@link SelectModel} that is used inside html select box to set the expiry for the new reserve
     * collection.
     *
     * @return the select model
     */
    public SelectModel getExpirySelectModel() {
        try {
            return new ExpirySelectModel(collectionService.getCollectionExpiryDates(), messages);
        } catch (ConfigurationException e) {
            log.error("configuration error", e);
            return null;
        }
    }


    /**
     * Returns the {@link ValueEncoder} to encode/decode {@link LibraryLocation} objects to use them inside html pages.
     *
     * @return the location value encoder
     */
    public ValueEncoder<LibraryLocation> getLibraryLocationEncoder() {
        return new LibraryLocationValueEncoder(libraryLocationDao);
    }

    public ValueEncoder<MCRCategory> getOriginEncoder() {
        return new MCRCategoryValueEncoder(mcrcategoryDao);
    }

    public ValueEncoder<Calendar> getTimeEncoder() {
        return new TimeValueEncoder();
    }
}
