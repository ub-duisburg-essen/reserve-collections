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
import org.apache.commons.lang3.tuple.Pair;
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
import java.util.*;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static unidue.rc.workflow.BookJobServiceImpl.BookJobAction.Create;
import static unidue.rc.workflow.BookJobServiceImpl.BookJobAction.Update;

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

    enum BookJobAction {
        Create,
        Update,
        Delete,
        Nothing
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
        if (isBookJobNeeded(book)) {

            createBookJob(book);
            sendNotificationMail(book, MailSubjectCause.created);
        }
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
                sendNotificationMail(book, MailSubjectCause.deleted);
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
        List<Book> books = collection.getEntries()
                .stream()
                .filter(entry -> entry.getValue() instanceof Book)
                .map(entry -> entry.getBook())
                .collect(Collectors.toList());
        checkBookJobs(books);
    }

    @Override
    public void afterEntryUpdate(Entry entry) {
        if (entry.getValue() instanceof Book) {
            Book book = (Book) entry.getValue();
            BookJob bookJob = book.getBookJob();
            if (entry.isDeleted() && bookJob != null) {

                try {
                    deleteBookJob(bookJob);
                    sendNotificationMail(book, MailSubjectCause.deleted);
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

        BookJob bookJob = book.getBookJob();
        BookJobAction action = detectBookJobAction(book);
        switch (action) {
            case Create:
                try {
                    createBookJob(book);
                    sendNotificationMail(book, MailSubjectCause.created);
                } catch (CommitException e) {
                    LOG.error("could not create book job for book " + book);
                }
                break;
            case Update:
                updateBookJobView(bookJob);
                break;
            case Delete:
                try {
                    deleteBookJob(bookJob);
                    sendNotificationMail(book, MailSubjectCause.deleted);
                } catch (DeleteException e) {
                    LOG.error("could not delete book job " + bookJob.getId());
                }
                break;
            case Nothing:
            default:
                break;
        }
    }

    private void checkBookJobs(List<Book> books) {
        Map<BookJobAction, List<Pair<BookJobAction, Book>>> actions = books.stream()
                .map(b -> Pair.of(detectBookJobAction(b), b))
                .collect(Collectors.groupingBy(Pair::getLeft));

        actions.forEach((action, pairs) -> {
            List<Book> booksByAction = pairs.stream().map(p -> p.getRight()).collect(Collectors.toList());
            switch (action) {
                case Create:
                    booksByAction.forEach(book -> {
                        try {
                            createBookJob(book);
                        } catch (CommitException e) {
                            LOG.error("could not create book job for book " + book);
                        }
                    });
                    sendNotificationMail(books, MailSubjectCause.created);
                    break;
                case Update:
                    booksByAction.stream()
                            .map(Book::getBookJob)
                            .forEach(this::updateBookJobView);
                    break;
                case Delete:
                    booksByAction.forEach(book -> {
                        try {
                            deleteBookJob(book.getBookJob());
                        } catch (DeleteException e) {
                            LOG.error("could not delete book job " + book.getBookJob().getId());
                        }
                    });
                    sendNotificationMail(books, MailSubjectCause.deleted);
                    break;
                case Nothing:
                default:
                    break;
            }
        });
    }

    private BookJobAction detectBookJobAction(Book book) {
        boolean isBookJobNeeded = isBookJobNeeded(book);
        BookJob bookJob = book.getBookJob();
        BookJobAction check = BookJobAction.Nothing;
        if (bookJob == null && isBookJobNeeded) {
            // no bookjob given but needed -> create one
            check = Create;
        } else if (bookJob != null && isBookJobNeeded) {
            // book job given and needed -> update
            check = Update;
        } else if (bookJob != null && !isBookJobNeeded) {
            // book job given but not needed -> delete
            check = BookJobAction.Delete;
        } else if (bookJob == null && !isBookJobNeeded) {
            // no book job given and not needed -> nothing to do
            check = BookJobAction.Nothing;
        }
        return check;
    }

    private void createBookJob(Book book) throws CommitException {
        BookJob job = new BookJob();
        job.setStatus(BookJobStatus.NEW);
        job.setModified(new Date());
        job.setBook(book);
        bookJobDAO.create(job);

        commitBookJobToSOLR(job);
    }

    private void deleteBookJob(BookJob bookJob) throws DeleteException {

        try {
            bookJobDAO.delete(bookJob);
            deleteBookJobFromSOLR(bookJob);
        } catch (DeleteException e) {
            LOG.error("could not update book job " + bookJob, e);
            throw e;
        }

    }

    private void sendNotificationMail(List<Book> books, MailSubjectCause cause) {
        String templateFile = "/vt/mail.books.vm";
        List<ReserveCollection> collections = books.stream()
                .map(Book::getReserveCollection)
                .distinct()
                .collect(Collectors.toList());

        // collect all recipients
        List<BookMailGroup> mailGroups = new ArrayList<>();
        collections.stream()
                .map(c -> mailRecipientDAO.getRecipients(c.getLibraryLocation(), Book.class))
                .flatMap(Collection::stream)
                .distinct()
                .forEach(recipient -> mailGroups.add(new BookMailGroup(recipient)));

        // group books by recipient, collections, books
        mailGroups.stream()
                .forEach(g -> {
                    // all collections that the group with given recipient is responsible for
                    Set<ReserveCollection> filteredCollections = collections.stream()
                            .filter(c -> mailRecipientDAO.getRecipients(c.getLibraryLocation(), Book.class).contains(g.recipient))
                            .collect(Collectors.toSet());
                    g.collections.addAll(filteredCollections);

                    // all books of all filtered collections
                    Set<Book> filteredBooks = books.stream()
                            .filter(b -> filteredCollections.contains(b.getReserveCollection()))
                            .collect(Collectors.toSet());
                    g.books.addAll(filteredBooks);
                });

        // build subject
        String subjectCause = cause.equals(MailSubjectCause.created)
                              ? messages.get("new.books")
                              : cause.equals(MailSubjectCause.deleted)
                                ? messages.get("deleted.books")
                                : StringUtils.EMPTY;
        String subject = String.format(messages.get("updated.books"), subjectCause);

        // send mail
        mailGroups.stream().forEach(g -> {
            // collect links to collections that will be used inside mail.books.vm and collection.vm
            Map<ReserveCollection, String> collectionLinks = g.collections
                    .stream()
                    .collect(Collectors.toMap(c -> c, c -> mailService.createCollectionLink(c)));

            VelocityContext context = new VelocityContext();
            context.put("collections", g.collections);
            context.put("collectionLinks", collectionLinks);
            context.put("books", g.books);
            sendNotificationMail(context, templateFile, subject, g.recipient);
        });
    }

    private void sendNotificationMail(Book book, MailSubjectCause cause) {
        VelocityContext context = new VelocityContext();
        String templateFile = "/vt/mail.book.vm";
        ReserveCollection collection = book.getReserveCollection();

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
        sendNotificationMail(context, templateFile, subject.toString(), recipients.toArray(new OrderMailRecipient[recipients.size()]));
    }

    private void sendNotificationMail(VelocityContext context, String templateFile, String subject, OrderMailRecipient... recipients) {

        String from = config.getString("mail.from");
        User currentUser = securityService.getCurrentUser();
        try {
            MailServiceImpl.MailBuilder mailBuilder = mailService.builder(templateFile)
                    .from(from)
                    .subject(subject.toString())
                    .context(context)
                    .addRecipients(Stream.of(recipients).map(OrderMailRecipient::getMail).toArray(String[]::new));
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

    private static class BookMailGroup {

        OrderMailRecipient recipient;
        Collection<ReserveCollection> collections;
        Collection<Book> books;

        private BookMailGroup(OrderMailRecipient recipient) {
            this.recipient = recipient;
            this.collections = new HashSet<>();
            this.books = new HashSet<>();
        }
    }
}
