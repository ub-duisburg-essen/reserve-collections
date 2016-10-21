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
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unidue.rc.dao.*;
import unidue.rc.model.*;
import unidue.rc.model.solr.SolrScanJobView;
import unidue.rc.search.SolrService;
import unidue.rc.security.CollectionSecurityService;
import unidue.rc.system.MailService;
import unidue.rc.system.MailServiceImpl;
import unidue.rc.system.SystemConfigurationService;
import unidue.rc.system.SystemMessageService;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Nils Verheyen
 * @since 04.12.14 10:45
 */
public class ScanJobServiceImpl implements ScanJobService {

    private static final Logger LOG = LoggerFactory.getLogger(ScanJobServiceImpl.class);

    enum MailSubjectCause {
        deleted,
        created,
        updated
    }

    @Inject
    private SolrService solrService;

    @Inject
    private MailService mailService;

    @Inject
    private CollectionSecurityService securityService;

    @Inject
    private SystemConfigurationService config;

    @Inject
    private ScanJobDAO scanJobDAO;

    @Inject
    private RoleDAO roleDAO;

    @Inject
    private UserDAO userDAO;

    @Inject
    private OrderMailRecipientDAO mailRecipientDAO;

    @Inject
    private SystemMessageService messages;

    @Override
    public void update(ScanJob scanJob) throws CommitException {
        scanJobDAO.update(scanJob);
        updateScanJobView(scanJob);
    }

    @Override
    public void onScannableCreated(Scannable scannable) throws CommitException {
        if (isScanJobNeeded(scannable))
            createScanJob(scannable);
    }

    @Override
    public void onScannableUpdated(Scannable scannable) throws CommitException {
        ScanJob scanJob = scannable.getScanJob();

        // check ist scan job is already present
        if (scanJob != null) {

            // if scan job is present and content is available set status done and delete view
            if (scannable.isContentAvailable()) {

                scanJob.setStatus(ScanJobStatus.DONE);
                scanJobDAO.update(scanJob);
            }
            updateScanJobView(scanJob);
        } else if (isScanJobNeeded(scannable)) {
            // if no scan job is present but needed create one
            createScanJob(scannable);
        }
    }

    @Override
    public void beforeScannableDelete(Scannable scannable) throws DeleteException {
        ScanJob scanJob = scannable.getScanJob();
        if (scanJob != null) {
            deleteScanJob(scanJob, scannable);
        }
    }

    @Override
    public void afterScannableDelete(Scannable scannable) {
    }

    @Override
    public void afterCollectionUpdate(ReserveCollection collection) {

        collection.getEntries()
                .stream()
                .filter(entry -> entry.getValue() instanceof Scannable)
                .map(entry -> (Scannable) entry.getValue())
                .forEach(scannable -> checkScanJob(scannable));
    }

    @Override
    public void afterFileDeleted(Scannable scannable) {
        checkScanJob(scannable);
    }

    @Override
    public void onEntryUpdated(Entry entry) {
        if (entry.getValue() instanceof Scannable) {
            Scannable scannable = (Scannable) entry.getValue();
            checkScanJob(scannable);
            if (entry.isDeleted() && !scannable.isContentAvailable())
                sendNotificationMail(scannable, buildDefaultMailContext(scannable), MailSubjectCause.deleted);
        }
    }

    @Override
    public boolean isScanJobNeeded(Scannable scannable) {
        ReserveCollection collection = scannable.getReserveCollection();
        return !scannable.isContentAvailable()
                && !scannable.getEntry().isDeleted()
                && (collection.isActive() || collection.isNew());
    }

    @Override
    public void checkScanJob(Scannable scannable) {
        boolean isScanJobNeeded = isScanJobNeeded(scannable);
        ScanJob scanJob = scannable.getScanJob();

        if (scanJob == null && isScanJobNeeded) {
            // no scanjob given but needed -> create one
            try {
                createScanJob(scannable);
            } catch (CommitException e) {
                LOG.error("could not create scan job for scannable " + scannable);
            }
        } else if (scanJob != null && isScanJobNeeded) {
            // scan job given and needed -> update
            updateScanJobView(scanJob);
        } else if (scanJob != null && !isScanJobNeeded) {
            // scan job given but not needed -> delete
            try {
                setStatus(scanJob, ScanJobStatus.DONE);
            } catch (CommitException e) {
                LOG.error("could not finish scan job " + scanJob.getId());
            }
        } else if (scanJob == null && !isScanJobNeeded) {
            // no scan job given and not needed -> nothing to do
        }
    }

