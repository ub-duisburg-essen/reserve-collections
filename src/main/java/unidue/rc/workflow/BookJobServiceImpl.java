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
import org.apache.cayenne.di.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.EmailException;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unidue.rc.dao.BookJobDAO;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.DeleteException;
import unidue.rc.dao.OrderMailRecipientDAO;
import unidue.rc.model.*;
import unidue.rc.model.solr.SolrBookJobView;
import unidue.rc.search.SolrService;
import unidue.rc.security.CollectionSecurityService;
import unidue.rc.system.MailService;
import unidue.rc.system.MailServiceImpl;
import unidue.rc.system.SystemConfigurationService;
import unidue.rc.system.SystemMessageService;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * @author Nils Verheyen
 * @since 04.12.14 11:00
 */
public class BookJobServiceImpl implements BookJobService {

    private static final Logger LOG = LoggerFactory.getLogger(BookJobServiceImpl.class);

    enum MailSubjectCause {
        deleted,
        created
    }

    @Inject
    private BookJobDAO bookJobDAO;

    @Inject
    private OrderMailRecipientDAO mailRecipientDAO;

    @Inject
    private SolrService solrService;

    @Inject
    private CollectionSecurityService securityService;

    @Inject
    private MailService mailService;

    @Inject
    private SystemConfigurationService config;

    @Inject
    private SystemMessageService messages;

    @Override
    public void onBookCreated(Book book) throws CommitException {

        // create new book job if location of collection is a physical location
        if (isBookJobNeeded(book))
            createBookJob(book);
    }

    @Override
    public void onBookUpdated(Book book) {
        checkBookJob(book);
    }

    @Override
    public void beforeBookDelete(Book book) {

        BookJob bookJob = book.getBookJob();
        if (bookJob != null) {
            try {
                deleteBookJob(bookJob);
            } catch (DeleteException e) {
                LOG.error("could not delete book job of book " + book, e);
            }
        }
    }

    @Override
    public void afterBookDelete(Book book) {
    }

    @Override
    public void afterCollectionUpdate(ReserveCollection collection) {
        collection.getEntries()
                .stream()
                .filter(entry -> entry.getValue() instanceof Book)
                .map(entry -> entry.getBook())
                .forEach(book -> checkBookJob(book));
    }

    @Override
    public void afterEntryUpdate(Entry entry) {
        if (entry.getValue() instanceof Book) {
            Book book = (Book) entry.getValue();
            BookJob bookJob = book.getBookJob();
            if (entry.isDeleted() && bookJob != null) {

                try {
                    deleteBookJob(bookJob);
                } catch (DeleteException e) {
                    LOG.debug("could not delete book job", e);
                }
            } else {
                checkBookJob(book);
            }
        }
    }

    @Override
    public boolean isBookJobNeeded(Book book) {
        ReserveCollection collection = book.getReserveCollection();
        return collection.isActive()
                && collection.getLibraryLocation().isPhysical()
                && !book.getEntry().isDeleted();
    }

    @Override
    public void checkBookJob(Book book) {

        boolean isBookJobNeeded = isBookJobNeeded(book);
        BookJob bookJob = book.getBookJob();
        if (bookJob == null && isBookJobNeeded) {
            // no bookjob given but needed -> create one
            try {
                createBookJob(book);
            } catch (CommitException e) {
                LOG.error("could not create book job for book " + book);
            }
        } else if (bookJob != null && isBookJobNeeded) {
            // book job given and needed -> update
            updateBookJobView(bookJob);
        } else if (bookJob != null && !isBookJobNeeded) {
            // book job given but not needed -> delete
            try {
                deleteBookJob(bookJob);
            } catch (DeleteException e) {
                LOG.error("could not delete book job " + bookJob.getId());
            }
        } else if (bookJob == null && !isBookJobNeeded) {
            // no book job given and not needed -> nothing to do
        }
    }

