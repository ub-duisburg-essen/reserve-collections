/**
 * Copyright (C) 2014 - 2017 Universitaet Duisburg-Essen (semapp|uni-due.de)
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
package unidue.rc.ui.components;


import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.slf4j.Logger;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.DeleteException;
import unidue.rc.model.ReserveCollection;
import unidue.rc.model.ReserveCollectionStatus;
import unidue.rc.ui.pages.Index;
import unidue.rc.ui.pages.collection.ActivateCollection;
import unidue.rc.ui.pages.collection.ViewCollection;
import unidue.rc.workflow.CheckNumberException;
import unidue.rc.workflow.CollectionService;

/**
 * <p> A <code>ReserveCollectionActionsLinkList</code> can be used as a component to render a bootstrap navbar list with
 * links to create a new reserve collection or edit / delete an existing one. </p>
 * <pre>
 *     {@code <t:createentrylinklist collection="..."/>}
 * </pre>
 *
 * @author Marcus Koesters
 * @since 25.09.13 12:15
 */
public class ReserveCollectionActionsLinkList {

    @Inject
    private CollectionService collectionService;

    @Inject
    private ComponentResources resources;

    @Inject
    private PageRenderLinkSource linkSource;

    @Inject
    private Logger log;

    @Parameter(required = false, allowNull = true, name = "collection")
    @Property
    private ReserveCollection collection;

    /**
     * Deactivates the reserve collection managed by this page.
     */
    @OnEvent("renew")
    Object onRenewReserveCollection() {

        try {
            collectionService.renew(collection);
            Link viewCollectionLink = linkSource.createPageRenderLinkWithContext(ViewCollection.class, collection.getId());
            viewCollectionLink.addParameter(ViewCollection.WORKFLOW_PARAM, ReserveCollectionStatus.NEW.name());
            return viewCollectionLink;
        } catch (CommitException e) {
            return resources.getPageName();
        }
    }

    /**
     * Deactivates the reserve collection managed by this page.
     */
    @OnEvent("activate")
    Object onActivateReserveCollection() {

        try {
            collectionService.activate(collection);
            Link viewCollectionLink = linkSource.createPageRenderLinkWithContext(ViewCollection.class, collection.getId());
            viewCollectionLink.addParameter(ViewCollection.WORKFLOW_PARAM, ReserveCollectionStatus.ACTIVE.name());
            return viewCollectionLink;
        } catch (CommitException e) {
            return resources.getPageName();
        } catch (CheckNumberException e) {
            return linkSource.createPageRenderLinkWithContext(ActivateCollection.class, collection.getId());
        }
    }

    /**
     * Deactivates the reserve collection managed by this page.
     */
    @OnEvent("deactivate")
    Object onDeactivateReserveCollection() {

        try {
            collectionService.deactivate(collection);
            Link viewCollectionLink = linkSource.createPageRenderLinkWithContext(ViewCollection.class, collection.getId());
            viewCollectionLink.addParameter(ViewCollection.WORKFLOW_PARAM, ReserveCollectionStatus.DEACTIVATED.name());
            return viewCollectionLink;
        } catch (CommitException e) {
            return resources.getPageName();
        }
    }

    /**
     * Deactivates the reserve collection managed by this page.
     */
    @OnEvent("archive")
    Object onArchiveReserveCollection() {

        try {
            collectionService.archive(collection);
            Link viewCollectionLink = linkSource.createPageRenderLinkWithContext(ViewCollection.class, collection.getId());
            viewCollectionLink.addParameter(ViewCollection.WORKFLOW_PARAM, ReserveCollectionStatus.ARCHIVED.name());
            return viewCollectionLink;
        } catch (CommitException e) {
            return resources.getPageName();
        }
    }

    /**
     * Deletes the reserve collection managed by this page.
     *
     * @return page, which should be displayed after delete
     */
    @OnEvent("delete")
    Object onDeleteReserveCollection() {

        try {
            collectionService.delete(collection);
            return Index.class;
        } catch (DeleteException e) {
            return resources.getPageName();
        }
    }

    public Boolean getActivateable() {
        return collectionService.canActivate(collection);
    }
}
