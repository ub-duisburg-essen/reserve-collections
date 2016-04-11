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
package unidue.rc.ui.pages.webservices.moodle;


import org.apache.shiro.authz.AuthorizationException;
import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.RequestGlobals;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.slf4j.Logger;
import unidue.rc.dao.ResourceDAO;
import unidue.rc.model.Resource;
import unidue.rc.plugins.moodle.DecryptedRequestData;
import unidue.rc.plugins.moodle.model.Request;
import unidue.rc.plugins.moodle.model.ResourceRequest;
import unidue.rc.plugins.moodle.services.MoodleRequestHandler;
import unidue.rc.plugins.moodle.services.MoodleService;
import unidue.rc.plugins.moodle.services.MoodleWebService;
import unidue.rc.system.SystemConfigurationService;
import unidue.rc.ui.RequestError;

import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.StringWriter;

/**
 * Created by nils on 17.06.15.
 */
public class FileRequest implements MoodleWebService {

    @Inject
    private Logger log;

    @Inject
    private ResourceDAO resourceDAO;

    @Inject
    private RequestGlobals globals;

    @Inject
    private PageRenderLinkSource linkSource;

    @Inject
    private MoodleRequestHandler requestHandler;

    @Inject
    private MoodleService moodleService;

    @Inject
    private SystemConfigurationService config;

    private ResourceRequest requestData;
    private Resource resource;

    StreamResponse onActivate() {
        return requestHandler.handle(globals, this);
    }

    @Override
    public Request deserialize(DecryptedRequestData decryptedRequestData) throws XMLStreamException {
        // deserialize request data
        try {
            Serializer serializer = new Persister();
            log.debug(decryptedRequestData.getRequestData());
            requestData = serializer.read(ResourceRequest.class, decryptedRequestData.getRequestData());
            return requestData;
        } catch (Exception e) {
            log.error("could not deserialize request data", e);
            throw new XMLStreamException("could not deserialize request data");
        }
    }

    @Override
    public boolean isSecretValid() {
        return requestData.getSecret().equals(config.getString("moodle.secret"));
    }

    @Override
    public void loadResources() throws RequestError {
        resource = resourceDAO.get(Resource.class, requestData.getResourceID());
        if (resource == null
                || resource.getEntry() == null
                || resource.getEntry().isDeleted()) {
            throw new RequestError(HttpServletResponse.SC_NOT_FOUND, "resource " + requestData.getResourceID() + " not found");
        }
    }

    @Override
    public void checkPermission(Request request) throws AuthorizationException {
    }

    @Override
    public String execute() throws RequestError, IOException {

        // cache download uri
        String sessionID = moodleService.cacheResourceRequest(requestData);
        String downloadURI = linkSource.createPageRenderLinkWithContext(Download.class, sessionID).toAbsoluteURI();

        Serializer serializer = new Persister();
        try (StringWriter writer = new StringWriter()) {
            serializer.write(downloadURI, writer);
            return writer.toString();
        } catch (Exception e) {
            throw new IOException("could not write collection list", e);
        }
    }
}
