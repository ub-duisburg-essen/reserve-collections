package unidue.rc.workflow;

/*
 * #%L
 * Semesterapparate
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 - 2016 Universitaet Duisburg Essen
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
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.SelectQuery;
import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unidue.rc.dao.*;
import unidue.rc.model.*;
import unidue.rc.system.DateConvertUtils;
import unidue.rc.system.SystemConfigurationService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by nils on 21.03.16.
 */
public class CollectionWarningServiceImpl implements CollectionWarningService {

    private static final Logger LOG = LoggerFactory.getLogger(CollectionWarningServiceImpl.class);

    @Inject
    private SystemConfigurationService config;

    @Inject
    private ReserveCollectionDAO collectionDAO;

    @Inject
    private ParticipationDAO participationDAO;

    @Inject
    private RoleDAO roleDAO;

    @Inject
    private UserDAO userDAO;

    @Inject
    private WarningDAO warningDAO;

    private Integer daysUntilFirstWarning;
    private Integer daysUntilSecondWarning;
    private int maxDaysUntilWarning;
    private LocalDate baseDate;

    @Override
    public void sendWarnings(LocalDate baseDate) throws ConfigurationException {
        this.daysUntilFirstWarning = config.getInt("days.until.first.warning");
        this.daysUntilSecondWarning = config.getInt("days.until.second.warning");
        this.maxDaysUntilWarning = Math.max(daysUntilFirstWarning, daysUntilSecondWarning);
        this.baseDate = baseDate;

        if (daysUntilFirstWarning == null)
            throw new ConfigurationException("days.until.first.warning not configured");
        if (daysUntilSecondWarning == null)
            throw new ConfigurationException("days.until.second.warning not configured");

        Role docentRole = roleDAO.getRole(DefaultRole.DOCENT);

        // initialize base data that is needed for warning messages
        List<ReserveCollection> collections = collectionDAO.getExpiringCollections(baseDate, maxDaysUntilWarning,
                ReserveCollectionStatus.ACTIVE);
        List<Participation> participations = participationDAO.getActiveParticipations(collections, docentRole);

        // group collections by docent
        List<CollectionsByUser> data = group(participations, collections);

        // remove collections whose docent was already warned
        data = filter(data);

        // send warnings
        for (CollectionsByUser cd : data) {
            LOG.info(String.format("%s", cd.docent));
            for (ReserveCollection collection : cd.collections) {
                LOG.info(String.format("    %1$s", collection));
            }
        }
    }

    private List<CollectionsByUser> filter(List<CollectionsByUser> data) {
        for (CollectionsByUser cd : data) {

            cd.collections = cd.collections.stream()
                    .filter(c -> !hasBeenWarned(cd.docent, c))
                    .collect(Collectors.toList());
        }

        data = data.stream()
                .filter(d -> d.collections != null && !d.collections.isEmpty())
                .collect(Collectors.toList());
        return data;
    }

    private boolean hasBeenWarned(User user, ReserveCollection collection) {
        LocalDate warningDate = calculateWarningDate(collection);
        List<Warning> warnings = warningDAO.getWarnings(user.getUserid(), collection);
        return warnings.stream()
                .anyMatch(w -> DateConvertUtils.asLocalDate(w.getCalculatedFor()).isEqual(warningDate));
    }

    private LocalDate calculateWarningDate(ReserveCollection collection) {
        LocalDate validTo = DateConvertUtils.asLocalDate(collection.getValidTo());

        LocalDate firstWarning = validTo.minusDays(daysUntilFirstWarning);
        LocalDate secondWarning = validTo.minusDays(daysUntilSecondWarning);


        if (isAfterOrEqual(baseDate, firstWarning) && baseDate.isBefore(secondWarning))
            return firstWarning;
        else
            return secondWarning;
    }

    private List<CollectionsByUser> group(List<Participation> participations, List<ReserveCollection> collections) {

        // extract unique users ids from all given participations
        List<Integer> docentIDs = participations.stream()
                .map(d -> d.getUserId())
                .distinct()
                .collect(Collectors.toList());

        // cache users for participations
        List<User> users = userDAO.getUsers(docentIDs);

        List<CollectionsByUser> result = new ArrayList<>();

        // go through all collections, participations and users to group the correct data together
        for (ReserveCollection c : collections) {

            for (Participation p : participations) {

                for (User u : users) {

                    // maybe a user with collections is already present in the result
                    Optional<CollectionsByUser> optional = result.stream()
                            .filter(collectionsByUser -> collectionsByUser.docent.equals(u))
                            .findAny();

                    CollectionsByUser value;

                    // if not add a new one
                    if (!optional.isPresent()) {
                        CollectionsByUser collectionsByUser = new CollectionsByUser();
                        collectionsByUser.docent = u;
                        result.add(collectionsByUser);
                        value = collectionsByUser;
                    } else {
                        value = optional.get();
                    }

                    // add the collection that current user participates in necessary
                    if (p.getCollectionID().equals(c.getId())
                            && u.getUserid().equals(p.getUserId())) {
                        value.collections.add(c);
                    }
                }
            }
        }

        return result;
    }

    private static boolean isAfterOrEqual(LocalDate date, LocalDate comparator) {
        return date.isAfter(comparator) || date.isEqual(comparator);
    }

    private static class CollectionsByUser {

        private User docent;
        private List<ReserveCollection> collections;

        public CollectionsByUser() {
            this.collections = new ArrayList<>();
        }
    }
}
