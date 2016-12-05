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
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.HttpError;
import org.slf4j.Logger;
import unidue.rc.dao.ResourceDAO;
import unidue.rc.io.AttachmentStreamResponse;
import unidue.rc.io.InlineStreamResponse;
import unidue.rc.model.Resource;
import unidue.rc.security.CollectionSecurityService;
import unidue.rc.system.BaseURLService;
import unidue.rc.ui.ResourcePageUtil;
import unidue.rc.ui.SecurityContextPage;
import unidue.rc.ui.ProtectedPage;
import unidue.rc.workflow.ResourceService;

import javax.servlet.http.HttpServletResponse;


/**
 * <p> A <code>Download</code> page is able to perform downloads of {@link unidue.rc.model.Resource}s. To perform a
 * download the resource id must be given inside this pages activation context and a download methodto use. Possible
 * values are:
 * </p>
 * <ul>
 *     <li>attachment</li>
 *     <li>inline</li>
 * </ul>
 *
 * Example use: {@code <t:pagelink page="entry/download" context="[resource.id,'attachment']">Download</t:pagelink>}
 *
 * @author Nils Verheyen
 * @since 25.08.14 09:27
 */
@ProtectedPage
public class Download implements SecurityContextPage {

    @Inject
    private Logger log;

    @Inject
    private Messages messages;

    @Inject
    private ResourceDAO resourceDAO;

    @Inject
    private ResourceService resourceService;

    @Property
    private Resource resource;

    @OnEvent(EventConstants.ACTIVATE)
    Object onActivate(Integer resourceID, BaseURLService.DownloadMethod downloadMethod) {
        return getStreamResponse(resourceID, downloadMethod);
    }

    @OnEvent(EventConstants.ACTIVATE)
    Object onActivate(Integer resourceID, BaseURLService.DownloadMethod downloadMethod, String filename) {
        return getStreamResponse(resourceID, downloadMethod);
    }

    private Object getStreamResponse(Integer resourceID, BaseURLService.DownloadMethod downloadMethod) {
        resource = resourceDAO.get(Resource.class, resourceID);
        if (resource == null || !resource.isFileAvailable())
            return new HttpError(HttpServletResponse.SC_NOT_FOUND, messages.get("not.found"));

        java.io.File file = resourceService.download(resource);

        return BaseURLService.DownloadMethod.Attachment.equals(downloadMethod)
                 ? new AttachmentStreamResponse(file, resource)
                 : new InlineStreamResponse(file, resource);
    }

    @OnEvent(EventConstants.PASSIVATE)
    Object onPassivate() {
        return resource.getId();
    }

    @Override
    public void checkPermission(CollectionSecurityService securityService, EventContext activationContext) throws AuthorizationException {
        ResourcePageUtil.checkPermission(securityService, activationContext, resourceDAO);
    }
}
