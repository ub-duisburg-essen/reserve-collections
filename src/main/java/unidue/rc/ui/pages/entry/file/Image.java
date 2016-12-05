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
package unidue.rc.ui.pages.entry.file;


import org.apache.shiro.authz.AuthorizationException;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.HttpError;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.slf4j.Logger;
import se.unbound.tapestry.breadcrumbs.BreadCrumb;
import se.unbound.tapestry.breadcrumbs.BreadCrumbList;
import unidue.rc.dao.ResourceDAO;
import unidue.rc.model.Resource;
import unidue.rc.security.CollectionSecurityService;
import unidue.rc.ui.SecurityContextPage;
import unidue.rc.ui.ProtectedPage;
import unidue.rc.ui.ResourcePageUtil;
import unidue.rc.ui.pages.Media;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Nils Verheyen
 * @since 15.10.14 11:09
 */
@BreadCrumb(titleKey = "text")
@ProtectedPage
public class Image implements SecurityContextPage {

    @Inject
    private Logger log;

    @Inject
    private PageRenderLinkSource linkSource;

    @Inject
    private Messages messages;

    @Inject
    private ResourceDAO resourceDAO;

    @Property
    private Resource image;

    @SessionState
    private BreadCrumbList breadCrumbList;

    @SetupRender
    public void beginRender() {
        breadCrumbList.getLastCrumb().setTitle(image.getFileName());
    }

    @OnEvent(EventConstants.ACTIVATE)
    Object onActivate(Integer resourceID) {
        image = resourceDAO.get(Resource.class, resourceID);
        return image == null || !image.isFileAvailable()
               ? new HttpError(HttpServletResponse.SC_NOT_FOUND, messages.get("not.found"))
               : null;
    }

    @OnEvent(EventConstants.PASSIVATE)
    Object onPassivate() {
        return image.getId();
    }

    public Link getOriginalImage() {
        return linkSource.createPageRenderLinkWithContext(Media.class, image.getId());
    }

    @Override
    public void checkPermission(CollectionSecurityService securityService, EventContext activationContext) throws AuthorizationException {
        ResourcePageUtil.checkPermission(securityService, activationContext, resourceDAO);
    }

}
