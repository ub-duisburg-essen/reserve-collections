/**
 * Copyright (C) 2014 - 2016 Universitaet Duisburg-Essen (semapp|uni-due.de)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package unidue.rc.ui.pages.webservices.moodle;


import miless.model.User;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.slf4j.Logger;
import unidue.rc.dao.ResourceDAO;
import unidue.rc.dao.UserDAO;
import unidue.rc.io.AttachmentStreamResponse;
import unidue.rc.model.Resource;
import unidue.rc.plugins.moodle.model.ResourceRequest;
import unidue.rc.plugins.moodle.services.MoodleService;
import unidue.rc.ui.pages.Error403;
import unidue.rc.ui.pages.Error404;
import unidue.rc.workflow.ResourceService;

/**
 * Created by nils on 17.06.15.
 */
public class Download {

    @Inject
    private Logger log;

    @Inject
    private ResourceDAO resourceDAO;

    @Inject
    private UserDAO userDAO;

    @Inject
    private ResourceService resourceService;

    @Inject
    private PageRenderLinkSource linkSource;

    @Inject
    private MoodleService moodleService;

    Object onActivate(String sessionID) {
        ResourceRequest request = moodleService.getResourceRequest(sessionID);
        if (request == null)
            return linkSource.createPageRenderLink(Error403.class);

        Resource resource = resourceDAO.get(Resource.class, request.getResourceID());
        if (resource == null || !resource.isFileAvailable())
            return linkSource.createPageRenderLink(Error404.class);

        User user = userDAO.getUser(request.getUsername(), moodleService.getRealm(request.getAuthtype()));

        java.io.File file = resourceService.download(resource, user);
        return file != null && file.exists()
               ? new AttachmentStreamResponse(file, resource)
               : linkSource.createPageRenderLink(Error404.class);
    }
}
