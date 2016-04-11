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


import miless.model.User;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Service;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.RequestGlobals;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.slf4j.Logger;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.ResourceDAO;
import unidue.rc.dao.UserDAO;
import unidue.rc.model.ActionDefinition;
import unidue.rc.model.Entry;
import unidue.rc.model.Resource;
import unidue.rc.plugins.moodle.CollectionMapper;
import unidue.rc.plugins.moodle.DecryptedRequestData;
import unidue.rc.plugins.moodle.model.DeleteRequest;
import unidue.rc.plugins.moodle.model.File;
import unidue.rc.plugins.moodle.services.MoodleRequestHandler;
import unidue.rc.plugins.moodle.services.MoodleService;
import unidue.rc.plugins.moodle.services.MoodleWebService;
import unidue.rc.security.CollectionSecurityService;
import unidue.rc.system.SystemConfigurationService;
import unidue.rc.ui.RequestError;
import unidue.rc.workflow.EntryService;

import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.StringWriter;

/**
 * Created by nils on 28.09.15.
 */
public class Delete implements MoodleWebService<DeleteRequest> {

    @Inject
    private Logger log;

    @Inject
    private MoodleService moodleService;

    @Inject
    private SystemConfigurationService config;

    @Inject
    private RequestGlobals globals;

    @Inject
    private MoodleRequestHandler requestHandler;

    @Inject
    @Service(ResourceDAO.SERVICE_NAME)
    private ResourceDAO resourceDAO;

    @Inject
    @Service(UserDAO.SERVICE_NAME)
    private UserDAO userDAO;

    @Inject
    private EntryService entryService;

    @Inject
    private CollectionSecurityService securityService;

    private DeleteRequest deleteRequest;
    private Resource resource;
    private User user;

    @OnEvent(EventConstants.ACTIVATE)
    StreamResponse onActivate() {
        return requestHandler.handle(globals, this);
    }

    @Override
    public DeleteRequest deserialize(DecryptedRequestData decryptedRequestData) throws XMLStreamException {
        // deserialize request data
        try {
            Serializer serializer = new Persister();
            deleteRequest = serializer.read(DeleteRequest.class, decryptedRequestData.getRequestData());
            return deleteRequest;
        } catch (Exception e) {
            throw new XMLStreamException("could not read DeleteRequest", e);
        }
    }

    @Override
    public boolean isSecretValid() {
        return deleteRequest.getSecret().equals(config.getString("moodle.secret"));
    }

    @Override
    public void loadResources() throws RequestError {
        resource = resourceDAO.get(Resource.class, deleteRequest.getResourceID());
        if (resource == null || resource.getEntry() == null)
            throw new RequestError(HttpServletResponse.SC_NOT_FOUND, "resource with id " +
                    deleteRequest.getResourceID() + " not found");

        user = userDAO.getUser(deleteRequest.getUsername(), moodleService.getRealm(deleteRequest.getAuthtype()));
        if (user == null) {
            throw new RequestError(HttpServletResponse.SC_NOT_FOUND ,
                    "user " + deleteRequest.getUsername() + " of realm " + deleteRequest.getAuthtype() + " not found");
        }

    }

    @Override
    public void checkPermission(DeleteRequest request) {
        Entry entry = resource.getEntry();
        if (!securityService.isPermitted(ActionDefinition.EDIT_ENTRIES, user.getId(), entry.getReserveCollection().getId()))
            throw new AuthorizationException("delete of resource " + resource.getId() + " not allowed");
    }

    @Override
    public String execute() throws RequestError, IOException {
        Entry entry = resource.getEntry();

        try {
            entryService.delete(entry);
        } catch (CommitException e) {
            log.error("could not delete entry " + entry, e);
            throw new IOException("could not delete entry " + entry);
        }

        Serializer serializer = new Persister();
        try (StringWriter writer = new StringWriter()) {
            File responseObject = CollectionMapper.map(resource);
            serializer.write(responseObject, writer);
            return writer.toString();
        } catch (Exception e) {
            throw new IOException("could not write collection list", e);
        }
    }
}
