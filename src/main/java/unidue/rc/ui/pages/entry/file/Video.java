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


import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.HttpError;
import org.slf4j.Logger;
import se.unbound.tapestry.breadcrumbs.BreadCrumb;
import se.unbound.tapestry.breadcrumbs.BreadCrumbList;
import unidue.rc.dao.ResourceDAO;
import unidue.rc.model.ReserveCollection;
import unidue.rc.model.Resource;
import unidue.rc.plugins.videostreaming.VideoSource;
import unidue.rc.plugins.videostreaming.VideoStreamingService;
import unidue.rc.security.CollectionSecurityService;
import unidue.rc.ui.ProtectedPage;
import unidue.rc.ui.ResourcePageUtil;
import unidue.rc.ui.SecurityContextPage;
import unidue.rc.workflow.ResourceService;

import javax.servlet.http.HttpServletResponse;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Nils Verheyen
 * @since 25.08.14 09:27
 */
@BreadCrumb(titleKey = "play.video")
@ProtectedPage
public class Video implements SecurityContextPage {

    private static final URLCodec URL_CODEC = new URLCodec("UTF-8");

    @Inject
    private Logger log;

    @Inject
    private Messages messages;

    @Inject
    private ResourceService resourceService;

    @Inject
    private VideoStreamingService streamingService;

    @Inject
    private ResourceDAO resourceDAO;

    @Property
    private Resource video;

    @Property
    private List<VideoSource> videoSources;

    @Property
    private VideoSource videoSource;

    @SessionState
    private BreadCrumbList breadCrumbList;

    @SetupRender
    public void beginRender() {
        breadCrumbList.getLastCrumb().setTitle(video.getFileName());
    }

    @OnEvent(EventConstants.ACTIVATE)
    Object onActivate(Integer resourceID) {
        video = resourceDAO.get(Resource.class, resourceID);
        if (video == null || !video.isFileAvailable())
            return new HttpError(HttpServletResponse.SC_NOT_FOUND, messages.get("not.found"));

        videoSources = streamingService.getVideoSources(video.getExtension());

        return null;
    }

    @OnEvent(EventConstants.PASSIVATE)
    Object onPassivate() {
        return video.getId();
    }

    public String getVideoSourceURI() throws URISyntaxException {
        Map<String, String> pathParams = new HashMap<>();
        ReserveCollection collection = resourceService.getCollection(video);

        // collection id parameter
        if (collection != null)
            pathParams.put("collectionid", Integer.toString(collection.getId()));

        // resource id parameter
        pathParams.put("resourceid", Integer.toString(video.getId()));

        // filename parameter
        try {
            pathParams.put("filename", URL_CODEC.encode(video.getFileName()));
        } catch (EncoderException e) {
            log.error("could not url encode video file name", e);
        }

        return streamingService.getSourceURI(videoSource, pathParams).toString();
    }

    public boolean getHasVideoSources() {
        return videoSources != null && !videoSources.isEmpty();
    }

    public boolean isDownloadAllowed() {
        return resourceService.isDownloadAllowed(video);
    }

    @Override
    public void checkPermission(CollectionSecurityService securityService, EventContext activationContext) throws AuthorizationException {
        ResourcePageUtil.checkPermission(securityService, activationContext, resourceDAO);
    }
}
