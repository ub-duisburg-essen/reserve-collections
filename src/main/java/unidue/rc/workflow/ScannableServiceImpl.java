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


import org.apache.cayenne.Persistent;
import org.apache.cayenne.di.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.DeleteException;
import unidue.rc.dao.EntryDAO;
import unidue.rc.dao.ScanJobDAO;
import unidue.rc.model.Entry;
import unidue.rc.model.ReserveCollection;
import unidue.rc.model.Resource;
import unidue.rc.model.Scannable;
import unidue.rc.search.SolrService;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author Nils Verheyen
 * @since 05.12.13 09:25
 */
public class ScannableServiceImpl implements ScannableService {

    private static final Logger LOG = LoggerFactory.getLogger(ScannableServiceImpl.class);

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
            update(scannable);
        }

        resource.setFullTextURL(fullTextURL);
        resourceService.update(resource);
        scanJobService.onScannableUpdated(scannable);
        return resource;
    }

    @Override
    public Resource update(Scannable scannable, String filename, InputStream input) throws CommitException, IOException {
        Resource resource = scannable.getResource();
        if (resource == null)
            resourceService.create(filename, input, scannable);
        else
            resourceService.update(resource, filename, input);

        scanJobService.onScannableUpdated(scannable);
        return resource;
    }

    @Override
    public void update(Scannable scannable) throws CommitException {
        entryDAO.update(scannable);
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
    public void deleteFile(Scannable scannable) throws CommitException {
        Resource resource = scannable.getResource();
        if (resource == null)
            return;

        resourceService.deleteFile(resource);
        scanJobService.afterFileDeleted(scannable);
    }
}
