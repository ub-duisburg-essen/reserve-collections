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


import org.apache.cayenne.BaseContext;
import org.apache.cayenne.DataRow;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.Persistent;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.query.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unidue.rc.dao.*;
import unidue.rc.model.*;
import unidue.rc.search.SolrService;
import unidue.rc.system.SystemConfigurationService;
import unidue.rc.system.SystemMessageService;

import java.io.*;
import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * @author Nils Verheyen
 * @since 05.12.13 09:25
 */
public class ScannableServiceImpl implements ScannableService {

    private static final Logger LOG = LoggerFactory.getLogger(ScannableServiceImpl.class);

    private static final String LINE_SEPARATOR = System.lineSeparator();

    @Inject
    private ScanJobDAO scanJobDAO;

    @Inject
    private EntryDAO entryDAO;

    @Inject
    private ScanJobService scanJobService;

    @Inject
    private ResourceService resourceService;

    @Inject
    private SolrService solrService;

    @Inject
    private SystemConfigurationService config;

    @Inject
    private SystemMessageService messages;

    @Override
    public void create(Scannable scannable, ReserveCollection collection) throws CommitException {
        entryDAO.createEntry(scannable, collection);
        scanJobService.onScannableCreated(scannable);
    }

    @Override
    public void create(Scannable scannable, ReserveCollection collection, Resource resource) throws CommitException {
        entryDAO.createEntry(scannable, collection);
        scannable.setResource(resource);
        entryDAO.update(scannable);

        scanJobService.onScannableCreated(scannable);
    }

    @Override
    public Resource create(Scannable scannable, ReserveCollection collection, String fullTextURL) throws CommitException {
        create(scannable, collection);
        return resourceService.create(fullTextURL, scannable);
    }

    @Override
    public Resource create(Scannable scannable, ReserveCollection collection, String filename, InputStream
            input) throws CommitException, IOException {
        create(scannable, collection);
        return resourceService.create(filename, input, scannable);
    }

    @Override
    public Resource update(Scannable scannable, String fullTextURL) throws CommitException {
        Resource resource = scannable.getResource();

        if (resource == null) {

            resource = resourceService.create(fullTextURL, scannable);
            scannable.setResource(resource);
        }

        update(scannable);
        if (!StringUtils.equals(resource.getFullTextURL(), fullTextURL)) {

            resource.setFullTextURL(fullTextURL);
            resourceService.update(resource);
            scanJobService.onScannableUpdated(scannable);
        }
        return resource;
    }

    @Override
    public Resource update(Scannable scannable, String filename, InputStream input) throws CommitException, IOException {
        Resource resource = scannable.getResource();
        if (resource == null)
            resourceService.create(filename, input, scannable);
        else
            resourceService.update(resource, filename, input);

        update(scannable);
        scanJobService.onScannableUpdated(scannable);
        return resource;
    }

    @Override
    public void update(Scannable scannable) throws CommitException {
        scannable.setModified(new Date());
        scanJobService.onScannableUpdated(scannable);
    }

    @Override
    public void afterCollectionUpdate(ReserveCollection collection) throws CommitException {
        List<Entry> entries = collection.getEntries();
        for (Entry e : entries) {
            Persistent entryValue = e.getValue();
            if (entryValue instanceof Scannable) {
                scanJobService.afterCollectionUpdate(collection);
            }
        }
    }

    @Override
    public void beforeEntryDelete(Entry entry) throws DeleteException {

        Persistent entryValue = entry.getValue();
        if (entryValue instanceof Scannable) {
            scanJobService.beforeScannableDelete((Scannable) entryValue);
        }
    }

    @Override
    public void afterEntryDelete(Entry entry) {
    }

    @Override
    public void afterEntryUpdate(Entry entry) throws CommitException {
        scanJobService.onEntryUpdated(entry);
    }

    @Override
    public <T extends Scannable> T duplicate(T scannable, ReserveCollection collection) {
        T clone = (T) scannable.clone();
        if (!scannable.getReserveCollection().equals(collection)) {
            throw new UnsupportedOperationException("cloning into other collection not allowed");
        }
        return clone;
    }

    @Override
    public void setFileDeleted(Scannable scannable) throws CommitException {
        Resource resource = scannable.getResource();
        if (resource == null)
            return;

        resourceService.setFileDeleted(resource);
        scanJobService.afterFileDeleted(scannable);
    }

