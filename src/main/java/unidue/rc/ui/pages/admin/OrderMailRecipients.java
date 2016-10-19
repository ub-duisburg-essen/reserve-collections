package unidue.rc.ui.pages.admin;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.Service;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;
import se.unbound.tapestry.breadcrumbs.BreadCrumb;
import unidue.rc.dao.BaseDAO;
import unidue.rc.dao.DeleteException;
import unidue.rc.dao.LibraryLocationDAO;
import unidue.rc.model.LibraryLocation;
import unidue.rc.model.OrderMailRecipient;
import unidue.rc.system.SystemConfigurationService;
import unidue.rc.ui.ProtectedPage;
import unidue.rc.ui.selectmodel.ClassNameSelectModel;
import unidue.rc.ui.selectmodel.LibraryLocationListSelectModel;
import unidue.rc.ui.selectmodel.LibraryLocationSelectModel;
import unidue.rc.ui.valueencoder.BaseValueEncoder;
import unidue.rc.ui.valueencoder.ClassNameValueEncoder;
import unidue.rc.ui.valueencoder.LibraryLocationValueEncoder;
import unidue.rc.workflow.ScannableService;

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

    @Property
    private List<OrderMailRecipient> recipients;

    @Property
    private OrderMailRecipient recipient;

    @Property
    private String errorMessage;

    @Property
    private LibraryLocation location;

    @Property
    private String instanceClass;

    @Property
    private String mail;

    @OnEvent(value = "remove")
    void onRemoveRecipient(OrderMailRecipient recipient) {
        try {
            scannableService.removeOrderMailRecipient(recipient);
        } catch (DeleteException e) {
            log.error("could not remove recipient", e);
        }
    }

    @OnEvent(value = EventConstants.SUCCESS, component = "recipientForm")
    void onSuccessFromRecipientsForm() {

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
