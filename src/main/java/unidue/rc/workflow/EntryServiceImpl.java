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
import unidue.rc.dao.*;
import unidue.rc.model.*;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author Nils Verheyen
 * @since 12.01.15 14:47
 */
public class EntryServiceImpl implements EntryService {

    @Inject
    private BookDAO bookDAO;

    @Inject
    private HeadlineDAO headlineDAO;

    @Inject
    private EntryDAO entryDAO;

    @Inject
    private BookService bookService;

    @Inject
    private ResourceService resourceService;

    @Inject
    private ScannableService scannableService;

    @Override
    public void delete(Entry entry) throws CommitException {

        entry.setDeleted(new Date());
        update(entry);
    }

    @Override
    public void update(Entry entry) throws CommitException {
        entry.setModified(new Date());
        entryDAO.update(entry);

        scannableService.afterEntryUpdate(entry);
        bookService.afterEntryUpdate(entry);
    }

    @Override
    public void afterCollectionUpdate(ReserveCollection collection) throws CommitException {

        scannableService.afterCollectionUpdate(collection);
        resourceService.afterCollectionUpdate(collection);
        bookService.afterCollectionUpdate(collection);
    }

    @Override
    public void beforeCollectionDelete(ReserveCollection collection) throws DeleteException {

        List<Entry> entries = collection.getEntries();
        for (Entry e : entries) {
            resourceService.beforeEntryDelete(e);
            scannableService.beforeEntryDelete(e);
            bookService.beforeEntryDelete(e);

            entryDAO.delete(e);
        }
    }

    @Override
    public void afterCollectionDelete(ReserveCollection collection) {

        List<Entry> entries = collection.getEntries();
        for (Entry e : entries) {
            resourceService.afterEntryDelete(e);
            scannableService.afterEntryDelete(e);
            bookService.afterEntryDelete(e);
        }
    }
}
