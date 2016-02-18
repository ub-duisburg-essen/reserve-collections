package unidue.rc.plugins.alephsync;

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

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.di.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.mail.EmailException;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.velocity.VelocityContext;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import unidue.rc.dao.*;
import unidue.rc.model.*;
import unidue.rc.model.solr.SolrCollectionView;
import unidue.rc.search.SolrQueryBuilder;
import unidue.rc.search.SolrResponse;
import unidue.rc.search.SolrService;
import unidue.rc.system.*;
import unidue.rc.workflow.EntryService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by nils on 29.06.15.
 */
public class AlephSynchronizerImpl extends BaseCronJob implements AlephSynchronizer {
    private static final Logger LOG = Logger.getLogger(AlephSynchronizerImpl.class);

    private static final int MAX_SEARCH_RESULTS = 100;

    @Inject
    private AlephDAOImpl client;

    @Inject
    private SystemConfigurationService config;

    @Inject
    private ReserveCollectionDAO collectionDAO;

    @Inject
    private SolrService solrService;

    @Inject
    private EntryService entryService;

    @Inject
    private MailService mailService;

    @Inject
    private EntryDAO entryDAO;

    @Inject
    private BookDAO bookDAO;

    @Inject
    private SystemMessageService messages;

    private ObjectContext context;

    private VelocityContext velocityContext;

    private List<AlephSyncData> syncData;

    @Override
    protected void run(JobExecutionContext context) throws JobExecutionException {
        LOG.info("running aleph synchronization");

        syncAleph();
    }

    /**
     * Synchronizes all slots, therefore all reserve collections, with aleph.
     */
    public void syncAleph() throws JobExecutionException {

        try {

            setupSyncData();
            sync();
        } catch (JobExecutionException e) {
            velocityContext.put("errorMessage", e.getMessage());
            velocityContext.put("errorStacktrace", ExceptionUtils.getStackTrace(e));
            throw e;
        } finally {
            sendSyncMail();
        }
    }

    private void setupSyncData() throws JobExecutionException {

        LOG.info("\nSynchronizing reserve collections with Aleph accounts...\n");
        velocityContext = new VelocityContext();
        syncData = new ArrayList<>();
    }

    private void sendSyncMail() {
        String templateFile = "/vt/mail.aleph.sync.vm";

        String subject = messages.get("aleph.sync.mail.subject");
        velocityContext.put("allSyncData", syncData);

        String recipient = config.getString("system.mail");
        String from = config.getString("mail.from");
        try {
            Mail mail = mailService.builder(templateFile)
                    .from(from)
                    .subject(subject.toString())
                    .context(velocityContext)
                    .addRecipient(recipient)
                    .create();
            mailService.sendMail(mail);
        } catch (EmailException e) {
            LOG.error("could not send mail", e);
        } catch (IOException e) {
            LOG.error("could not create mail", e);
        }
    }

    private void sync() throws JobExecutionException {

        SolrResponse<SolrCollectionView> views = getCollectionViews(0);
        long count = views.getCount();
        int offset = 0;
        while (offset < count) {

            views.getItems().stream()
                    .map(view -> collectionDAO.get(ReserveCollection.class, Integer.valueOf(view.getCollectionID())))
                    .filter(collection -> collection.getLibraryLocation().isPhysical())
                    .filter(collection -> !StringUtils.isEmpty(collection.getAlephSystemId()))
                    .forEach(collection -> {

                        try {
                            syncCollection(collection);
                        } catch (SQLException | CommitException | DeleteException e) {
                            LOG.error("could not sync collection " + collection.getId(), e);
                        }
                    });

            views = getCollectionViews(offset);
            offset += MAX_SEARCH_RESULTS;
        }
        LOG.info("All documents synchronized with aleph entries");
    }

