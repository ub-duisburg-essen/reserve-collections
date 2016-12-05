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
package unidue.rc.workflow;


import miless.model.User;
import org.apache.cayenne.Persistent;
import org.apache.cayenne.di.Inject;
import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.DeleteException;
import unidue.rc.dao.ResourceDAO;
import unidue.rc.model.CopyrightReviewStatus;
import unidue.rc.model.Entry;
import unidue.rc.model.FileAccess;
import unidue.rc.model.ReserveCollection;
import unidue.rc.model.Resource;
import unidue.rc.model.ResourceContainer;
import unidue.rc.model.solr.SolrCopyrightView;
import unidue.rc.search.SolrService;
import unidue.rc.security.CollectionSecurityService;
import unidue.rc.system.CryptService;
import unidue.rc.system.SystemConfigurationService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * Created by marcus.koesters on 01.08.14.
 */
public class ResourceServiceImpl implements ResourceService {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceServiceImpl.class);

    @Inject
    private SystemConfigurationService config;

    @Inject
    private SolrService solrService;

    @Inject
    private CryptService cryptService;


    @Inject
    private CollectionSecurityService securityService;

    @Inject
    private ResourceDAO resourceDAO;

    @Override
    public boolean isDownloadAllowed(Resource resource) {
        String mimeType = resource.getMimeType();
        ResourceContainer resourceContainer = resource.getResourceContainer();
        Entry entry = resourceContainer != null
                ? resourceContainer.getEntry()
                : null;
        ReserveCollection collection = entry != null
                ? entry.getReserveCollection()
                : null;
        // download is forbidden if resource is audio or video and download is not allowed in collection
        return !(mimeType != null
                && (mimeType.startsWith("audio") || mimeType.startsWith("video"))
                && collection != null
                && !collection.isMediaDownloadAllowed());
    }

    @Override
    public File download(Resource resource) {

        User currentUser = securityService.getCurrentUser();
        return download(resource, currentUser);
    }

    @Override
    public File download(Resource resource, User user) {
        if (!resource.isFileAvailable())
            return null;

        String filesDir = config.getString("files.store");

        if (user != null) {

            String usernameHash = cryptService.buildSHA256Hash(user.getUsername() + config.getString("hash.salt"));
            String realmHash = cryptService.buildSHA256Hash(user.getRealm() + config.getString("hash.salt"));

            FileAccess access = new FileAccess();
            access.setAccessDate(new Date());
            access.setResource(resource);
            access.setUserhash(usernameHash);
            access.setRealmhash(realmHash);
            try {
                resourceDAO.create(access);
            } catch (CommitException e) {
                LOG.error("could not create file access for resource " + resource.getId(), e);
            }
        } else {
            LOG.warn("could not create file access for resource " + resource.getId() + "; cause: unknown user");
        }

        return new java.io.File(filesDir, resource.getFilePath());
    }

    @Override
    public Resource create(String filename, InputStream input, ResourceContainer container) throws CommitException, IOException {
        Resource resource = resourceDAO.createResourceFromStream(filename, input, container);
        container.setResource(resource);
        resourceDAO.update(container);

        commitResourceToSOLR(resource);

        return resource;
    }

    @Override
    public Resource create(String url, ResourceContainer container) throws CommitException {

        Resource resource = new Resource();
        resource.setFullTextURL(url);
        resource.setCopyrightReviewStatus(CopyrightReviewStatus.NOT_REVIEWED);
        resourceDAO.create(resource);

        container.setResource(resource);
        resourceDAO.update(container);

        return resource;
    }

    @Override
    public Resource create(String url) throws CommitException {
        Resource resource = new Resource();
        resource.setFullTextURL(url);
        resource.setCopyrightReviewStatus(CopyrightReviewStatus.NOT_REVIEWED);
        resourceDAO.create(resource);

        return resource;
    }

    @Override
    public Resource upload(ReserveCollection collection, String filename, InputStream input) throws CommitException, IOException {
        Resource resource = new Resource();
        resource.setCopyrightReviewStatus(CopyrightReviewStatus.NOT_REVIEWED);
        resourceDAO.create(resource);

        save(resource, collection.getId(), filename, input);

        resourceDAO.update(resource);

        return resource;
    }

    @Override
    public void update(Resource resource, String filename, InputStream input) throws CommitException, IOException {

        Entry e = resource.getEntry();
        ReserveCollection collection = e.getReserveCollection();

        save(resource, collection.getId(), filename, input);

        update(resource);
    }

    @Override
    public void update(Resource resource) throws CommitException {
        resourceDAO.update(resource);
        deleteResourceFromSOLR(resource);
        commitResourceToSOLR(resource);
    }

    private void save(Resource resource, int collectionID, String filename, InputStream input) throws IOException, CommitException {
        resourceDAO.deleteFile(resource);
        File outputFile = resourceDAO.createOutputFile(collectionID, resource.getId(), filename);
        FileUtils.copyInputStreamToFile(input, outputFile);

        String filePath = resourceDAO.buildFilePath(collectionID, resource.getId(), filename);
        String mimeType = resourceDAO.detectMimeType(outputFile);

        resource.setFilePath(filePath);
        resource.setMimeType(mimeType);
        resource.setFileDeleted(null);
    }

    @Override
    public void delete(Resource resource) throws DeleteException {

        resourceDAO.delete(resource);
    }

    @Override
    public void afterCollectionUpdate(ReserveCollection collection) {
        rebuildSearchIndex();
    }

    @Override
    public void beforeEntryDelete(Entry entry) {
        Persistent entryValue = entry.getValue();
        if (entryValue instanceof ResourceContainer) {
            ResourceContainer container = (ResourceContainer) entryValue;
            Resource resource = container.getResource();

            if (resource != null) {

                resourceDAO.deleteFile(resource);
                try {
                    resourceDAO.delete(resource);
                    deleteResourceFromSOLR(resource);
                } catch (DeleteException e) {
                    LOG.error("could not delete resource " + resource, e);
                }
            }
        }
    }

    @Override
    public void afterEntryDelete(Entry entry) {
    }

    @Override
    public void setFileDeleted(Resource resource) throws CommitException {
        resource.setFileDeleted(new Date());
        resourceDAO.update(resource);
    }

    @Override
    public ResourceDAO.FileDeleteStatus deleteFile(Resource resource) throws CommitException {
        return resourceDAO.deleteFile(resource);
    }

    private void rebuildSearchIndex() {
        try {
            solrService.fullImport(SolrService.Core.Copyright);
        } catch (SolrServerException | IOException e) {
            LOG.error("could not perform full import", e);
        }
    }

    private void commitResourceToSOLR(Resource resource) {

        Entry entry = resource.getEntry();

        LOG.info("adding CopyrightReviewStatus to SOLR: " + resource);

        ReserveCollection rc = entry.getReserveCollection();

        SolrCopyrightView view = new SolrCopyrightView();
        view.setCollectionNumber(rc.getNumber().getNumber().toString());
        view.setCollectionNumberNumeric(rc.getNumber().getNumber());
        view.setCollectionID(rc.getId());
        view.setCollectionTitle(rc.getTitle());
        view.setEntryID(entry.getId().toString());
        view.setEntryIDNumeric(entry.getId());
        view.setModified(entry.getModified());
        view.setFileName(resource.getFileName());
        view.setMimeType(resource.getMimeType());
        view.setResourceID(resource.getId());

        CopyrightReviewStatus copyrightReviewStatus = resource.getCopyrightReviewStatus() == null
                ? CopyrightReviewStatus.NOT_REVIEWED
                : resource.getCopyrightReviewStatus();
        view.setReviewStatus(copyrightReviewStatus.getValue());

        solrService.addBean(view, SolrService.Core.Copyright);

    }

    private void deleteResourceFromSOLR(Resource resource) {
        solrService.deleteByID(resource, SolrService.Core.Copyright);
    }
}
