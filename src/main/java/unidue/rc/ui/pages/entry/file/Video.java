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
import org.slf4j.Logger;
import se.unbound.tapestry.breadcrumbs.BreadCrumb;
import se.unbound.tapestry.breadcrumbs.BreadCrumbList;
import unidue.rc.dao.ResourceDAO;
import unidue.rc.model.Resource;
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
@Import(library = {
        "context:vendor/jwplayer-5.10/jwplayer.js",
        "context:js/jwplayer.js",
}, stylesheet = {
        "context:vendor/video-js/video-js.min.css"
})
@BreadCrumb(titleKey = "play.video")
@ProtectedPage
public class Video implements SecurityContextPage {

    @Inject
    private Logger log;

    @Inject
    private SystemConfigurationService config;

    @Inject
    @Path("context:vendor/jwplayer-5.10/player.swf")
    @Property
    private Asset jwPlayer;

    @Inject
    @Path("context:vendor/jwplayer-5.10/skins/modieus.zip")
    @Property
    private Asset jwPlayerSkin;

    @Inject
    private JavaScriptSupport javaScriptSupport;

    @Inject
    private Messages messages;

    @Inject
    private MimeService mimeService;

    @Inject
    private ResourceService resourceService;

    @Inject
    private ResourceDAO resourceDAO;

    @Inject
    private PageRenderLinkSource linkSource;

    @Property
    private Resource video;

    @Persist(PersistenceConstants.FLASH)
    private Map<String, Integer> protocols;

    @Property
    private String protocol;

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

        protocols = new LinkedHashMap<>();
        Iterator<String> keys = config.getKeys("stream.protocol.port");
        while (keys.hasNext()) {
            String key = keys.next();
            protocols.put(key.substring(key.lastIndexOf('.') + 1), config.getInt(key));
        }
        return null;
    }

    @OnEvent(EventConstants.PASSIVATE)
    Object onPassivate() {
        return video.getId();
    }

    @AfterRender
    void afterRender() {
        String sourceURI = getSource("rtmp");
        String streamingMountPoint = config.getString("streaming.server.mount.point");

        try {
            String rtmpSourcePath = null;
            if (!sourceURI.equals('#')) {

                URIBuilder sourceURIBuilder = new URIBuilder(sourceURI);
                URI rtmpSourceURI = sourceURIBuilder.build();
                rtmpSourcePath = rtmpSourceURI.getPath();
                rtmpSourcePath = rtmpSourcePath.substring(streamingMountPoint.length());
                rtmpSourcePath = rtmpSourcePath + '?' + rtmpSourceURI.getQuery();
            }

            javaScriptSupport.addScript(buildJWStartScript(getFallbackURL(), rtmpSourcePath));
        } catch (URISyntaxException e) {
            log.error("could not build uri from " + sourceURI, e);
        }
    }

    private String buildJWStartScript(String htmlSource, String rtmpSource) {
        StringBuilder builder = new StringBuilder("jQuery.fn.startjw({");
        builder.append(String.format("  player: '%s',", jwPlayer.toClientURL()));
        builder.append(String.format("  skin: '%s',", jwPlayerSkin.toClientURL()));

        if (htmlSource != null)
            builder.append(String.format("  html5Source: '%s',", htmlSource));

        if (rtmpSource != null)
            builder.append(String.format("  rtmpSource: '%s',", rtmpSource));

        builder.append("});");
        return builder.toString();
    }

    public String getSource(String protocol) {
        Integer port = protocols.get(protocol);
        return port != null
                ? getVideoSourceURL(video, protocol, port)
                : "#";
    }

    public String getVideoSourceURL(Resource video, String protocol, Integer port) {
        /*
        <source src="${protocol}://${sys:streaming.server.url}:${port}${sys:streaming.server.mount.point}${video.filePath}"
                                type="${mimeType}"/>
         */
        try {
            URI videoURI = new URIBuilder()
                    .setScheme(protocol)
                    .setHost(config.getString("streaming.server.url"))
                    .setPort(port)
                    .setPath(config.getString("streaming.server.mount.point") + video.getFilePath())
                    .build();

            String videoURIStr = videoURI.toString();
            String urlProcessor = config.getString("streaming.server.url.processor");
            return StringUtils.isEmpty(urlProcessor)
                    ? videoURIStr
                    : requestVideoURL(urlProcessor, videoURIStr);
        } catch (URISyntaxException e) {
            log.error("could not build video uri for " + video.getId(), e);
        }
        return null;
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

    public String getFallbackURL() {
        return linkSource.createPageRenderLinkWithContext(Media.class, video.getId(),
                video.getFileName()).toAbsoluteURI();
    }

    public Integer getPort() {
        return mimeService.getStreamingPort(protocol);
    }

    public boolean isDownloadAllowed() {
        return resourceService.isDownloadAllowed(video);
    }

    @Override
    public void checkPermission(CollectionSecurityService securityService, EventContext activationContext) throws AuthorizationException {
        ResourcePageUtil.checkPermission(securityService, activationContext, resourceDAO);
    }
}
