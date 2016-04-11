package unidue.rc.ui.pages.webservices.moodle;


import miless.model.User;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.slf4j.Logger;
import unidue.rc.dao.ResourceDAO;
import unidue.rc.dao.UserDAO;
import unidue.rc.io.AttachmentStreamResponse;
import unidue.rc.model.Resource;
import unidue.rc.plugins.moodle.model.ResourceRequest;
import unidue.rc.plugins.moodle.services.MoodleService;
import unidue.rc.ui.pages.Error403;
import unidue.rc.ui.pages.Error404;
import unidue.rc.workflow.ResourceService;

/**
 * Created by nils on 17.06.15.
 */
public class Download {

    @Inject
    private Logger log;

    @Inject
    private ResourceDAO resourceDAO;

    @Inject
    private UserDAO userDAO;

    @Inject
    private ResourceService resourceService;

    @Inject
    private PageRenderLinkSource linkSource;

    @Inject
    private MoodleService moodleService;

    Object onActivate(String sessionID) {
        ResourceRequest request = moodleService.getResourceRequest(sessionID);
        if (request == null)
            return linkSource.createPageRenderLink(Error403.class);

        Resource resource = resourceDAO.get(Resource.class, request.getResourceID());
        if (resource == null || resource.getFileDeleted() != null)
            return linkSource.createPageRenderLink(Error404.class);

        User user = userDAO.getUser(request.getUsername(), moodleService.getRealm(request.getAuthtype()));

        java.io.File file = resourceService.download(resource, user);
        return new AttachmentStreamResponse(file, resource);
    }
}