    public void syncCollection(ReserveCollection collection) throws SQLException, CommitException, DeleteException {
        List<Book> presentBooks = collection.getBooksWithSignature();

        // aleph signatures contains all loaned presentBooks of user with aleph system id kept inside target collection
        List<AlephBook> alephBooks = client.listSignatures(collection.getAlephSystemId());

        // this has to be done in order!
        int deleted = removeDeletedBooks(presentBooks, alephBooks);
        int updated = updateBooks(presentBooks, alephBooks);
        int added = addNewBooks(collection, alephBooks);

        syncData.add(new AlephSyncData(collection.getId(), deleted, updated, added));

        LOG.info(String.format("collection: %5d / deleted %3d / updated %3d / added %3d", collection.getId(), deleted, updated, added));
    }

    int removeDeletedBooks(List<Book> presentBooks, List<AlephBook> alephBooks) throws DeleteException, CommitException {
        LOG.debug("-------- removing deleted books --------");

        List<Book> booksToRemove = new ArrayList<>();

        for (Book book : presentBooks) {

            String signature = BookUtils.getNormalized(book.getSignature());
            Optional<AlephBook> alephBook = alephBooks.stream()
                    .filter(b -> signature.equals(b.getSignature()))
                    .findAny();

            // Remove entry if target book is not loaned and has no reference to other collection
            if (!alephBook.isPresent()
                    && StringUtils.isEmpty(book.getCollectionNumber())
                    && BookingStatus.IS_BOOKED.equals(book.getBookingStatus())) {

                // mark book as not needed for the remaining of this workflow
                booksToRemove.add(book);

                LOG.debug("removed book " + signature);
            } else if (alephBook.isPresent()) {
                // remove signature inside retrieved aleph properties
                alephBooks.remove(alephBook.get());
            }
        }
        int result = booksToRemove.size();
        booksToRemove.forEach(book -> {
            Entry entry = book.getEntry();
            try {
                bookDAO.delete(book);
                entryDAO.delete(entry);
            } catch (DeleteException e) {
                LOG.error("could not delete book " + book.getId(), e);
            }
        });

        return result;
    }

    int updateBooks(List<Book> presentBooks, List<AlephBook> alephBooks) throws SQLException, CommitException {
        LOG.debug("-------- adding new presentBooks --------");

        int num = 0;
        // update any present book data that is already inside alephs loaned books
        for (Book presentBook : presentBooks) {

            String signature = BookUtils.getNormalized(presentBook.getSignature());
            Optional<AlephBook> optionalBook = alephBooks.stream()
                    .filter(b -> signature.equals(b.getSignature()))
                    .findAny();
            if (optionalBook.isPresent()) {

                AlephBook alephBook = optionalBook.get();

                alephBooks.remove(alephBook);

                presentBook.setBookingStatus(BookingStatus.IS_BOOKED);
                client.setBookData(presentBook, alephBook.getSignature());
                entryService.update(presentBook.getEntry());

                BookJob bookJob = presentBook.getBookJob();
                if (bookJob != null && bookJob.getStatus().equals(BookJobStatus.NEW)) {
                    bookJob.setStatus(BookJobStatus.SYSTEM_DONE);
                    bookDAO.update(bookJob);
                }

                num++;
            }
        }

        return num;
    }

    int addNewBooks(ReserveCollection collection, List<AlephBook> alephBooks) throws SQLException, CommitException {
        LOG.debug("-------- adding new presentBooks --------");

        int num = 0;

        // create new entries for all left signatures inside alephs records
        for (AlephBook alephBook : alephBooks) {

            // create new "book"
            Book newBook = new Book();
            newBook.setBookingStatus(BookingStatus.IS_BOOKED);
            client.setBookData(newBook, alephBook.getSignature());
            bookDAO.createEntry(newBook, collection);

            num++;
        }

        return num;
    }

    private SolrResponse<SolrCollectionView> getCollectionViews(int offset) throws JobExecutionException {
        SolrQuery query = new SolrQueryBuilder()
                .singleCondition(SolrCollectionView.STATUS_PROPERTY, ReserveCollectionStatus.ACTIVE.getValue().toString())
                .setOffset(offset)
                .setCount(MAX_SEARCH_RESULTS)
                .build();
        SolrResponse<SolrCollectionView> response;
        try {
            response = solrService.query(SolrCollectionView.class, query);
        } catch (SolrServerException e) {
            throw new JobExecutionException("error occured during querying solr", e);
        }

        return response;
    }

}
