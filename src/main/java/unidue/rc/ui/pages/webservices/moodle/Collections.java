package unidue.rc.ui.pages.webservices.moodle;

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

import miless.model.User;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.RequestGlobals;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.slf4j.Logger;
import unidue.rc.dao.ReserveCollectionDAO;
import unidue.rc.dao.ResourceDAO;
import unidue.rc.dao.RoleDAO;
import unidue.rc.dao.UserDAO;
import unidue.rc.model.ActionDefinition;
import unidue.rc.model.ReserveCollection;
import unidue.rc.plugins.moodle.CollectionMapper;
import unidue.rc.plugins.moodle.DecryptedRequestData;
import unidue.rc.plugins.moodle.model.CollectionList;
import unidue.rc.plugins.moodle.model.GetCollectionsRequest;
import unidue.rc.plugins.moodle.model.Request;
import unidue.rc.plugins.moodle.services.MoodleRequestHandler;
import unidue.rc.plugins.moodle.services.MoodleService;
import unidue.rc.plugins.moodle.services.MoodleWebService;
import unidue.rc.security.CollectionSecurityService;
import unidue.rc.system.SystemConfigurationService;
import unidue.rc.ui.RequestError;

import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by nils on 15.06.15.
 */
public class Collections implements MoodleWebService<GetCollectionsRequest> {

    @Inject
    private Logger log;

    @Inject
    private ReserveCollectionDAO collectionDAO;

    @Inject
    private ResourceDAO resourceDAO;

    @Inject
    private RoleDAO roleDAO;

    @Inject
    private UserDAO userDAO;

    @Inject
    private RequestGlobals globals;

    @Inject
    private MoodleService moodleService;


    @Inject
    private CollectionSecurityService securityService;

    @Inject
    private MoodleRequestHandler requestHandler;

    @Inject
    private SystemConfigurationService config;

    private GetCollectionsRequest requestData;
    private User user;

    StreamResponse onActivate() {

        return requestHandler.handle(globals, this);
    }

    @Override
    public GetCollectionsRequest deserialize(DecryptedRequestData decryptedRequestData) throws XMLStreamException {

        // deserialize request data
        try {

            Serializer serializer = new Persister();
            requestData = serializer.read(GetCollectionsRequest.class, decryptedRequestData.getRequestData());
            return requestData;
        } catch (Exception e) {
            throw new XMLStreamException("could not read GetCollectionsRequest", e);
        }
    }

    @Override
    public boolean isSecretValid() {
        return requestData.getSecret().equals(config.getString("moodle.secret"));
    }

    @Override
    public void loadResources() throws RequestError {
        // get necessary objects
        user = userDAO.getUser(requestData.getUsername(), moodleService.getRealm(requestData.getAuthtype()));
        if (user == null) {
            throw new RequestError(HttpServletResponse.SC_NOT_FOUND ,
                    "user " + requestData.getUsername() + " of realm " + requestData.getAuthtype() + " not found");
        }
    }

    @Override
    public void checkPermission(GetCollectionsRequest request) throws AuthorizationException {
    }

    @Override
    public String execute() throws IOException {

        List<ReserveCollection> collections = collectionDAO.getCollections(user, ActionDefinition.EDIT_ENTRIES);

        CollectionList map = CollectionMapper.map(resourceDAO, collections);

        Serializer serializer = new Persister();
        try (StringWriter writer = new StringWriter()) {
            serializer.write(map, writer);
            return writer.toString();
        } catch (Exception e) {
            throw new IOException("could not write collection list", e);
        }
    }
}
