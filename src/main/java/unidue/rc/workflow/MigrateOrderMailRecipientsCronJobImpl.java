package unidue.rc.workflow;

import org.apache.cayenne.di.Inject;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.OrderMailRecipientDAO;
import unidue.rc.model.*;
import unidue.rc.system.BaseCronJob;
import unidue.rc.system.SystemConfigurationService;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Created by nils on 20.10.16.
 */
public class MigrateOrderMailRecipientsCronJobImpl extends BaseCronJob implements MigrateOrderMailRecipientsCronJob {

    private static final Logger LOG = LoggerFactory.getLogger(MigrateOrderMailRecipientsCronJobImpl.class);

    @Inject
    private SystemConfigurationService config;

    @Inject
    private OrderMailRecipientDAO recipientDAO;

    @Override
    protected void run(JobExecutionContext context) throws JobExecutionException {

        migrateCollectionRecipients();
        migrateChapterRecipients();
        migrateArticleRecipients();
        migrateBookRecipients();
    }

    private void migrateCollectionRecipients() {
        Stream.of(CollectionAdmin.values())
                .forEach(admin -> Stream.of(admin.getLocations())
                        .forEach(adminLocation -> migrate(admin.getMail(), adminLocation, ReserveCollection.class)));
    }

    private void migrateChapterRecipients() {
        Stream.of(ScannableOrderAdmin.values())
                .forEach(admin -> Stream.of(admin.getLocations())
                        .forEach(adminLocation -> migrate(admin.getMail(), adminLocation, BookChapter.class)));
    }

    private void migrateArticleRecipients() {
        Stream.of(ScannableOrderAdmin.values())
                .forEach(admin -> Stream.of(admin.getLocations())
                        .forEach(adminLocation -> migrate(admin.getMail(), adminLocation, JournalArticle.class)));
    }

    private void migrateBookRecipients() {
        Stream.of(BookOrderAdmin.values())
                .forEach(admin -> Stream.of(admin.getLocations())
                        .forEach(adminLocation -> migrate(admin.getMail(), adminLocation, Book.class)));
    }

    private void migrate(String mail, DefaultLocation targetLocation, Class targetClass) {
        LibraryLocation location = recipientDAO.get(LibraryLocation.class, targetLocation.getId());
        List<OrderMailRecipient> recipients = recipientDAO.getRecipients(location, targetClass);

        Predicate<OrderMailRecipient> predicate = this.buildCollectionEqualPredicate(location, mail, targetClass);
        boolean isPresent = recipients.stream().filter(predicate).findAny().isPresent();
        if (!isPresent) {

            try {
                recipientDAO.addOrderMailRecipient(location, buildAddress(mail), targetClass);
            } catch (CommitException e) {
                LOG.error("could not add recipient of " + location + " with " + mail + " to class " + targetClass);
            }
        }
    }

    private Predicate<OrderMailRecipient> buildCollectionEqualPredicate(LibraryLocation targetLocation, String targetMail, Class targetClass) {
        return obj -> obj.getLocation().equals(targetLocation)
                && StringUtils.equals(obj.getMail(), targetMail)
                && StringUtils.equals(obj.getInstanceClass(), targetClass.getName());
    }

    private InternetAddress buildAddress(String mail) {
        try {
            InternetAddress[] addresses = InternetAddress.parse(mail);
            return addresses != null && addresses.length > 0
                   ? addresses[0]
                   : null;
        } catch (AddressException e) {
            LOG.error("could not build email from " + mail);
            return null;
        }
    }
}
