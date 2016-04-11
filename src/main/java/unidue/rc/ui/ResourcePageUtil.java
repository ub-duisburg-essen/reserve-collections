package unidue.rc.ui;


import org.apache.shiro.authz.AuthorizationException;
import org.apache.tapestry5.EventContext;
import unidue.rc.dao.ResourceDAO;
import unidue.rc.model.ActionDefinition;
import unidue.rc.model.Resource;
import unidue.rc.security.CollectionSecurityService;

/**
 * Created by nils on 18.06.15.
 */
public class ResourcePageUtil {

    public static void checkPermission(CollectionSecurityService securityService, EventContext activationContext, ResourceDAO resourceDAO) throws AuthorizationException {
        Integer resourceID = activationContext.get(Integer.class, 0);
        Resource resource = resourceDAO.get(Resource.class, resourceID);
        Integer collectionID = resource.getEntry().getReserveCollection().getId();
        securityService.checkPermission(ActionDefinition.VIEW_RESERVE_COLLECTION, collectionID);
    }
}