    private void createBookJob(Book book) throws CommitException {
        BookJob job = new BookJob();
        job.setStatus(BookJobStatus.NEW);
        job.setModified(new Date());
        job.setBook(book);
        bookJobDAO.create(job);

        commitBookJobToSOLR(job);

        sendNotificationMail(book, MailSubjectCause.created);
    }

    private void deleteBookJob(BookJob bookJob) throws DeleteException {

        try {
            Book book = bookJob.getBook();
            bookJobDAO.delete(bookJob);
            deleteBookJobFromSOLR(bookJob);
            sendNotificationMail(book, MailSubjectCause.deleted);
        } catch (DeleteException e) {
            LOG.error("could not update book job " + bookJob, e);
            throw e;
        }

    }

    private void sendNotificationMail(Book book, MailSubjectCause cause) {
        VelocityContext context = new VelocityContext();
        String templateFile = "/vt/mail.book.vm";
        ReserveCollection collection = book.getReserveCollection();
        User currentUser = securityService.getCurrentUser();

        // collection link
        String collectionLink = mailService.createCollectionLink(collection);

        // authors
        String authors = mailService.buildAuthors(collection);

        // origin
        String origin = mailService.buildOrigin(collection);

        // entry link
        String entryLink = mailService.createEntryLink(book.getEntry());

        StringBuilder subjectContext = new StringBuilder();
        switch (cause) {
            case created:
                subjectContext.append(messages.get("new.book"));
                break;
            case deleted:
                subjectContext.append(messages.get("deleted.book"));
                break;
        }
        if (book.getSignature() != null) {
            subjectContext.append(' ');
            subjectContext.append(book.getSignature());
        }

        context.put("book", book);
        String subject = mailService.buildSubject(book.getEntry(),
                subjectContext.toString(), authors);
        context.put("collectionLink", collectionLink);
        context.put("authors", authors);
        context.put("origin", origin);
        context.put("entryLink", entryLink);

        // send mail

        List<OrderMailRecipient> recipients = mailRecipientDAO.getRecipients(collection.getLibraryLocation(), Book.class);
        String from = config.getString("mail.from");
        try {
            MailServiceImpl.MailBuilder mailBuilder = mailService.builder(templateFile)
                    .from(from)
                    .subject(subject.toString())
                    .context(context)
                    .addRecipients(recipients.stream().map(OrderMailRecipient::getMail).toArray(String[]::new));
            if (currentUser != null && !StringUtils.isEmpty(currentUser.getEmail()))
                mailBuilder.addReplyTo(currentUser.getEmail());

            Mail mail = mailBuilder.create();
            mailService.sendMail(mail);
        } catch (CommitException e) {
            LOG.error("could not save mail", e);
        } catch (EmailException e) {
            LOG.error("could not send mail", e);
        } catch (IOException e) {
            LOG.error("could not create mail", e);
        }
    }

    private void updateBookJobView(BookJob job) {
        deleteBookJobFromSOLR(job);
        commitBookJobToSOLR(job);
    }

    private void commitBookJobToSOLR(BookJob job) {
        LOG.info("adding BookJob to solr: " + job);

        ReserveCollection collection = job.getBook().getReserveCollection();

        SolrBookJobView jobView = new SolrBookJobView();
        jobView.setCollectionID(collection.getId());
        jobView.setJobID(job.getId().toString());
        jobView.setJobIDNumeric(job.getId());
        jobView.setCollectionNumber(collection.getNumber().getNumber().toString());
        jobView.setCollectionNumberNumeric(collection.getNumber().getNumber());
        jobView.setModified(job.getBook().getModified());
        jobView.setSignature(job.getBook().getSignature());
        jobView.setTitle(job.getBook().getTitle());
        jobView.setStatus(job.getStatus().getValue());
        jobView.setLocation(collection.getLibraryLocation().getName());
        jobView.setLocationID(collection.getLibraryLocation().getId());

        solrService.addBean(jobView, SolrService.Core.BookJob);
    }

    private void deleteBookJobFromSOLR(BookJob job) {

        solrService.deleteByID(job, SolrService.Core.BookJob);
    }

}