    @Override
    public String getUploadBarcodeContent(Scannable scannable) {
        return Integer.toString(scannable.getId());
    }

    private void createScanJob(Scannable scannable) throws CommitException {
        ScanJob job = new ScanJob();
        job.setStatus(ScanJobStatus.NEW);
        job.setScannable(scannable);
        scanJobDAO.create(job);

        commitScanJobToSolr(job);

        sendNotificationMail(scannable, buildDefaultMailContext(scannable), MailSubjectCause.created);
    }

    private void deleteScanJob(ScanJob job, Scannable scannable) throws DeleteException {

        scanJobDAO.delete(job);
        deleteScanJobFromSolr(job);
        sendNotificationMail(scannable, buildDefaultMailContext(scannable), MailSubjectCause.deleted);
    }

    private void setStatus(ScanJob job, ScanJobStatus status) throws CommitException {

        job.setStatus(status);
        scanJobDAO.update(job);
        updateScanJobView(job);
    }

    private void sendNotificationMail(Scannable scannable, MailContextBuilder contextBuilder, MailSubjectCause cause) {

        String templateFile = null;
        ReserveCollection collection = scannable.getReserveCollection();

        String type = "";

        List<OrderMailRecipient> recipients = Collections.EMPTY_LIST;
        if (scannable instanceof BookChapter) {
            templateFile = "/vt/mail.book.chapter.vm";
            type = cause.name() + ".book.chapter";
            recipients = mailRecipientDAO.getRecipients(collection.getLibraryLocation(), BookChapter.class);
        } else if (scannable instanceof JournalArticle) {
            templateFile = "/vt/mail.journal.article.vm";
            type = cause.name() + ".journal.article";
            recipients = mailRecipientDAO.getRecipients(collection.getLibraryLocation(), JournalArticle.class);
        }
        String subject = mailService.buildSubject(scannable.getEntry(), messages.get(type), mailService.buildAuthors(collection));

        // send mail

        User currentUser = securityService.getCurrentUser();
        try {
            MailServiceImpl.MailBuilder mailBuilder = mailService.builder(templateFile)
                    .from(config.getString("mail.from"))
                    .subject(subject.toString())
                    .context(contextBuilder.build())
                    .addRecipients(recipients.stream().map(OrderMailRecipient::getMail).toArray(String[]::new));
            if (currentUser != null && StringUtils.isNotBlank(currentUser.getEmail()))
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

    private MailContextBuilder buildDefaultMailContext(Scannable scannable) {
        MailContextBuilder contextBuilder = MailContextBuilder.create();
        ReserveCollection collection = scannable.getReserveCollection();

        // user
        User currentUser = securityService.getCurrentUser();
        contextBuilder.roles(roleDAO.getRoles(currentUser));

        // collection link
        String collectionLink = mailService.createCollectionLink(collection);

        // authors
        String authors = mailService.buildAuthors(collection);

        // origin
        String origin = mailService.buildOrigin(collection);

        // entry link
        String entryLink = mailService.createEntryLink(scannable.getEntry());

        if (scannable instanceof BookChapter) {
            contextBuilder.chapter((BookChapter) scannable);
        } else if (scannable instanceof JournalArticle) {
            contextBuilder.article((JournalArticle) scannable);
        }
        contextBuilder.collectionLink(collectionLink)
                .authors(authors)
                .origin(origin)
                .entryLink(entryLink);

        return contextBuilder;
    }

    private void updateScanJobView(ScanJob job) {

        // extract old and new values
        String newPageStart = job.getScannable().getPageStart();
        String newPageEnd = job.getScannable().getPageEnd();

        String oldPageStart = null;
        String oldPageEnd = null;
        try {
            SolrScanJobView view = solrService.getById(SolrScanJobView.class, job.getId().toString());

            oldPageStart = view.getPageStart();
            oldPageEnd = view.getPageEnd();

        } catch (SolrServerException e) {
            LOG.warn("could not find scan job view with id " + job.getId());
        }

        // update view in solr
        deleteScanJobFromSolr(job);
        commitScanJobToSolr(job);

        // send notification mail if needed
        if ((newPageStart != null && oldPageStart != null && !newPageStart.equals(oldPageStart))
                || (newPageEnd != null && oldPageEnd != null && !newPageEnd.equals(oldPageEnd))) {

            MailContextBuilder contextBuilder = buildDefaultMailContext(job.getScannable());
            contextBuilder.oldPageStart(oldPageStart)
                    .oldPageEnd(oldPageEnd);
            sendNotificationMail(job.getScannable(), contextBuilder, MailSubjectCause.updated);
        }
    }

    private void commitScanJobToSolr(ScanJob job) {


        SolrScanJobView view = new SolrScanJobView();

        Scannable scannable = job.getScannable();
        if (scannable != null) {

            ReserveCollection rc = scannable.getReserveCollection();
            Entry entry = scannable.getEntry();

            view.setWorkTitle(scannable.getWorkTitle());
            view.setWorkAuthors(scannable.getWorkAuthors());
            view.setPartTitle(scannable.getPartTitle());
            view.setPartAuthors(scannable.getPartAuthors());
            view.setSignature(scannable.getSignature());
            view.setPageStart(scannable.getPageStart());
            view.setPageEnd(scannable.getPageEnd());
            view.setScannableType(scannable.getClass().getSimpleName());
            view.setJobID(job.getId());
            view.setReserveCollectionID(rc.getId());
            view.setCollectionNumber(rc.getNumber().getNumber());
            view.setEntryID(entry.getId());
            view.setJobModified(job.getModified());
            view.setScannableCreated(entry.getCreated());
            view.setScannableModified(scannable.getModified());
            view.setStatus(job.getStatus().getValue());
            view.setLocation(rc.getLibraryLocation().getName());
            view.setLocationID(rc.getLibraryLocation().getId());
            view.setDocents(getDocents(rc));

            Integer reviserID = job.getReviserID();
            if (reviserID != null) {
                User reviser = userDAO.getUserById(reviserID);
                if (reviser != null) {
                    view.setReviser(reviser.getRealname());
                    view.setReviserID(reviserID);
                }
            }

            solrService.addBean(view, SolrService.Core.ScanJob);
        } else {
            LOG.warn("scan job " + job + " does not belong to a scannable");
        }
    }

    private List<String> getDocents(ReserveCollection collection) {
        return collection.getDocentParticipations().stream()
                .filter(p -> p.getEndDate() == null)
                .map(p -> userDAO.getUserById(p.getUserId()))
                .filter(u -> u != null)
                .map(u -> u.getRealname())
                .collect(Collectors.toList());
    }

    private void deleteScanJobFromSolr(ScanJob job) {

        solrService.deleteByID(job, SolrService.Core.ScanJob);
    }

    private static class MailContextBuilder {

        private VelocityContext context = new VelocityContext();

        static MailContextBuilder create() {
            return new MailContextBuilder();
        }

        VelocityContext build() {
            return context;
        }

        MailContextBuilder collectionLink(String collectionLink) {
            context.put("collectionLink", collectionLink);
            return this;
        }

        MailContextBuilder authors(String authors) {
            context.put("authors", authors);
            return this;
        }

        MailContextBuilder origin(String origin) {
            context.put("origin", origin);
            return this;
        }

        MailContextBuilder entryLink(String entryLink) {
            context.put("entryLink", entryLink);
            return this;
        }

        MailContextBuilder chapter(BookChapter chapter) {
            context.put("chapter", chapter);
            return this;
        }

        MailContextBuilder article(JournalArticle article) {
            context.put("article", article);
            return this;
        }

        MailContextBuilder oldPageStart(String oldPageStart) {
            context.put("oldPageStart", oldPageStart);
            return this;
        }

        MailContextBuilder oldPageEnd(String oldPageEnd) {
            context.put("oldPageEnd", oldPageEnd);
            return this;
        }

        MailContextBuilder roles(List<Role> roles) {
            context.put("roles", roles);
            return this;
        }
    }
}
