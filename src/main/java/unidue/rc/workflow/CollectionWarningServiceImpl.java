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
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.EmailException;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unidue.rc.dao.*;
import unidue.rc.model.*;
import unidue.rc.system.*;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by nils on 21.03.16.
 */
public class CollectionWarningServiceImpl implements CollectionWarningService {

    private static final Logger LOG = LoggerFactory.getLogger(CollectionWarningServiceImpl.class);

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd. MMMM yyyy");

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

    @Inject
    private MailService mailService;

    @Inject
    private BaseURLService urlService;

    @Inject
    private SystemMessageService messages;

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
            try {
                Mail mail = sendWarning(cd.docent, cd.collections);
                for (ReserveCollection collection : cd.collections) {
                    LOG.info(String.format("    %1$s", collection));
                    saveWarning(mail, collection, cd.docent);
                }
            } catch (EmailException e) {
                LOG.error("could not send mail", e);
            }
        }
    }

    private Warning saveWarning(Mail mail, ReserveCollection collection, User user) {
        try {

            return warningDAO.create(mail, collection, user, calculateWarningDate(collection));
        } catch (CommitException e) {
            LOG.error("could not create warning for " + mail + "; " + collection + "; " + user);
            return null;
        }
    }

    private Mail sendWarning(User docent, List<ReserveCollection> collections) throws EmailException {
        VelocityContext context = new VelocityContext();

        String subject = messages.get("mail.subject.expiring.collections");
        if (collections.size() == 1) {

            ReserveCollection collection = collections.get(0);
            context.put("collection", collection);
            context.put("prolongLink", urlService.getProlongLink(collection));
        } else if (collections.size() > 1) {

            Map<ReserveCollection, String> collectionsParam = collections.stream()
                    .collect(Collectors.toMap(Function.identity(), c -> urlService.getProlongLink(c)));

            context.put("collections", collectionsParam);
            context.put("strformatter", new StringFormatter());
        }
        context.put("dateformatter", DATE_FORMAT);
        String recipient = docent.getEmail();
        String from = config.getString("mail.from");

        try {
            MailServiceImpl.MailBuilder mailBuilder = mailService.builder("/vt/expiration.warning.msg.html.vm")
                    .from(from)
                    .subject(subject.toString())
                    .addBcc(config.getString("system.mail"))
                    .textTemplateName("/vt/expiration.warning.msg.text.vm")
                    .context(context)
                    .addRecipients(recipient);

            Mail mail = mailBuilder.create();
            mailService.sendMail(mail);
            return mail;
        } catch (IOException e) {
            LOG.error("could not create mail", e);
        }
        return null;
    }

    private List<CollectionsByUser> filter(List<CollectionsByUser> data) {
        for (CollectionsByUser cd : data) {

            cd.collections = cd.collections.stream()
                    .filter(c -> !hasBeenWarned(cd.docent, c))
                    .collect(Collectors.toList());
        }

        data = data.stream()
                .filter(d -> d.collections != null && !d.collections.isEmpty())
                .filter(d -> {
                    boolean isMailGiven = StringUtils.isNotBlank(d.docent.getEmail());
                    if (!isMailGiven)
                        LOG.warn("docent " + d.docent.getUserid() + " has no email address");
                    return isMailGiven;
                })
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

    public static class StringFormatter {

        public String format(String value, String format) {
            return value != null
                   ? String.format(value, format)
                   : null;
        }
    }

    private static class CollectionsByUser {

        private User docent;
        private List<ReserveCollection> collections;

        public CollectionsByUser() {
            this.collections = new ArrayList<>();
        }
    }
}
