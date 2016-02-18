package unidue.rc.system;

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

import org.apache.cayenne.di.Inject;
import org.apache.commons.mail.EmailException;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unidue.rc.dao.MailDAO;
import unidue.rc.model.Mail;

/**
 * Created by nils on 07.07.15.
 */
public class MailCronJobImpl extends BaseCronJob implements MailCronJob {

    private static final Logger LOG = LoggerFactory.getLogger(MailCronJobImpl.class);

    private static final int MAX_MAILS_TO_SEND = 50;

    @Inject
    private MailService mailService;

    @Inject
    private SystemConfigurationService config;

    @Inject
    private MailDAO mailDAO;

    @Override
    protected void run(JobExecutionContext context) {
        LOG.info("sending unsend mail");

        mailDAO.getUnsendMails().stream()
                .filter(mail -> mail.getNumTries() < config.getInt("mail.max.tries"))
                .limit(MAX_MAILS_TO_SEND)
                .forEach(this::send);

        LOG.info("... mail send finished");
    }

    private void send(Mail unsendMail) {
        try {
            mailService.sendMail(unsendMail);
        } catch (EmailException e) {
            LOG.error("could not send mail", e);
        }
    }
}
