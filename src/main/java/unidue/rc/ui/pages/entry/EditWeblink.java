package unidue.rc.ui.pages.entry;

/*
 * #%L
 * Semesterapparate
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 Universitaet Duisburg Essen
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.ValidationException;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.Service;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.TextField;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.slf4j.Logger;
import se.unbound.tapestry.breadcrumbs.BreadCrumb;
import unidue.rc.dao.BaseDAO;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.HeadlineDAO;
import unidue.rc.model.ActionDefinition;
import unidue.rc.model.Headline;
import unidue.rc.model.ReserveCollection;
import unidue.rc.model.WebLink;
import unidue.rc.security.CollectionSecurityService;
import unidue.rc.ui.ProtectedPage;
import unidue.rc.ui.SecurityContextPage;
import unidue.rc.ui.pages.collection.ViewCollection;
import unidue.rc.workflow.EntryService;

import java.net.MalformedURLException;
import java.net.URL;

@BreadCrumb(titleKey = "edit.weblink")
@ProtectedPage
public class EditWeblink implements SecurityContextPage {

    @Inject
    private PageRenderLinkSource linkSource;

    @Inject
    private Logger log;

    @Property
    @Persist
    private WebLink weblink;

    @Property
    private ReserveCollection collection;

    @Inject
    @Service(BaseDAO.SERVICE_NAME)
    private BaseDAO baseDAO;

    @Inject
    @Service(HeadlineDAO.SERVICE_NAME)
    private HeadlineDAO headlineDAO;

    @Property
    private Headline headline;

    @Inject
    private Messages messages;

    @Component(id = "edit_weblink_form")
    private Form form;

    @Component(id = "weblinkurl")
    private TextField weblinkField;

    public String getCollectionTitle() {
        return weblink.getEntry().getReserveCollection().getTitle();
    }


    @OnEvent(EventConstants.ACTIVATE)
    void onActivate(Integer entryId) {
        log.info("loading entry " + entryId);
        weblink = baseDAO.get(WebLink.class, entryId);
        log.info("weblink is  " + weblink);
        collection = weblink.getEntry().getReserveCollection();
        headline = weblink.getEntry().getAssignedHeadline();
    }

    void onValidateFromWeblinkurl(String value) throws ValidationException {
        if (StringUtils.isEmpty(value)) {

            form.recordError(weblinkField, messages.format("error.url.not.valid"));
            return;
        }
        // add protocol if necessary
        if (!value.contains("://"))
            value = "http://" + value;

        try {
            new URL(value);
        } catch (MalformedURLException e) {
            throw new ValidationException(messages.format("error.url.not.valid"));
        }
    }

    @OnEvent(EventConstants.SUCCESS)
    Object afterFormSubmit() {

        try {
            baseDAO.update(weblink);
            log.info("weblink entry for " + weblink + " updated");
            if (headline != null)
                headlineDAO.move(weblink.getEntry(), headline);
            Link viewCollectionLink = linkSource.createPageRenderLinkWithContext(ViewCollection.class,
                    collection.getId());

            return viewCollectionLink;
        } catch (CommitException e) {
            form.recordError(messages.format("error.msg.could.not.commit.weblink", weblink));
            return null;
        }
    }

    /**
     * Called when the link for this page is generated.
     *
     * @return
     * @see {@link org.apache.tapestry5.EventConstants#PASSIVATE}
     */
    @OnEvent(EventConstants.PASSIVATE)
    Integer onPassivate() {
        return weblink.getId();
    }

    @Override
    public void checkPermission(CollectionSecurityService securityService, EventContext activationContext) throws AuthorizationException {
        Integer weblinkID = activationContext.get(Integer.class, 0);
        WebLink weblink = baseDAO.get(WebLink.class, weblinkID);
        securityService.checkPermission(ActionDefinition.EDIT_ENTRIES, weblink.getEntry().getReserveCollection().getId());
    }

}
