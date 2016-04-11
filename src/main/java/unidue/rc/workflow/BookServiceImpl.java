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


import org.apache.cayenne.di.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unidue.rc.dao.BookDAO;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.DeleteException;
import unidue.rc.model.*;

/**
 * @author Nils Verheyen
 * @since 05.12.13 09:27
 */
public class BookServiceImpl implements BookService {

    private static final Logger LOG = LoggerFactory.getLogger(BookServiceImpl.class);

    @Inject
    private BookDAO bookDAO;

    @Inject
    private BookJobService bookJobService;

    @Inject
    private ResourceService resourceService;

    @Override
    public void create(Book book, ReserveCollection collection) throws CommitException {
        bookDAO.createEntry(book, collection);

        if (bookJobService.isBookJobNeeded(book)) {
            book.setBookingStatus(BookingStatus.AWAITS_BOOKING);
            bookDAO.update(book);
        }

        bookJobService.onBookCreated(book);
    }

    @Override
    public void delete(Book book) throws DeleteException {
        bookJobService.beforeBookDelete(book);
        bookDAO.delete(book);
        bookJobService.afterBookDelete(book);
    }

    @Override
    public void update(Book book, String fullTextURL) throws CommitException {

        Resource resource = book.getResource();

        if (resource == null) {

            resource = resourceService.create(fullTextURL, book);
            book.setResource(resource);
        }
        bookDAO.update(book);

        resource.setFullTextURL(fullTextURL);
        resourceService.update(resource);

        bookJobService.onBookUpdated(book);
    }

    @Override
    public void afterCollectionUpdate(ReserveCollection collection) {
        bookJobService.afterCollectionUpdate(collection);
    }

    @Override
    public void beforeEntryDelete(Entry entry) {

        Book book = entry.getBook();
        try {
            if (book != null) {
                bookJobService.beforeBookDelete(book);
                delete(book);
                bookJobService.afterBookDelete(book);
            }
        } catch (DeleteException e) {
            LOG.error("could not delete book " + book, e);
        }
    }

    @Override
    public void afterEntryDelete(Entry entry) {
    }

    @Override
    public void afterEntryUpdate(Entry entry) throws CommitException {
        bookJobService.afterEntryUpdate(entry);
    }

}
