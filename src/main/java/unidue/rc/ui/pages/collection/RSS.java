package unidue.rc.ui.pages.collection;


import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.HttpError;
import org.slf4j.Logger;
import unidue.rc.dao.ParticipationDAO;
import unidue.rc.dao.ReserveCollectionDAO;
import unidue.rc.io.CollectionRSSWriter;
import unidue.rc.io.XMLStreamResponse;
import unidue.rc.model.Participation;
import unidue.rc.model.ReserveCollection;
import unidue.rc.model.rss.Channel;
import unidue.rc.security.CollectionSecurityService;
import unidue.rc.ui.ProtectedPage;
import unidue.rc.ui.SecurityContextPage;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by nils on 20.10.15.
 */
@ProtectedPage(isAuthenticationNeeded = false)
public class RSS implements SecurityContextPage {

    @Inject
    private Logger log;

    @Inject
    private ReserveCollectionDAO collectionDAO;

    @Inject
    private ParticipationDAO participationDAO;

    @Inject
    private CollectionRSSWriter writer;

    @OnEvent(EventConstants.ACTIVATE)
    Object onActivate(Integer collectionID, Integer userID, String accessKey) {
        ReserveCollection collection = collectionDAO.get(ReserveCollection.class, collectionID);
        if (collection == null)
            return new HttpError(HttpServletResponse.SC_NOT_FOUND, "could not find collection with id " + collectionID);

        log.debug(String.format("user: %7d accessing collection: %7d", userID, collectionID));

        Channel rss = writer.serialize(collection);
        return new XMLStreamResponse(rss);
    }

    @Override
    public void checkPermission(CollectionSecurityService securityService, EventContext activationContext) throws AuthorizationException {
        Integer collectionID = activationContext.get(Integer.class, 0);
        Integer userID = activationContext.get(Integer.class, 1);
        String accessKey = activationContext.get(String.class, 2);

        Participation participation = participationDAO.getActiveParticipation(userID, collectionID);
        if (participation == null || !StringUtils.equals(accessKey, participation.getAccessKey()))
            throw new AuthorizationException("retrieval of rss feed not allowed");
    }
}
