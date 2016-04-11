package unidue.rc.ui.pages.entry.file;


import org.apache.shiro.authz.AuthorizationException;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.slf4j.Logger;
import se.unbound.tapestry.breadcrumbs.BreadCrumb;
import se.unbound.tapestry.breadcrumbs.BreadCrumbList;
import unidue.rc.dao.ResourceDAO;
import unidue.rc.model.Resource;
import unidue.rc.security.CollectionSecurityService;
import unidue.rc.ui.SecurityContextPage;
import unidue.rc.ui.ProtectedPage;
import unidue.rc.ui.ResourcePageUtil;
import unidue.rc.ui.pages.Media;

/**
 * @author Nils Verheyen
 * @since 15.10.14 11:09
 */
@BreadCrumb(titleKey = "text")
@ProtectedPage
public class Image implements SecurityContextPage {

    @Inject
    private Logger log;

    @Inject
    private PageRenderLinkSource linkSource;

    @Inject
    private ResourceDAO resourceDAO;

    @Property
    private Resource image;

    @SessionState
    private BreadCrumbList breadCrumbList;

    @SetupRender
    public void beginRender() {
        breadCrumbList.getLastCrumb().setTitle(image.getFileName());
    }

    @OnEvent(EventConstants.ACTIVATE)
    void onActivate(Integer resourceID) {
        image = resourceDAO.get(Resource.class, resourceID);
    }

    @OnEvent(EventConstants.PASSIVATE)
    Object onPassivate() {
        return image.getId();
    }

    public Link getOriginalImage() {
        return linkSource.createPageRenderLinkWithContext(Media.class, image.getId());
    }

    @Override
    public void checkPermission(CollectionSecurityService securityService, EventContext activationContext) throws AuthorizationException {
        ResourcePageUtil.checkPermission(securityService, activationContext, resourceDAO);
    }

}
