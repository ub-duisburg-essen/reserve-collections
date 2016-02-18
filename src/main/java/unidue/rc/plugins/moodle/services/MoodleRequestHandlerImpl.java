package unidue.rc.plugins.moodle.services;

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

import org.apache.cayenne.di.Inject;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.services.RequestGlobals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unidue.rc.io.XMLStreamResponse;
import unidue.rc.plugins.moodle.DecryptedRequestData;
import unidue.rc.plugins.moodle.model.Request;
import unidue.rc.ui.RequestError;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <p>A <code>MoodleRequestHandlerImpl</code> is able to handle a request send to one of the different moodle web services.
 * The {@link MoodleWebService} is used in order:</p>
 * <ul>
 *     <li>{@link MoodleWebService#deserialize(DecryptedRequestData)}</li>
 *     <li>{@link MoodleWebService#isSecretValid()}</li>
 *     <li>{@link MoodleWebService#loadResources()}</li>
 *     <li>{@link MoodleWebService#execute()}</li>
 * </ul>
 */
public class MoodleRequestHandlerImpl implements MoodleRequestHandler {
    
    private static final Logger LOG = LoggerFactory.getLogger(MoodleRequestHandlerImpl.class);

    private static final String TIME_LOG_FORMAT = "%20s: %dms";

    @Inject
    private MoodleService moodleService;
    
    @Override
    public StreamResponse handle(RequestGlobals globals, MoodleWebService webService) {
        long time = System.currentTimeMillis();
        // decrypt request data
        DecryptedRequestData decryptedRequestData;
        try {
            decryptedRequestData = moodleService.decryptRequestData(globals.getHTTPServletRequest());
        } catch (RequestError e) {
            LOG.error("could not decrypt request data", e);
            return createXMLResponseFromObject(e);
        }
        LOG.debug(String.format(TIME_LOG_FORMAT, "decryption in", (System.currentTimeMillis() - time)));
        time = System.currentTimeMillis();

        // deserialize request data
        Request request;
        try {
            request = webService.deserialize(decryptedRequestData);
        } catch (Exception e) {
            LOG.error("could not deserialize request data", e);
            return createXMLResponseFromObject(new RequestError(HttpServletResponse.SC_BAD_REQUEST, "could not deserialize request data " + decryptedRequestData.getRequestData()));
        }
        LOG.debug(String.format(TIME_LOG_FORMAT, "deserialization in", (System.currentTimeMillis() - time)));
        time = System.currentTimeMillis();

        // validate secret
        if (!webService.isSecretValid()) {
            LOG.error("invalid secret used");
            return createXMLResponseFromObject(new RequestError(HttpServletResponse.SC_BAD_REQUEST, "invalid request data"));
        }
        LOG.debug(String.format(TIME_LOG_FORMAT, "validation in", (System.currentTimeMillis() - time)));
        time = System.currentTimeMillis();

        try {
            // load necessary objects
            webService.loadResources();
        } catch (RequestError e) {
            LOG.error("could not load resources");
            return createXMLResponseFromObject(e);
        }
        LOG.debug(String.format(TIME_LOG_FORMAT, "load resource in", (System.currentTimeMillis() - time)));
        time = System.currentTimeMillis();

        try {
            webService.checkPermission(request);
        } catch (AuthorizationException e) {
            LOG.error("request not allowed", e);
            return createXMLResponseFromObject(new RequestError(HttpServletResponse.SC_FORBIDDEN, e.getMessage()));
        }
        LOG.debug(String.format(TIME_LOG_FORMAT, "permission check in", (System.currentTimeMillis() - time)));
        time = System.currentTimeMillis();

        try {
            // create response object
            String responseObject = webService.execute();

            // encrypt response data
            XMLStreamResponse response = moodleService.createResponseObject(responseObject, decryptedRequestData.getSymkey());
            LOG.debug(String.format(TIME_LOG_FORMAT, "response in", (System.currentTimeMillis() - time)));
            return response;
        } catch (RequestError e) {
            LOG.error("could create response object", e);
            return createXMLResponseFromObject(e);
        } catch (IOException e) {
            LOG.error("could create response object", e);
            return createXMLResponseFromObject(new RequestError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "could not write response object"));
        }
    }

    private static XMLStreamResponse createXMLResponseFromObject(Object annotatedObject) {
        return new XMLStreamResponse(annotatedObject);
    }
}
