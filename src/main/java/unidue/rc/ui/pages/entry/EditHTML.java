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
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Import;
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
import unidue.rc.dao.EntryDAO;
import unidue.rc.dao.HeadlineDAO;
import unidue.rc.model.*;
import unidue.rc.security.CollectionSecurityService;
import unidue.rc.ui.ProtectedPage;
import unidue.rc.ui.SecurityContextPage;
import unidue.rc.ui.pages.collection.ViewCollection;
import unidue.rc.workflow.EntryService;

/**
 * Within this page a Text / HTML Entry can be added to an existing  {@link unidue.rc.model.ReserveCollection}.
 *
 * @author Marcus Koesters
 */
@BreadCrumb(titleKey = "edit.html")
@ProtectedPage
public class EditHTML implements SecurityContextPage {

    @Inject
    private Logger log;

    /**
     * The {@link unidue.rc.model.ReserveCollection} edited within this page. The {@link
     * org.apache.tapestry5.annotations.Persist} annotation has to be present here, so the object is page persisted.
     *
     * @see <a href="http://tapestry.apache.org/persistent-page-data.html">Persistent Page Data</a>
     */
    @Property
    private ReserveCollection collection;

    @Inject
    @Service(HeadlineDAO.SERVICE_NAME)
    private HeadlineDAO headlineDAO;

    @Property
    private Headline headline;

    @Inject
    @Service(EntryDAO.SERVICE_NAME)
    private EntryDAO entryDAO;

    @Inject
    private PageRenderLinkSource linkSource;

    @Property
    private Html html;

    @Inject
    private Messages messages;

    @Component(id = "html_form")
    private Form form;

    public String getTitle() {
        return html.getEntry().getReserveCollection().getTitle();
    }

    @OnEvent(EventConstants.ACTIVATE)
    void onActivate(Integer entryId) {

        log.info("loading entry " + entryId);
        html = entryDAO.get(Html.class, entryId);
        collection = html.getEntry().getReserveCollection();
        headline = html.getEntry().getAssignedHeadline();
    }

    @OnEvent(EventConstants.SUCCESS)
    Object onHTMLSubmitted() {

        try {
            entryDAO.update(html);
            log.info("html entry " + html + " updated");
            if (headline != null)
                headlineDAO.move(html.getEntry(), headline);
            Link viewCollectionLink = linkSource.createPageRenderLinkWithContext(ViewCollection.class,
                    collection.getId());

            return viewCollectionLink;
        } catch (CommitException e) {
            form.recordError(messages.format("error.msg.could.not.commit.html", html));
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
        return html.getId();
    }


    @Override
    public void checkPermission(CollectionSecurityService securityService, EventContext activationContext) throws AuthorizationException {
        Integer htmlID = activationContext.get(Integer.class, 0);
        Html html = entryDAO.get(Html.class, htmlID);
        securityService.checkPermission(ActionDefinition.EDIT_ENTRIES, html.getEntry().getReserveCollection().getId());
    }

}