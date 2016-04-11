package unidue.rc.system;


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
