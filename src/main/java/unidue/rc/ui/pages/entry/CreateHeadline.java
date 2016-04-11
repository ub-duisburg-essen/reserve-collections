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
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Persist;
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
import unidue.rc.dao.HeadlineDAO;
import unidue.rc.dao.ReserveCollectionDAO;
import unidue.rc.model.ActionDefinition;
import unidue.rc.model.Headline;
import unidue.rc.model.ReserveCollection;
import unidue.rc.security.RequiresActionPermission;
import unidue.rc.ui.ProtectedPage;
import unidue.rc.ui.pages.collection.ViewCollection;

/**
 * Within this page an existing {@link ReserveCollection} can be edited.
 *
 * @author Marcus Koesters
 */
@BreadCrumb(titleKey = "new.headline")
@ProtectedPage
public class CreateHeadline {

    @Inject
    private PageRenderLinkSource linkSource;

    @Inject
    private Logger log;

    @InjectPage
    private ViewCollection view;

    @Property
    @Validate("required")
    private String heading;

    /**
     * The {@link ReserveCollection} edited within this page. The {@link Persist} annotation has to be present here, so
     * the object is page persisted.
     *
     * @see <a href="http://tapestry.apache.org/persistent-page-data.html">Persistent Page Data</a>
     */
    @Property
    @Persist
    private ReserveCollection collection;

    @Property
    private Headline headline;

    @Inject
    @Service(HeadlineDAO.SERVICE_NAME)
    private HeadlineDAO headlineDAO;

    @Inject
    private ReserveCollectionDAO collectionDAO;

    @Component(id = "headline_form")
    private Form form;

    @Inject
    private Messages messages;

    public String getTitle() {
        return collection.getTitle();
    }


    @OnEvent(EventConstants.ACTIVATE)
    @RequiresActionPermission(ActionDefinition.EDIT_ENTRIES)
    void onActivate(Integer rcId) {

        log.info("loading reserve collection " + rcId);
        collection = collectionDAO.get(ReserveCollection.class, rcId);

    }

    @OnEvent(EventConstants.SUCCESS)
    Object onHeadlineSubmitted() {
        Headline newHeadline = new Headline();
        newHeadline.setText(heading);
        try {
            headlineDAO.createEntry(newHeadline, collection);

            if (headline != null)
                headlineDAO.move(newHeadline.getEntry(), this.headline);

            log.info("headline entry for " + collection + " saved");
            Link viewCollectionLink = linkSource.createPageRenderLinkWithContext(ViewCollection.class,
                    collection.getId());
            return viewCollectionLink;
        } catch (CommitException e) {
            form.recordError(messages.format("error.msg.could.not.commit.headline", newHeadline));
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