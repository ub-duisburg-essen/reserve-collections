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
package unidue.rc.migration;


import org.apache.cayenne.di.Inject;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.mail.EmailException;
import org.apache.velocity.VelocityContext;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.MigrationDAO;
import unidue.rc.model.Mail;
import unidue.rc.model.Migration;
import unidue.rc.model.ReserveCollection;
import unidue.rc.model.legacy.Document;
import unidue.rc.system.BaseCronJob;
import unidue.rc.system.MailService;
import unidue.rc.system.SystemConfigurationService;
import unidue.rc.system.SystemMessageService;

import java.io.IOException;
import java.util.Collection;

/**
 * Created by nils on 12.08.15.
 */
public class DurableCollectionMigrationCronJobImpl extends BaseCronJob implements DurableCollectionMigrationCronJob {

    private static final Logger LOG = LoggerFactory.getLogger(DurableCollectionMigrationCronJobImpl.class);

    private static String[] RECIPIENTS = {
            "nils.verheyen@uni-due.de",
            "sonja.hendriks@uni-due.de",
            "katrin.falkenstein-feldhoff@uni-due.de"
    };

    @Inject
    private MigrationDAO migrationDAO;

    @Inject
    private LegacyXMLService legacyXMLService;

    @Inject
    private MigrationService migrationService;

    @Inject
    private MailService mailService;

    @Inject
    private SystemMessageService messages;

    @Inject
    private SystemConfigurationService config;

    @Override
    protected void run(JobExecutionContext context) throws JobExecutionException {

        LOG.info("running migration");

        Collection<Document> durableCollections;
        try {
            durableCollections = legacyXMLService.getDurableCollectionDocIDs();
        } catch (ConfigurationException e) {
            throw new JobExecutionException("could not get durable collections", e);
        }
        LOG.info("durable collections: " + durableCollections.size());
        durableCollections.stream()
                .map(document -> migrationDAO.getMigrationByDocID(document.getId().toString()))
                .filter(migration -> migration != null)
                .filter(migration -> !migration.isFinished())
                .forEach(this::perform);

        LOG.info("... migration finished");
    }

    private void perform(Migration migration) {
        VelocityContext context = new VelocityContext();

        String subject;

        context.put("migration", migration);
        try {
            ReserveCollection collection = migrationService.migrateReserveCollection(migration, migration.getMigrationCode());
            context.put("collection", collection);
            context.put("status", collection.getStatus().name());

            subject = messages.get("migration.finished");

        } catch (MigrationException | ConfigurationException e) {
            context.put("errorMessage", e.getMessage());
            context.put("errorStacktrace", ExceptionUtils.getStackTrace(e));

            subject = messages.get("migration.failed");
        }

        // send mail
        Mail mail = null;
        try {
            mail = mailService.builder("/vt/mail.migration.vm")
                    .from(config.getString("mail.from"))
                    .subject(subject.toString())
                    .context(context)
                    .addRecipients(RECIPIENTS)
                    .create();

            mailService.sendMail(mail);
        } catch (EmailException e) {
            LOG.error("could not send mail", e);
        } catch (IOException e) {
            LOG.error("could not create mail", e);
        }

        if (mail != null) {
            migration.addToMails(mail);
            try {
                migrationDAO.update(migration);
            } catch (CommitException e) {
                LOG.error("could not update migration in db", e);
            }
        }
    }
}
