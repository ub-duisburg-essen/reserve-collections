package unidue.rc.ui.pages.entry;

/*
 * #%L
 * Semesterapparate
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 - 2015 Universitaet Duisburg Essen
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

import org.apache.shiro.authz.AuthorizationException;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.Service;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.upload.services.UploadedFile;
import org.slf4j.Logger;
import se.unbound.tapestry.breadcrumbs.BreadCrumb;
import unidue.rc.dao.BaseDAO;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.HeadlineDAO;
import unidue.rc.model.*;
import unidue.rc.security.CollectionSecurityService;
import unidue.rc.ui.ProtectedPage;
import unidue.rc.ui.SecurityContextPage;
import unidue.rc.ui.components.JournalArticleFormFragment;
import unidue.rc.ui.pages.collection.ViewCollection;
import unidue.rc.workflow.ScannableService;

import java.io.IOException;
import java.util.List;

/**
 * Created by nils on 31.07.15.
 */
@ProtectedPage
@BreadCrumb(titleKey = "duplicate.journal.article")
public class DuplicateJournalArticle implements SecurityContextPage {

    @Inject
    private Logger log;

    @Inject
    private ScannableService scannableService;

    @Inject
    @Service(BaseDAO.SERVICE_NAME)
    private BaseDAO baseDAO;

    @Inject
    @Service(HeadlineDAO.SERVICE_NAME)
    private HeadlineDAO headlineDAO;

    @Inject
    private PageRenderLinkSource linkSource;

    @Inject
    private Messages messages;

    @Component(id = "articleForm")
    private Form form;

    @Component(id = "articleFormFragment")
    private JournalArticleFormFragment articleFormFragment;

    @Property
    private ReserveCollection collection;

    private JournalArticle source;

    @Property
    private JournalArticle duplicate;

    @Property
    private Headline headline;

    @Property
    private String url;

    @OnEvent(EventConstants.ACTIVATE)
    void onActivate(Integer articleID) {
        this.source = baseDAO.get(JournalArticle.class, articleID);
        this.collection = source.getReserveCollection();
        this.duplicate = scannableService.duplicate(source, collection);

        Resource resource = this.source.getResource();
        if (resource != null) {
            url = resource.getFullTextURL();
        }
    }

    @OnEvent(EventConstants.PASSIVATE)
    Integer onPassivate() {
        return source.getId();
    }

    @OnEvent(value = EventConstants.VALIDATE, component = "articleForm")
    void onValidateFromArticle() {

        articleFormFragment.validate(form);
        if (form.getHasErrors())
            return;

        List<UploadedFile> uploads = articleFormFragment.getUploads();
        String url = articleFormFragment.getUrl();
        Headline headline = articleFormFragment.getHeadline();
        try {
            // createResource article article
            scannableService.create(duplicate, collection);

            Resource resource = null;
            // createResource resource if url is present
            if (url != null) {
                resource = scannableService.update(duplicate, url);

            }
            if (uploads != null && !uploads.isEmpty()) {

                UploadedFile uploadedFile = uploads.get(0);

                // resource was created due to a given url
                if (resource != null) {
                    scannableService.update(duplicate, uploadedFile.getFileName(), uploadedFile.getStream());
                } else {
                    // no url is given -> createResource new resource with file contents
                    resource = scannableService.update(duplicate, uploadedFile.getFileName(), uploadedFile
                            .getStream());
                }
            }

            // if url and/or file was given associate article to it
            if (resource != null) {
                duplicate.setResource(resource);
                scannableService.update(duplicate);
            }

            log.info("Journal entry for " + collection + " saved");
            if (duplicate != null && headline != null)
                headlineDAO.move(duplicate.getEntry(), headline);
        } catch (IOException | CommitException e) {
            log.info("could not commit article " + duplicate);
            form.recordError(messages.format("error.msg.could.not.commit.article", duplicate));
        }
    }

    @OnEvent(EventConstants.SUCCESS)
    Object onSuccess() {
        return linkSource.createPageRenderLinkWithContext(ViewCollection.class, collection.getId());
    }

    @Override
    public void checkPermission(CollectionSecurityService securityService, EventContext activationContext) throws AuthorizationException {
        Integer articleID = activationContext.get(Integer.class, 0);
        JournalArticle article = baseDAO.get(JournalArticle.class, articleID);
        securityService.checkPermission(ActionDefinition.EDIT_ENTRIES, article.getEntry().getReserveCollection().getId());
    }
}
