package unidue.rc.ui.pages.user;

/*
 * #%L
 * Semesterapparate
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 - 2015 Universitaet Duisburg Essen
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import miless.model.User;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;
import se.unbound.tapestry.breadcrumbs.BreadCrumb;
import se.unbound.tapestry.breadcrumbs.BreadCrumbList;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.DeleteException;
import unidue.rc.dao.ParticipationDAO;
import unidue.rc.dao.UserDAO;
import unidue.rc.model.ActionDefinition;
import unidue.rc.model.LibraryLocation;
import unidue.rc.model.Participation;
import unidue.rc.security.CollectionSecurityService;
import unidue.rc.ui.ProtectedPage;
import unidue.rc.ui.SecurityContextPage;
import unidue.rc.ui.valueencoder.BaseValueEncoder;
import unidue.rc.workflow.CollectionService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by nils on 30.07.15.
 */
@ProtectedPage
@BreadCrumb(titleKey = "my.collections")
public class Participations implements SecurityContextPage {

    @Inject
    private Logger log;

    @Inject
    private CollectionSecurityService securityService;

    @Inject
    private CollectionService collectionService;

    @Inject
    @Service(UserDAO.SERVICE_NAME)
    private UserDAO userDAO;

    @Inject
    @Service(ParticipationDAO.SERVICE_NAME)
    private ParticipationDAO participationDAO;

    @Inject
    private Messages messages;

    @SessionState
    private BreadCrumbList breadCrumbList;

    @Property
    private User user;

    @Property
    private List<Participation> participations;

    @Property
    private Participation participation;

    @SetupRender
    void onSetupRender() {

        breadCrumbList.getLastCrumb().setTitle(getCaption());
    }

    void onActivate(Integer userID) {
        this.user = userDAO.getUserById(userID);

        List<Participation> activeParticipations = participationDAO.getActiveParticipations(this.user);

        // filter and sort locations of participations
        Stream<LibraryLocation> locations = activeParticipations.stream()
                .map(participation -> participation.getReserveCollection().getLibraryLocation())
                .distinct()
                .sorted((l1, l2) -> l1.getName().compareTo(l2.getName()));

        this.participations = new ArrayList<>();

        // for each location (sorted) add all participations
        locations.forEach(location -> {

            // sort participations by number
            List<Participation> participationsByLocation = activeParticipations.stream()
                    .filter(p -> p.getReserveCollection().getLibraryLocation().equals(location))
                    .sorted((p1, p2) -> p1.getReserveCollection().getNumber().compareTo(p2.getReserveCollection().getNumber()))
                    .collect(Collectors.toList());
            this.participations.addAll(participationsByLocation);
        });
    }

    @OnEvent(EventConstants.PASSIVATE)
    Integer onPassivate() {
        return user != null ? user.getId() : null;
    }

    @OnEvent("removeParticipation")
    public void onRemoveParticipation(Integer participationID) {

        Participation participation = participationDAO.get(Participation.class, participationID);
        try {
            collectionService.endParticipation(participation);
        } catch (CommitException e) {
            log.error("Could not end  participation " + this.participation + " " + e);
        } catch (DeleteException e) {
            log.error("could not remove permissions of participation " + this.participation + " " + e);

        }
    }

    public boolean isParticipationEndingAllowed() {
        return collectionService.isParticipationEndingAllowed(participation)
                || securityService.isPermitted(ActionDefinition.DELETE_OWN_PARTICIPATION, participation.getReserveCollection().getId());
    }

    public String getCaption() {
        User currentUser = securityService.getCurrentUser();
        return user.equals(currentUser)
                ? messages.get("my.collections")
                : messages.format("collections.of.user", user.getRealname());
    }

    public String getStatus() {
        return messages.get(participation.getReserveCollection().getStatus().name());
    }

    public BaseValueEncoder<Participation> getParticipationEncoder() {
        return new BaseValueEncoder(Participation.class, participationDAO);
    }

    @Override
    public void checkPermission(CollectionSecurityService securityService, EventContext activationContext) throws AuthorizationException {
        User currentUser = securityService.getCurrentUser();
        Integer targetUserID = activationContext.get(Integer.class, 0);
        if (currentUser != null
                // current user wants to edit his own details
                && currentUser.getId().equals(targetUserID)
                // but has no access
                && !securityService.isPermitted(ActionDefinition.EDIT_USER, targetUserID)) {

            // create permissions
            try {
                securityService.createInstancePermissions(targetUserID, targetUserID, User.class, ActionDefinition.EDIT_USER);
            } catch (CommitException e) {
                throw new AuthorizationException(e);
            }
        }
        securityService.checkPermission(ActionDefinition.EDIT_USER, targetUserID);
    }
}
