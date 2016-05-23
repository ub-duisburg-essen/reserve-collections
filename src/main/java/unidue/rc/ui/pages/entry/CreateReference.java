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


import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.ValidationException;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.Service;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.slf4j.Logger;
import se.unbound.tapestry.breadcrumbs.BreadCrumb;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.EntryDAO;
import unidue.rc.dao.HeadlineDAO;
import unidue.rc.dao.ReserveCollectionDAO;
import unidue.rc.model.*;
import unidue.rc.security.RequiresActionPermission;
import unidue.rc.ui.ProtectedPage;
import unidue.rc.ui.pages.collection.ViewCollection;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by nils on 30.06.15.
 */
@BreadCrumb(titleKey = "new.reference")
@ProtectedPage
public class CreateReference {

    @Inject
    private Logger log;

    @Inject
    @Service(EntryDAO.SERVICE_NAME)
    private EntryDAO entryDAO;

    @Inject
    private ReserveCollectionDAO collectionDAO;

    @Inject
    @Service(HeadlineDAO.SERVICE_NAME)
    private HeadlineDAO headlineDAO;

    @Inject
    private Messages messages;

    @Inject
    private PageRenderLinkSource linkSource;

    @Component(id = "reference_form")
    private Form referenceForm;

    @Property
    private ReserveCollection collection;

    @Property
    private Headline headline;

    @Property
    @Validate("required")
    private String referenceTitle;

    @Property
    private String authors;

    @Property
    private String volume;

    @Property
    private String edition;

    @Property
    @Validate("required")
    private String placeOfPublication;

    @Property
    private String publisher;

    @Property
    @Validate("required")
    private Integer yearOfPublication;

    @Property
    private String isbn;

    @Property
    private String fullTextURL;

    @Property
    private String comment;

    @Property
    private String collectionReference;

    @Property
    private String signature;

    private Integer collectionID;

    @OnEvent(EventConstants.ACTIVATE)
    @RequiresActionPermission(ActionDefinition.EDIT_ENTRIES)
    void onActivate(Integer collectionID) {
        this.collectionID = collectionID;

        collection = collectionDAO.get(ReserveCollection.class, collectionID);
    }

    @OnEvent(EventConstants.PASSIVATE)
    Integer onPassivate() {
        return collectionID;
    }

    void onValidateFromFullTextURL(String value) throws ValidationException {
        if (value == null) return;
        try {
            URL test = new URL(value);
        } catch (MalformedURLException e) {
            throw new ValidationException(e.getLocalizedMessage());
        }
    }

    @OnEvent(EventConstants.SUCCESS)
    Object afterFormSubmit() {
        Reference reference = new Reference();
        reference.setTitle(referenceTitle);
        reference.setAuthors(authors);
        reference.setVolume(volume);
        reference.setEdition(edition);
        reference.setPlaceOfPublication(placeOfPublication);
        reference.setPublisher(publisher);
        reference.setYearOfPublication(yearOfPublication);
        reference.setIsbn(isbn);
        reference.setComment(comment);
        reference.setCollectionNumber(collectionReference);
        reference.setSignature(signature);

        try {
            entryDAO.createEntry(reference, collection);
            if (reference != null && headline != null)
                headlineDAO.move(reference.getEntry(), headline);

            if (fullTextURL != null) {
                Resource resource = new Resource();
                resource.setFullTextURL(fullTextURL);
                reference.setResource(resource);
                entryDAO.update(reference);
            }
            Link viewCollectionLink = linkSource.createPageRenderLinkWithContext(ViewCollection.class,
                    collection.getId());
            return viewCollectionLink;
        } catch (CommitException e) {
            referenceForm.recordError(messages.format("error.msg.could.not.commit.reference", reference));
            return null;
        }
    }
}
