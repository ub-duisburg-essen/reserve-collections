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
import org.apache.commons.fileupload.FileUploadException;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.annotations.Service;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.RequestGlobals;
import org.apache.tapestry5.upload.services.MultipartDecoder;
import org.apache.tapestry5.upload.services.UploadedFile;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.slf4j.Logger;
import unidue.rc.dao.*;
import unidue.rc.model.ActionDefinition;
import unidue.rc.model.ReserveCollection;
import unidue.rc.model.Resource;
import unidue.rc.plugins.moodle.CollectionMapper;
import unidue.rc.plugins.moodle.DecryptedRequestData;
import unidue.rc.plugins.moodle.model.File;
import unidue.rc.plugins.moodle.model.UploadRequest;
import unidue.rc.plugins.moodle.services.MoodleRequestHandler;
import unidue.rc.plugins.moodle.services.MoodleService;
import unidue.rc.plugins.moodle.services.MoodleWebService;
import unidue.rc.security.CollectionSecurityService;
import unidue.rc.system.SystemConfigurationService;
import unidue.rc.ui.RequestError;
import unidue.rc.workflow.ResourceService;

import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.StringWriter;

/**
 * Created by nils on 22.09.15.
 */
public class Upload implements MoodleWebService<UploadRequest> {

    @Inject
    private Logger log;

    @Inject
    private MoodleService moodleService;

    @Inject
    private RequestGlobals globals;

    @Inject
    private MultipartDecoder multipartDecoder;

    @Inject
    private SystemConfigurationService config;

    @Inject
    @Service(ReserveCollectionDAO.SERVICE_NAME)
    private ReserveCollectionDAO collectionDAO;

    @Inject
    @Service(EntryDAO.SERVICE_NAME)
    private EntryDAO entryDAO;

    @Inject
    @Service(UserDAO.SERVICE_NAME)
    private UserDAO userDAO;

    @Inject
    private ResourceService resourceService;

    @Inject
    private CollectionSecurityService securityService;

    @Inject
    private MoodleRequestHandler requestHandler;

    private UploadRequest requestData;
    private UploadedFile upload;
    private ReserveCollection collection;
    private User user;

    StreamResponse onActivate() {

        return requestHandler.handle(globals, this);
    }

    @Override
    public UploadRequest deserialize(DecryptedRequestData decryptedRequestData) throws XMLStreamException {

        // decrypt request data
        try {
            Serializer serializer = new Persister();
            requestData = serializer.read(UploadRequest.class, decryptedRequestData.getRequestData());
            return requestData;
        } catch (Exception e) {
            throw new XMLStreamException("could not read UploadRequest", e);
        }
    }

    @Override
    public boolean isSecretValid() {
        return requestData.getSecret().equals(config.getString("moodle.secret"));
    }

    @Override
    public void loadResources() throws RequestError {
        upload = multipartDecoder.getFileUpload("file");
        if (upload == null) {
            FileUploadException uploadException = multipartDecoder.getUploadException();
            if (uploadException != null)
                log.error("could not get file from request", uploadException);
            throw new RequestError(HttpServletResponse.SC_BAD_REQUEST, "could not get file from request");
        }

        collection = collectionDAO.get(ReserveCollection.class, requestData.getCollectionID());
        if (collection == null) {
            throw new RequestError(HttpServletResponse.SC_NOT_FOUND, "collection with id " + requestData.getCollectionID() + " does not exist");
        }
        user = userDAO.getUser(requestData.getUsername(), moodleService.getRealm(requestData.getAuthtype()));
        if (user == null) {
            throw new RequestError(HttpServletResponse.SC_NOT_FOUND,
                    "user " + requestData.getUsername() + " of realm " + requestData.getAuthtype() + " not found");
        }
    }

    @Override
    public void checkPermission(UploadRequest request) throws AuthorizationException {

        if (!securityService.isPermitted(ActionDefinition.EDIT_ENTRIES, user.getId(), collection.getId()))
            throw new AuthorizationException("upload to collection " + collection.getId() + " not allowed");
    }

    @Override
    public String execute() throws RequestError, IOException {
        unidue.rc.model.File file = new unidue.rc.model.File();
        Resource resource;
        try {
            entryDAO.createEntry(file, collection);
            resource = resourceService.create(requestData.getFilename(), upload.getStream(), file);
        } catch (CommitException e) {
            try {
                entryDAO.delete(file.getEntry());
            } catch (DeleteException e1) {
                log.error("could not delete entry of file " + file, e1);
            }
            throw new RequestError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "could not create file entry");
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
