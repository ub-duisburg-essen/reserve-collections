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


import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.solr.common.util.URLUtil;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.ValidationException;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.TextField;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.slf4j.Logger;
import se.unbound.tapestry.breadcrumbs.BreadCrumb;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.EntryDAO;
import unidue.rc.dao.HeadlineDAO;
import unidue.rc.dao.ReserveCollectionDAO;
import unidue.rc.model.ActionDefinition;
import unidue.rc.model.Headline;
import unidue.rc.model.ReserveCollection;
import unidue.rc.model.WebLink;
import unidue.rc.security.RequiresActionPermission;
import unidue.rc.ui.ProtectedPage;
import unidue.rc.ui.pages.collection.ViewCollection;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Marc Gesthuesen
 */
@BreadCrumb(titleKey = "new.weblink")
@ProtectedPage
public class CreateWeblink {

    @Inject
    private Logger log;

    @Inject
    private PageRenderLinkSource linkSource;

    @Property
    private String name;

    @Validate("required")
    @Property
    private String url;

    @Property
    private Headline headline;

    @Property
    private ReserveCollection collection;

    @Inject
    @Service(EntryDAO.SERVICE_NAME)
    private EntryDAO entryDAO;

    @Inject
    @Service(HeadlineDAO.SERVICE_NAME)
    private HeadlineDAO headlineDAO;

    @Inject
    private ReserveCollectionDAO collectionDAO;

    @Component(id = "weblinkurl")
    private TextField weblinkField;

    @Inject
    private Messages messages;

    @Component(id = "create_weblink_form")
    private Form form;

    public String getTitle() {
        return collection.getTitle();
    }

    @OnEvent(EventConstants.ACTIVATE)
    @RequiresActionPermission(ActionDefinition.EDIT_ENTRIES)
    void onActivate(Integer rcId) {

        log.info("loading reserve collection " + rcId);
        collection = collectionDAO.get(ReserveCollection.class, rcId);
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
    Object onSuccess() {
        WebLink weblink = new WebLink();

        weblink.setName(name);
        weblink.setUrl(url);

        try {
            entryDAO.createEntry(weblink, collection);
            log.info("weblink entry for " + collection + " saved");

            if (headline != null)
                headlineDAO.move(weblink.getEntry(), headline);

            Link viewCollectionLink = linkSource.createPageRenderLinkWithContext(
                    ViewCollection.class, collection.getId());
            return viewCollectionLink;
        } catch (CommitException e) {
            form.recordError(messages.format("error.msg.could.not.commit.weblink", url));
            return null;
        }
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
