package unidue.rc.ui.pages.admin;

import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.TextField;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;
import se.unbound.tapestry.breadcrumbs.BreadCrumb;
import unidue.rc.dao.BaseDAO;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.DeleteException;
import unidue.rc.dao.LibraryLocationDAO;
import unidue.rc.model.LibraryLocation;
import unidue.rc.model.OrderMailRecipient;
import unidue.rc.system.SystemConfigurationService;
import unidue.rc.ui.ProtectedPage;
import unidue.rc.ui.selectmodel.ClassNameSelectModel;
import unidue.rc.ui.selectmodel.LibraryLocationListSelectModel;
import unidue.rc.ui.valueencoder.BaseValueEncoder;
import unidue.rc.ui.valueencoder.ClassNameValueEncoder;
import unidue.rc.ui.valueencoder.LibraryLocationValueEncoder;
import unidue.rc.workflow.ScannableService;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by nils on 17.10.16.
 */
@Import(library = {
        "context:vendor/jquery-chosen/chosen/chosen.jquery.js",
        "context:js/apply.jquery.chosen.js"
}, stylesheet = {
        "context:vendor/jquery-chosen/chosen/chosen.css"
})
@BreadCrumb(titleKey = "order.mail.recipients")
@ProtectedPage
public class OrderMailRecipients {

    private static final ClassNameValueEncoder CLASS_NAME_VALUE_ENCODER = new ClassNameValueEncoder();

    private static final Comparator<OrderMailRecipient> RECIPIENT_COMPARATOR = (r1, r2) -> {
        int i = r1.getLocation().getName().compareTo(r2.getLocation().getName());
        if (i != 0)
            return i;
        return r1.getMail().compareTo(r2.getMail());
    };

    @Inject
    private Logger log;

    @Inject
    @Service(BaseDAO.SERVICE_NAME)
    private BaseDAO baseDAO;

    @Inject
    @Service(LibraryLocationDAO.SERVICE_NAME)
    private LibraryLocationDAO locationDAO;

    @Inject
    private ScannableService scannableService;

    @Inject
    private SystemConfigurationService config;

    @Inject
    private Messages messages;

    @Component(id = "recipientForm")
    private Form recipientForm;

    @Component(id = "mail")
    private TextField mailField;

    @Property
    private List<OrderMailRecipient> recipients;

    @Property
    private OrderMailRecipient recipient;

    @Property
    private String errorMessage;

    @Property
    private LibraryLocation location;

    @Property
    private Class instanceClass;

    @Property
    private String mail;

    private InternetAddress validatedAddress;

    @OnEvent(EventConstants.ACTIVATE)
    void onActivate() {
        recipients = new ArrayList<>();
        loadRecipients(recipients, 0, BaseDAO.MAX_RESULTS);
        recipients = recipients.stream()
                .sorted(RECIPIENT_COMPARATOR)
                .collect(Collectors.toList());
    }

    private void loadRecipients(List<OrderMailRecipient> sink, int offset, int maxResults) {
        List<OrderMailRecipient> recipients = baseDAO.get(OrderMailRecipient.class, offset, maxResults);
        sink.addAll(recipients);
        if (recipients.size() >= maxResults)
            loadRecipients(sink, offset + maxResults, maxResults);
    }

    @OnEvent(value = "remove")
    void onRemoveRecipient(OrderMailRecipient recipient) {
        try {
            scannableService.removeOrderMailRecipient(recipient);
        } catch (DeleteException e) {
            log.error("could not remove recipient", e);
        }
    }

    @OnEvent(value = EventConstants.VALIDATE, component = "recipientForm")
    void onValidate() {
        try {
            InternetAddress[] validatedAddresses = InternetAddress.parse(mail);
            validatedAddress = validatedAddresses != null && validatedAddresses.length > 0
                               ? validatedAddresses[0]
                               : null;
            if (validatedAddress == null)
                recipientForm.recordError(mailField, messages.get("error.msg.invalid.email"));
        } catch (AddressException e) {
            recipientForm.recordError(mailField, messages.get("error.msg.invalid.email"));
        }
    }

    @OnEvent(value = EventConstants.SUCCESS, component = "recipientForm")
    void onSuccessFromRecipientsForm() {

        try {
            scannableService.addOrderMailRecipient(location, validatedAddress, instanceClass);
        } catch (CommitException e) {
            recipientForm.recordError(messages.get("error.msg.could.not.commit.oder.mail.recipient"));
        }
    }

    public String getInstanceClassLabel() {
        return messages.get(recipient.getInstanceClass());
    }

    public ValueEncoder<OrderMailRecipient> getRecipientEncoder() {
        return new BaseValueEncoder<>(OrderMailRecipient.class, baseDAO);
    }

    public SelectModel getInstanceClassModel() {
        List<Class> classes = config.getStringArray("order.mail.recipient.classes")
                .stream()
                .map(this::loadClass)
                .filter(c -> c != null)
                .collect(Collectors.toList());
        return new ClassNameSelectModel(classes, messages);
    }

    public ValueEncoder<Class> getInstanceClassEncoder() {
        return CLASS_NAME_VALUE_ENCODER;
    }

    public SelectModel getLibraryLocationSelectModel() {
        return new LibraryLocationListSelectModel(locationDAO);
    }

    public ValueEncoder<LibraryLocation> getLibraryLocationEncoder() {
        return new LibraryLocationValueEncoder(locationDAO);
    }

    private Class loadClass(String value) {
        try {
            return Class.forName(value);
        } catch (ClassNotFoundException e) {
            log.error("could not load class " + value, e);
            return null;
        }
    }
}
