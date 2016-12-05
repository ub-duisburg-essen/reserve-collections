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
package unidue.rc.ui.pages;


import org.apache.shiro.authz.AuthorizationException;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.HttpError;
import org.apache.tapestry5.services.RequestGlobals;
import unidue.rc.dao.ResourceDAO;
import unidue.rc.io.InlineStreamResponse;
import unidue.rc.io.RangeOutputStreamResponse;
import unidue.rc.model.Resource;
import unidue.rc.security.CollectionSecurityService;
import unidue.rc.ui.ProtectedPage;
import unidue.rc.ui.SecurityContextPage;
import unidue.rc.ui.ResourcePageUtil;
import unidue.rc.workflow.ResourceService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;

/**
 * A <code>Media</code> page can be used serve contents of {@code Resource} objects as {@link
 * unidue.rc.io.RangeOutputStreamResponse}. The response is returned on activation of this page. Use it as {@code
 * <source src="[pagelink]" type="[type]"/>} to specify content if <code>video</code> and <code>audio</code> elements in
 * html.
 *
 * @author Nils Verheyen
 * @since 04.09.14 10:17
 */
@ProtectedPage
public class Media implements SecurityContextPage {

    @Inject
    private RequestGlobals globals;

    @Inject
    private Messages messages;

    @Inject
    private ResourceDAO resourceDAO;

    @Inject
    private ResourceService resourceService;

    private Integer resourceID;

    @OnEvent(EventConstants.ACTIVATE)
    Object onActivate(Integer resourceID) {


        this.resourceID = resourceID;
        Resource resource = resourceDAO.get(Resource.class, resourceID);
        if (resource == null || !resource.isFileAvailable())
            return new HttpError(HttpServletResponse.SC_NOT_FOUND, messages.get("not.found"));

        HttpServletRequest request = globals.getHTTPServletRequest();
        HttpServletResponse response = globals.getHTTPServletResponse();

        final String mimeType = resource.getMimeType();
        final String mimeCategory = mimeType != null
                ? mimeType.substring(0, mimeType.indexOf("/"))
                : null;

        if (mimeCategory != null) {

            File file = resourceService.download(resource);
            switch (mimeCategory) {
                case "audio":
                case "video":
                    return new RangeOutputStreamResponse(file,
                            resource.getMimeType(), request, response);
                default:
                    return new InlineStreamResponse(file, resource);
            }
        }
        return null;
    }

    @OnEvent(EventConstants.PASSIVATE)
    Object onPassivate() {
        return resourceID;
    }

    @Override
    public void checkPermission(CollectionSecurityService securityService, EventContext activationContext) throws AuthorizationException {
        ResourcePageUtil.checkPermission(securityService, activationContext, resourceDAO);
    }
}
