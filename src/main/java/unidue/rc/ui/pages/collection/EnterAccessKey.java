package unidue.rc.ui.pages.collection;


import miless.model.User;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.TextField;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import se.unbound.tapestry.breadcrumbs.BreadCrumb;
import se.unbound.tapestry.breadcrumbs.BreadCrumbInfo;
import se.unbound.tapestry.breadcrumbs.BreadCrumbList;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.DeleteException;
import unidue.rc.dao.ParticipationDAO;
import unidue.rc.dao.ReserveCollectionDAO;
import unidue.rc.model.ReserveCollection;
import unidue.rc.security.CollectionSecurityService;
import unidue.rc.ui.ProtectedPage;
import unidue.rc.workflow.CollectionService;

import java.util.Optional;

/**
 * Created by nils on 28.05.15.
 */
@BreadCrumb(titleKey = "accessKey-label")
@ProtectedPage
public class EnterAccessKey {

    @Inject
    private CollectionService collectionService;

    @Inject
    private CollectionSecurityService securityService;

    @Inject
    private ReserveCollectionDAO collectionDAO;

    @Inject
    private ParticipationDAO participationDAO;

    @Inject
    private Messages messages;

    @Inject
    private PageRenderLinkSource linkSource;

    @Property
    private ReserveCollection collection;

    @Property
    private String accessKey;

    @InjectComponent("access_key_form")
    private Form accessKeyForm;

    @InjectComponent("accesskey")
    private TextField accessKeyField;

    @SessionState
    private BreadCrumbList breadCrumbList;

    @SetupRender
    void onSetupRender() {

        Optional<BreadCrumbInfo> collectionPageCrumb = breadCrumbList.getCrumbsToDisplay()
                .stream()
                .filter(breadCrumbInfo -> breadCrumbInfo.getPageName().equalsIgnoreCase("collection/view"))
                .findFirst();
        if (collectionPageCrumb.isPresent())
            collectionPageCrumb.get().setTitle(collection.getTitle());
    }

    @OnEvent(EventConstants.ACTIVATE)
    @RequiresAuthentication
    void onActivate(Integer collectionID) {
        collection = collectionDAO.get(ReserveCollection.class, collectionID);
    }

    @OnEvent(EventConstants.PASSIVATE)
    Integer onPassivate() {
        return collection.getId();
    }

    @OnEvent(value = EventConstants.VALIDATE, component = "access_key_form")
    void onValidateFromEnterAccessKey() {
        User currentUser = securityService.getCurrentUser();
        if (collectionService.isAccessKeyValid(accessKey, collection)) {


            try {
                collectionService.createParticipation(currentUser, collection, accessKey);
            } catch (CommitException e) {
                accessKeyForm.recordError(messages.get("error.msg.cant.create.participation"));
            } catch (DeleteException e) {
                accessKeyForm.recordError(messages.get("error.msg.cant.end.participation"));
            }
        } else {
            accessKeyForm.recordError(accessKeyField, messages.get("error.msg.invalid.access.key"));
        }
    }

    @OnEvent(value = EventConstants.SUCCESS, component = "access_key_form")
    Link onSuccess() {
        return linkSource.createPageRenderLinkWithContext(ViewCollection.class, collection.getId());
    }

    public String getInfoMessage() {
        return messages.format("info.message.enter.access.key", collection.getTitle());
    }
}
