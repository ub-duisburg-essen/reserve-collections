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


import org.apache.shiro.authz.AuthorizationException;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.ValidationException;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.slf4j.Logger;
import se.unbound.tapestry.breadcrumbs.BreadCrumb;
import se.unbound.tapestry.breadcrumbs.BreadCrumbList;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.EntryDAO;
import unidue.rc.dao.HeadlineDAO;
import unidue.rc.dao.ReserveCollectionDAO;
import unidue.rc.model.*;
import unidue.rc.security.CollectionSecurityService;
import unidue.rc.security.RequiresActionPermission;
import unidue.rc.ui.ProtectedPage;
import unidue.rc.ui.SecurityContextPage;
import unidue.rc.ui.pages.collection.ViewCollection;
import unidue.rc.workflow.EntryService;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by nils on 30.06.15.
 */
@BreadCrumb(titleKey = "edit.reference")
@ProtectedPage
public class EditReference  implements SecurityContextPage {

    @Inject
    private Logger log;

    @Inject
    @Service(EntryDAO.SERVICE_NAME)
    private EntryDAO entryDAO;

    @Inject
    @Service(ReserveCollectionDAO.SERVICE_NAME)
    private ReserveCollectionDAO collectionDAO;

    @Inject
    @Service(HeadlineDAO.SERVICE_NAME)
    private HeadlineDAO headlineDAO;

    @Inject
    private Messages messages;

    @SessionState
    private BreadCrumbList breadCrumbList;

    @Inject
    private PageRenderLinkSource linkSource;

    @Component(id = "reference_form")
    private Form referenceForm;

    @Property
    private ReserveCollection collection;

    @Property
    private Headline headline;

    @Property
    private Reference reference;

    @Property
    private String fullTextURL;

    private Integer referenceID;

    @OnEvent(EventConstants.ACTIVATE)
    void onActivate(Integer referenceID) {
        this.referenceID = referenceID;

        reference = entryDAO.get(Reference.class, referenceID);
        collection = reference.getEntry().getReserveCollection();
        fullTextURL = reference.getResource() != null ? reference.getResource().getFullTextURL() : null;
        headline = reference.getEntry().getAssignedHeadline();
    }

    @SetupRender
    void onSetupRender() {
        breadCrumbList.getLastCrumb().setTitle(messages.format("edit.reference", reference.getTitle()));
    }

    @OnEvent(EventConstants.PASSIVATE)
    Integer onPassivate() {
        return referenceID;
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

        try {
            if (fullTextURL != null) {
                Resource resource = reference.getResource();
                if (reference.getResource() == null) {
                    resource = new Resource();
                    reference.setResource(resource);
                }
                resource.setFullTextURL(fullTextURL);
            }

            entryDAO.update(reference);
            if (headline != null)
                headlineDAO.move(reference.getEntry(), headline);

            Link viewCollectionLink = linkSource.createPageRenderLinkWithContext(ViewCollection.class,
                    collection.getId());
            viewCollectionLink.setAnchor(reference.getId().toString());
            return viewCollectionLink;
        } catch (CommitException e) {
            referenceForm.recordError(messages.format("error.msg.could.not.commit.reference", reference));
            return null;
        }
    }

    @Override
    public void checkPermission(CollectionSecurityService securityService, EventContext activationContext) throws AuthorizationException {
        Integer referenceID = activationContext.get(Integer.class, 0);
        Reference reference = entryDAO.get(Reference.class, referenceID);
        securityService.checkPermission(ActionDefinition.EDIT_ENTRIES, reference.getEntry().getReserveCollection().getId());
    }
}