    @Override
    public File deleteAllFiles(String authorizationCode,
                               BiConsumer<Integer, Integer> updateProgressObserver) throws IllegalArgumentException, IOException {

        checkDeleteAllFilesAuthCode(authorizationCode);

        int nonFreeScannableFileCount = getNonFreeScannableFileCount();

        File log = createFileDeleteLog();

        runDeleteAllFiles(nonFreeScannableFileCount, log, updateProgressObserver);

        try {
            solrService.fullImport(SolrService.Core.ScanJob);
        } catch (SolrServerException e) {
            LOG.error("could not run full import on scan job core", e);
        }

        return log;
    }

    private void runDeleteAllFiles(int nonFreeScannableFileCount, File log, BiConsumer<Integer, Integer> updateProgressObserver) {

        ObjectContext context = BaseContext.getThreadObjectContext();

        int i = 0;
        int skipped = 0;
        int objectCount = 0;

        while (i < nonFreeScannableFileCount) {

            NamedQuery objectQuery = new NamedQuery((ReserveCollectionsDatamap.SELECT_NON_FREE_SCANNABLE_RESOURCES_QUERYNAME),
                    Collections.singletonMap("offset", Integer.toString(skipped)));
            List<Resource> resources = (List<Resource>) context.performQuery(objectQuery);

            LOG.debug("objects: " + resources.size());
            LOG.debug("skipped: " + skipped);

            for (Resource resource : resources) {

                try {
                    ResourceDAO.FileDeleteStatus deleteStatus = deleteFile(resource, log);
                    objectCount += deleteStatus.equals(ResourceDAO.FileDeleteStatus.Deleted)
                                   ? 1
                                   : 0;
                    skipped += deleteStatus.equals(ResourceDAO.FileDeleteStatus.NotDeleted)
                               ? 1
                               : 0;
                    setScannableComment(resource, messages.get("reference"));
                } catch (CommitException e) {
                    LOG.error("could not update resource " + resource.getId(), e);
                    log("could not update resource " + resource.getId() + "; cause: " + e.getMessage(), Level.ERROR, log);
                }
            }

            updateProgressObserver.accept(objectCount, nonFreeScannableFileCount);
            i += BaseDAO.MAX_RESULTS;
        }
    }

    private void setScannableComment(Resource resource, String comment) throws CommitException {

        ResourceContainer resourceContainer = resource.getResourceContainer();
        if (resourceContainer instanceof Scannable) {
            Scannable scannable = (Scannable) resourceContainer;
            scannable.setComment(comment);
            scannable.setModified(new Date());
            scanJobDAO.update(scannable);
        }
    }

    private ResourceDAO.FileDeleteStatus deleteFile(Resource resource, File log) throws CommitException {

        String filePath = resource.getFilePath();
        ResourceDAO.FileDeleteStatus fileDeleteStatus = resourceService.deleteFile(resource);
        switch (fileDeleteStatus) {
            case Deleted:
                log("deleted file: " + filePath, Level.INFO, log);
                break;
            case NoFile:
                log("file " + filePath + " does not exist in resource " + resource.getId(), Level.WARN, log);
                break;
            case NotDeleted:
                log("could not delete file " + filePath + " of resource " + resource.getId(), Level.ERROR, log);
                break;
        }
        return fileDeleteStatus;
    }

    private int getNonFreeScannableFileCount() {

        ObjectContext context = BaseContext.getThreadObjectContext();
        NamedQuery countQuery = new NamedQuery(ReserveCollectionsDatamap.COUNT_NON_FREE_SCANNABLE_FILES_QUERYNAME);
        List records = context.performQuery(countQuery);
        DataRow dr = (DataRow) records.get(0);
        return Integer.valueOf(dr.get("count").toString());
    }

    private void checkDeleteAllFilesAuthCode(String authorizationCode) throws IOException {

        List<String> passwdLines = FileUtils.readLines(new File(config.getString("scannable.file.delete.passwd")));
        String passwd = passwdLines.size() > 0
                        ? passwdLines.get(0)
                        : null;
        authorizationCode = StringUtils.defaultIfBlank(authorizationCode, "");
        if (!StringUtils.equals(passwd, authorizationCode))
            throw new IllegalArgumentException("auth code to delete all scan files is invalid");
    }

    private void log(String msg, Level logLevel, File log) {
        try {
            String data = String.format("%8s - %s%s", logLevel.toString(), msg, LINE_SEPARATOR);
            FileUtils.writeStringToFile(log, data, true);
        } catch (IOException e) {
            LOG.error("could not write string to file " + log, e);
        }
    }

    private File createFileDeleteLog() throws IOException {
        String logName = config.getString("scannable.file.delete.log");
        File file = new File(logName);
        if (file.exists()) {
            int suffix = 1;
            while ((file = new File(String.format("%s.%d", logName, suffix))).exists()) {
                suffix++;
            }
        }
        FileUtils.touch(file);
        return file;
    }
}
