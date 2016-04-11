package unidue.rc.ui.pages.participation;


import miless.model.User;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.HttpError;
import org.slf4j.Logger;
import se.unbound.tapestry.breadcrumbs.BreadCrumb;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.DeleteException;
import unidue.rc.dao.ParticipationDAO;
import unidue.rc.dao.ReserveCollectionDAO;
import unidue.rc.dao.RoleDAO;
import unidue.rc.dao.UserDAO;
import unidue.rc.model.ActionDefinition;
import unidue.rc.model.Participation;
import unidue.rc.model.ReserveCollection;
import unidue.rc.model.Role;
import unidue.rc.security.RequiresActionPermission;
import unidue.rc.ui.ProtectedPage;
import unidue.rc.workflow.CollectionService;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by marcus.koesters on 29.04.15.
 */

@BreadCrumb(titleKey = "view.participation")
@ProtectedPage
public class ViewParticipation {

    @Inject
    private Logger log;

    @Inject
    private Messages messages;

    @Inject
    private ReserveCollectionDAO collectionDAO;

    @Inject
    private UserDAO userDAO;

    @Inject
    private RoleDAO roleDAO;

    @Inject
    private ParticipationDAO participationDAO;

    @Inject
    CollectionService collectionService;

    @Property
    private ReserveCollection collection;

    @Property
    private Role role;

    @Property
    private Participation participation;

    @SetupRender
    public void beginRender() {
    }

    @OnEvent(EventConstants.ACTIVATE)
    @RequiresActionPermission(ActionDefinition.EDIT_RESERVE_COLLECTION)
    Object onPageActivate(int reserveCollectionId) {
        collection = collectionDAO.get(ReserveCollection.class, reserveCollectionId);
        // load resources
        if (collection == null)
            return new HttpError(HttpServletResponse.SC_NOT_FOUND, messages.get("error.msg.collection.not.found"));
        return null;
    }


    public List<Participation> getParticipations() {
        return participationDAO.getActiveParticipations(role, collection);
    }

    public List<Role> getRoles() {
        List<Role> roles = roleDAO.getRoles();
        return sortRoles(roles);
    }

    public User getUser() {
        return userDAO.getUserById(participation.getUserId());
    }


    @OnEvent(value = "deleteParticipation")
    Object onDeleteParticipation(Integer participationID) {
        Participation participation = participationDAO.get(Participation.class, participationID);
        try {
            collectionService.endParticipation(participation);
        } catch (CommitException e) {
            log.error("Could not end  participation " + this.participation + " " + e);
        } catch (DeleteException e) {
            log.error("could not remove permissions of participation " + this.participation + " " + e);

        }
        return this;
    }

    @OnEvent(EventConstants.PASSIVATE)
    Integer onPagePassivate() {
        return collection != null ? collection.getId() : null;
    }

    public boolean isParticipationEndingAllowed() {
        return collectionService.isParticipationEndingAllowed(participation);
    }

    /**
     * Sorts the list of roles by its name.
     *
     * @param result
     * @return
     */
    private List<Role> sortRoles(List<Role> result) {
        return result.stream()
                .sorted((r1, r2) -> r1.getName().compareTo(r2.getName()))
                .collect(Collectors.toList());
    }
}
