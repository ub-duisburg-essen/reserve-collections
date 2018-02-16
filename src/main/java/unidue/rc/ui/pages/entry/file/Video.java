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


import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.tapestry5.Asset;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.HttpError;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import se.unbound.tapestry.breadcrumbs.BreadCrumb;
import se.unbound.tapestry.breadcrumbs.BreadCrumbList;
import unidue.rc.dao.ResourceDAO;
import unidue.rc.model.ReserveCollection;
import unidue.rc.model.Resource;
import unidue.rc.plugins.videostreaming.VideoSource;
import unidue.rc.plugins.videostreaming.VideoStreamingService;
import unidue.rc.security.CollectionSecurityService;
import unidue.rc.system.SystemConfigurationService;
import unidue.rc.ui.ProtectedPage;
import unidue.rc.ui.ResourcePageUtil;
import unidue.rc.ui.SecurityContextPage;
import unidue.rc.ui.pages.Media;
import unidue.rc.ui.services.MimeService;
import unidue.rc.workflow.ResourceService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.*;

/**
 * @author Nils Verheyen
 * @since 25.08.14 09:27
 */
@BreadCrumb(titleKey = "play.video")
@ProtectedPage
public class Video implements SecurityContextPage {

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
        if (collection != null)
            pathParams.put("collectionid", Integer.toString(collection.getId()));
        pathParams.put("resourceid", Integer.toString(video.getId()));
        pathParams.put("filename", video.getFileName());

        return streamingService.getSourceURI(videoSource, pathParams).toString();
    }

    private String requestVideoURL(String urlProcessor, String videoURIStr) {
        CloseableHttpClient client = HttpClientBuilder.create().disableRedirectHandling().build();
        HttpClientContext context = HttpClientContext.create();
        try {
            HttpGet get = new HttpGet(urlProcessor + URLEncoder.encode(videoURIStr, "UTF-8"));
            CloseableHttpResponse response = client.execute(get, context);
            Header redirectHeader = response.getFirstHeader("Location");
            String redirectLocation = redirectHeader != null ? redirectHeader.getValue() : null;

            EntityUtils.consume(response.getEntity());
            client.close();
            return redirectLocation;
        } catch (IOException e) {
            log.error("could not get request url from " + urlProcessor + " for " + videoURIStr, e);
        }
        return null;
    }

    public boolean isDownloadAllowed() {
        return resourceService.isDownloadAllowed(video);
    }

    @Override
    public void checkPermission(CollectionSecurityService securityService, EventContext activationContext) throws AuthorizationException {
        ResourcePageUtil.checkPermission(securityService, activationContext, resourceDAO);
    }
}
