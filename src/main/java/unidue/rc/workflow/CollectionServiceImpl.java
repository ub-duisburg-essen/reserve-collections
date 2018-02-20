/**
 * Copyright (C) 2014 - 2017 Universitaet Duisburg-Essen (semapp|uni-due.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package unidue.rc.workflow;


import miless.model.User;
import org.apache.cayenne.di.Inject;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ContextedException;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.mail.EmailException;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unidue.rc.dao.*;
import unidue.rc.model.*;
import unidue.rc.model.solr.SolrCollectionView;
import unidue.rc.search.SolrService;
import unidue.rc.security.CollectionSecurityService;
import unidue.rc.system.BaseURLService;
import unidue.rc.system.MailService;
import unidue.rc.system.SystemConfigurationService;
import unidue.rc.system.SystemMessageService;

import java.io.*;
import java.io.File;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Nils Verheyen
 * @since 05.12.13 09:19
 */
public class CollectionServiceImpl implements CollectionService {

    private static final String CONFIG_VALID_SETTABLE_FROM_SUMMER = "validation.date.summer.settable.from";
    private static final String CONFIG_VALID_SETTABLE_FROM_WINTER = "validation.date.winter.settable.from";
    private static final String CONFIG_NO_SEMESTER_END = "no.semester.end";
    private static final String CONFIG_LECTURE_END_SUMMER = "lecture.end.summer";
    private static final String CONFIG_LECTURE_END_WINTER = "lecture.end.winter";
    private static final String CONFIG_SEMESTER_END_SUMMER = "summer.semester.end";
    private static final String CONFIG_SEMESTER_END_WINTER = "winter.semester.end";
    private static final String CONFIG_DAYS_TO_FIRST_WARNING = "days.until.first.warning";
    private static final String CONFIG_DAYS_TO_SECOND_WARNING = "days.until.second.warning";
    private static final String CONFIG_PROLONG_DATES = "prolong.dates";
    private static final String PROLONG_DATES_SEPARATOR = "(\\s)*,(\\s)*";

    private static final Logger LOG = LoggerFactory.getLogger(CollectionServiceImpl.class);

    @Inject
    private EntryService entryService;

    @Inject
    private SolrService solrService;

    @Inject
    private MailService mailService;

    @Inject
    private ResourceService resourceService;

    @Inject
    private SystemMessageService messages;

    @Inject
    private BaseURLService urlService;

    @Inject
    private ReserveCollectionDAO collectionDAO;

    @Inject
    private RoleDAO roleDAO;

    @Inject
    private UserDAO userDAO;

    @Inject
    private ParticipationDAO participationDAO;

    @Inject
    private ReserveCollectionNumberDAO numberDAO;

    @Inject
    private EntryDAO entryDAO;

    @Inject
    private OrderMailRecipientDAO mailRecipientDAO;

    @Inject
    private ResourceDAO resourceDAO;

    @Inject
    private SystemConfigurationService config;

    @Inject
    private CollectionSecurityService securityService;


    @Override
    public void create(ReserveCollection collection) throws CommitException {

        if (!collection.getLibraryLocation().isPhysical())
            collection.setStatus(ReserveCollectionStatus.ACTIVE);

        // normalize keys
        collection.setReadKey(collection.getReadKey().trim());
        String writeKey = collection.getWriteKey();
        if (StringUtils.isNotBlank(writeKey))
            collection.setWriteKey(writeKey.trim());

        collection.setProlongCode(RandomStringUtils.randomAlphanumeric(6));

        collectionDAO.create(collection);

        Role docentRole = roleDAO.getRole(DefaultRole.DOCENT);

        User currentUser = securityService.getCurrentUser();
        participationDAO.createParticipation(currentUser.getId(), collection, docentRole);

        securityService.createInstancePermissions(currentUser.getUserid(), collection.getId(), ReserveCollection.class,
                docentRole);


        commitCollectionToSolr(collection);

        if (collection.getLibraryLocation().isPhysical())
            sendCollectionMail(collection);

        createDefaultEntry(collection);
    }

    @Override
    public void update(ReserveCollection collection) throws CommitException {
        // update database
        collection.setModified(new Date());
        collectionDAO.update(collection);

        // update search index
        updateCollectionToSolr(collection);

        // call other services that the collection has changed
        entryService.afterCollectionUpdate(collection);
    }

    @Override
    public void renew(ReserveCollection collection) throws CommitException {

        collection.setStatus(ReserveCollectionStatus.NEW);
        update(collection);

        sendCollectionMail(collection);
    }

    @Override
    public void prolong(ReserveCollection collection, String code, Date to) throws CommitException, ContextedException {
        if (StringUtils.equals(collection.getProlongCode(), code)
                && collection.getProlongUsed() != null)
            throw new ContextedException("prolong code already used.");

        if (collection.getExpectedParticipations() == null)
            throw new ContextedException("expected participations must not be null");

        if (!StringUtils.equals(collection.getProlongCode(), code))
            throw new ContextedException("illegal prolong code used")
                    .addContextValue("is", collection.getProlongCode())
                    .addContextValue("was", code);

        collection.setValidTo(to);
        collection.setProlongUsed(new Date());
        update(collection);
    }

    private void validateActivation(ReserveCollection collection) throws CommitException {

        if (!canActivate(collection))
            throw new CommitException("collection " + collection.getId() + " can not be activated");
    }

    @Override
    public void activate(ReserveCollection collection) throws CheckNumberException, CommitException {

        validateActivation(collection);

        if (securityService.isPermitted(ActionDefinition.EDIT_RESERVE_COLLECTION_NUMBER))
            throw new CheckNumberException();

        // use original number if it is free otherwise choose a new one
        if (!numberDAO.isNumberFree(collection.getNumber().getNumber(), collection.getLibraryLocation())) {

            ReserveCollectionNumber number = buildNumber(collection.getLibraryLocation());
            collection.setNumber(number);
        }

        collection.setStatus(ReserveCollectionStatus.ACTIVE);

        update(collection);
    }

    @Override
    public void activate(ReserveCollection collection, Integer number) throws CommitException, NumberAssignedException {

        validateActivation(collection);

        ReserveCollectionNumber numberInDB = numberDAO.getNumber(number);
        if (numberInDB == null)
            numberInDB = numberDAO.create(number);
        else if (!numberInDB.isFree(collection.getLibraryLocation())) {
            throw new NumberAssignedException("number " + number + " already used");
        }

        collection.setStatus(ReserveCollectionStatus.ACTIVE);
        collection.setNumber(numberInDB);

        update(collection);
    }

    @Override
    public void deactivate(ReserveCollection collection) throws CommitException {

        // get all docents of collection
        Role docentRole = roleDAO.getRole(DefaultRole.DOCENT);
        Role assistantRole = roleDAO.getRole(DefaultRole.ASSISTANT);

        Predicate<Participation> concernedRoles = p -> !(p.getRole().equals(docentRole) || p.getRole().equals(assistantRole));

        // get all non docent|assistant participations
        List<Participation> participations = participationDAO.getActiveParticipations(collection)
                .stream()
                .filter(concernedRoles)
                .collect(Collectors.toList());
        for (Participation p : participations) {

            p.setEndDate(new Date());
            participationDAO.update(p);
        }

        collection.setReadKey(RandomStringUtils.randomAlphanumeric(6));
        collection.setWriteKey(RandomStringUtils.randomAlphanumeric(10));
        collection.setStatus(ReserveCollectionStatus.DEACTIVATED);
        update(collection);

        securityService.afterCollectionDeactivated(collection);

        sendCollectionMail(collection);
    }

    @Override
    public void deactivateExpired() {
        Integer dayCount = config.getInt("days.until.automatic.deactivation");

        LocalDate now = LocalDate.now();
        Predicate<ReserveCollection> expiredPredicate = collection ->
                now.minusDays(dayCount).isAfter(LocalDate.fromDateFields(collection.getValidTo()))
                && collection.getDissolveAt() == null
                && collection.getStatus().equals(ReserveCollectionStatus.ACTIVE);

        Predicate<ReserveCollection> dissolvePredicate = collection ->
                collection.getDissolveAt() != null
                && now.isAfter(LocalDate.fromDateFields(collection.getDissolveAt()))
                && collection.getStatus().equals(ReserveCollectionStatus.ACTIVE);

        batchDeactivate(0, BaseDAO.MAX_RESULTS, expiredPredicate, dissolvePredicate);
    }

    private void batchDeactivate(int offset, int maxResults, Predicate<ReserveCollection>...predicates) {
        List<ReserveCollection> collections = collectionDAO.get(ReserveCollection.class, offset, maxResults);
        for (ReserveCollection collection : collections) {

            boolean anyMatch = Stream.of(predicates).anyMatch(p -> p.test(collection));
            if (anyMatch) {
                try {
                    deactivate(collection);
                } catch (CommitException e) {
                    LOG.error("could not deactivate collection " + collection, e);
                }
            }
        }
        if (collections.size() >= maxResults)
            batchDeactivate(offset + maxResults, maxResults, predicates);
    }

    @Override
    public void archive(ReserveCollection collection) throws CommitException {
        endParticipations(collection);

        collection.setReadKey(RandomStringUtils.randomAlphanumeric(8));
        collection.setWriteKey(RandomStringUtils.randomAlphanumeric(12));
        collection.setStatus(ReserveCollectionStatus.ARCHIVED);
        update(collection);

        securityService.afterCollectionArchived(collection);
    }

    @Override
    public void delete(ReserveCollection collection) throws DeleteException {


        // inform services that entries where deleted
        entryService.beforeCollectionDelete(collection);

        securityService.beforeCollectionDelete(collection);

        // delete in search index
        deleteCollectionFromSolr(collection);

        // delete in backend
        collectionDAO.delete(collection);

        // inform services that entries where deleted
        entryService.afterCollectionDelete(collection);
    }

    @Override
    public boolean canActivate(final ReserveCollection collection) {
        /*
         a collection can become active if:

         - the status is not active and ..
            - the user is allowed to activate the collection or
            - the collection is e-only
          */
        return collection.isActivateable()
                && (securityService.isPermitted(ActionDefinition.ACTIVATE_RESERVE_COLLECTION)
                    || !collection.getLibraryLocation().isPhysical());
    }

    @Override
    public List<File> getFiles(ReserveCollection collection) {
        File filesDir = new File(config.getString("files.store"));
        return resourceDAO.getResourcesByCollection(collection)
                .stream()
                .filter(r -> resourceService.isDownloadAllowed(r))
                .map(r -> r.getFile(filesDir))
                .collect(Collectors.toList());
    }

    @Override
    public void afterLocationUpdate(LibraryLocation location) throws CommitException {
        rebuildSearchIndex();

        List<ReserveCollection> collections = collectionDAO.getCollections(location);
        for (ReserveCollection collection : collections) {
            entryService.afterCollectionUpdate(collection);
        }
    }

    @Override
    public void beforeLocationDelete(LibraryLocation location) throws DeleteException {
        rebuildSearchIndex();
        List<ReserveCollection> collections = collectionDAO.getCollections(location);
        for (ReserveCollection collection : collections) {
            try {
                delete(collection);
            } catch (DeleteException e) {
                LOG.error("could not delete collection " + collection, e);
            }
        }
    }

    @Override
    public void afterLocationDelete(LibraryLocation location) {
    }

    @Override
    public boolean isAccessKeyValid(String accessKey, ReserveCollection collection) {
        String readKey = collection.getReadKey();
        String writeKey = collection.getWriteKey();
        return (readKey != null && readKey.equals(accessKey))
                || (writeKey != null && writeKey.equals(accessKey));
    }

    @Override
    public void createParticipation(User user, ReserveCollection collection, String accessKey) throws CommitException, DeleteException {

        Role role = accessKey.equals(collection.getWriteKey())
                    ? roleDAO.getRole(DefaultRole.ASSISTANT)
                    : roleDAO.getRole(DefaultRole.STUDENT);

        setParticipation(user, collection, role);

    }

    @Override
    public void createParticipation(User user, ReserveCollection collection, Role role) throws CommitException, DeleteException {

        setParticipation(user, collection, role);
        updateCollectionToSolr(collection);
    }

    /**
     * Ends current {@link Participation} for target user on given collection if there is one and adds a new one with given
     * role.
     */
    private void setParticipation(User user, ReserveCollection collection, Role role) throws CommitException, DeleteException {

        Participation activeParticipation = participationDAO.getActiveParticipation(user, collection);
        if (activeParticipation != null) {
            endParticipation(activeParticipation);
        }
        if (role != null) {
            participationDAO.createParticipation(user.getId(), collection, role);
            securityService.createInstancePermissions(user.getUserid(), collection.getId(), ReserveCollection.class, role);
        }
    }

    @Override
    public void endParticipation(Participation participation) throws CommitException, DeleteException {

        participation.setEndDate(new Date());
        participationDAO.update(participation);

        ReserveCollection collection = participation.getReserveCollection();
        User user = userDAO.get(User.class, participation.getUserId());

        securityService.removeInstancePermissions(collection.getId(), user);
        updateCollectionToSolr(collection);
    }

    /**
     * Simply sets the end date of all participations in target collection.
     */
    private void endParticipations(ReserveCollection collection) {
        List<Participation> participations = participationDAO.getActiveParticipations(collection);
        for (Participation p : participations) {
            try {

                p.setEndDate(new Date());
                participationDAO.update(p);

            } catch (CommitException e) {
                LOG.error("could not update participation " + p, e);
            }
        }
    }

    @Override
    public boolean isParticipationEndingAllowed(Participation participation) {
        Role role = participation.getRole();
        Integer collectionID = participation.getReserveCollection().getId();
        User currentUser = securityService.getCurrentUser();
        User participationUser = userDAO.getUserById(participation.getUserId());

        // does the user wants to end the own participation
        if (currentUser.equals(participationUser)) {
            return securityService.isPermitted(ActionDefinition.DELETE_OWN_PARTICIPATION, collectionID);
        } else {

            // does the user wants to end the participation of another user
            if (role.getName().equals(DefaultRole.DOCENT.getName())) {
                return securityService.isPermitted(ActionDefinition.EDIT_DOCENT_PARTICIPATION, collectionID);
            } else if (role.getName().equals(DefaultRole.ASSISTANT.getName())) {
                return securityService.isPermitted(ActionDefinition.EDIT_ASSISTANT_PARTICIPATION, collectionID);
            } else if (role.getName().equals(DefaultRole.STUDENT.getName())
                    || !role.getIsDefault()) {
                return securityService.isPermitted(ActionDefinition.EDIT_STUDENT_PARTICIPATION, collectionID);
            }
        }

        return false;
    }

    @Override
    public Map<Calendar, String> getCollectionExpiryDates() throws ConfigurationException {
        Map<Calendar, String> result = new LinkedHashMap<>();

        DateTime now = DateTime.now();
        DateTime summerSettableFrom = getDateTime(CONFIG_VALID_SETTABLE_FROM_SUMMER);
        DateTime winterSettableFrom = getDateTime(CONFIG_VALID_SETTABLE_FROM_WINTER);

        DateTime noExpiry = getDateTime(CONFIG_NO_SEMESTER_END);
        String expiryLectureKey = null;
        String expirySemesterKey = null;

        if (now.isAfter(summerSettableFrom)) {
            expiryLectureKey = CONFIG_LECTURE_END_SUMMER;
            expirySemesterKey = CONFIG_SEMESTER_END_SUMMER;
        }
        if (now.isAfter(winterSettableFrom)) {

            expiryLectureKey = CONFIG_LECTURE_END_WINTER;
            expirySemesterKey = CONFIG_SEMESTER_END_WINTER;
        }

        if (expiryLectureKey != null)
            result.put(getDateTime(expiryLectureKey).toGregorianCalendar(), expiryLectureKey);
        if (expirySemesterKey != null)
            result.put(getDateTime(expirySemesterKey).toGregorianCalendar(), expirySemesterKey);
        result.put(noExpiry.toGregorianCalendar(), CONFIG_NO_SEMESTER_END);

        return result;
    }

    @Override
    public Map<Calendar, String> getCollectionProlongDates() throws ConfigurationException {

        String prolongDatesSetting = config.getString(CONFIG_PROLONG_DATES);
        List<DateTime> prolongDates = Collections.EMPTY_LIST;
        if (StringUtils.isNotBlank(prolongDatesSetting)) {
            Pattern pattern = Pattern.compile(PROLONG_DATES_SEPARATOR);
            prolongDates = pattern.splitAsStream(prolongDatesSetting)
                    .map(date -> DateTime.parse(date, DateTimeFormat.forPattern("dd.MM.yyyy")))
                    .collect(Collectors.toList());
        }

        List<Optional<Pair<DateTime, String>>> endDates = Stream.of(CONFIG_LECTURE_END_SUMMER, CONFIG_LECTURE_END_WINTER, CONFIG_SEMESTER_END_WINTER, CONFIG_SEMESTER_END_SUMMER)
                .map(key -> getEndDate(0, new DateTime(0), key))
                .filter(p -> p.isPresent())
                .collect(Collectors.toList());


        Map<Calendar, String> result = new LinkedHashMap<>();
        for (DateTime prolongDate : prolongDates) {
            Optional<Pair<DateTime, String>> endDate = findEndDate(prolongDate, endDates);
            if (endDate.isPresent()) {
                Pair<DateTime, String> pair = endDate.get();
                result.put(pair.getLeft().toGregorianCalendar(), pair.getRight());
            } else {
                result.put(prolongDate.toGregorianCalendar(), StringUtils.EMPTY);
            }
        }
        return result;
    }

    private Optional<Pair<DateTime, String>> findEndDate(DateTime dateTime, List<Optional<Pair<DateTime, String>>> dates) {
        return dates.stream()
                .filter(o -> o.isPresent())
                .map(o -> o.get())
                .filter(p -> p.getLeft().equals(dateTime))
                .findAny();
    }

    @Override
    public Calendar getLectureEnd() throws ConfigurationException {
        return getEndDate(CONFIG_LECTURE_END_SUMMER, CONFIG_LECTURE_END_WINTER);
    }

    @Override
    public Calendar getSemesterEnd() throws ConfigurationException {
        return getEndDate(CONFIG_SEMESTER_END_SUMMER, CONFIG_SEMESTER_END_WINTER);
    }

    private Calendar getEndDate(String endDateSummerConfigKey, String endDateWinterConfigKey) throws ConfigurationException {
        Map<Calendar, String> expiryDates = getCollectionExpiryDates();
        Optional<Calendar> endDate = expiryDates.entrySet().stream()
                .filter(entry -> entry.getValue().equals(endDateSummerConfigKey)
                        || entry.getValue().equals(endDateWinterConfigKey))
                .findFirst()
                .map(entry -> entry.getKey());
        return endDate.isPresent()
               ? endDate.get()
               : null;
    }

    @Override
    public Calendar getNextLectureEnd() throws ConfigurationException {
        Optional<Pair<DateTime, String>> endDate = getEndDate(1, DateTime.now(), CONFIG_LECTURE_END_SUMMER, CONFIG_LECTURE_END_WINTER);
        return endDate.isPresent()
               ? endDate.get().getLeft().toGregorianCalendar()
               : null;
    }

    @Override
    public Calendar getNextSemesterEnd() throws ConfigurationException {
        Optional<Pair<DateTime, String>> endDate = getEndDate(1, DateTime.now(), CONFIG_SEMESTER_END_SUMMER, CONFIG_SEMESTER_END_WINTER);
        return endDate.isPresent()
               ? endDate.get().getLeft().toGregorianCalendar()
               : null;
    }

    /**
     * Returns the n-th {@link DateTime} and its configuration key that is after given base, where n is the given index.
     *
     * @param index      n-th time found after given base
     * @param base       start date for calculation
     * @param configKeys config keys that point to dates that are checked for result
     * @return optional pair of datetime and its according config key that were calculated or null if no value could be found
     * @see #getDateTime(String)
     */
    private Optional<Pair<DateTime, String>> getEndDate(int index, DateTime base, String... configKeys) {
        return Arrays.stream(configKeys)
                .map(key -> {
                    try {
                        Pair<DateTime, String> endDate = Pair.of(getDateTime(key), key);
                        return endDate;
                    } catch (ConfigurationException e) {
                        LOG.error("invalid configured date time of key '" + key + "'");
                        return null;
                    }
                })
                .filter(p -> p != null)
                .filter(p -> p.getLeft().isAfter(base))
                .sorted((p1, p2) -> p1.getLeft().compareTo(p2.getLeft()))
                .skip(index)
                .findFirst();
    }

    @Override
    public List<String> getDocents(ReserveCollection collection) {

        Role role = roleDAO.getRole(DefaultRole.DOCENT);
        return participationDAO.getActiveParticipations(role, collection).stream()
                .map(participation -> {
                    User user = userDAO.getUserById(participation.getUserId());
                    return user != null
                           ? user.getRealname()
                           : null;
                })
                .filter(realname -> !StringUtils.isEmpty(realname))
                .collect(Collectors.toList());
    }

    @Override
    public ReserveCollectionNumber buildNumber(LibraryLocation location) throws CommitException {
        NumberGeneratorStrategy strategy = new InsertNumberGenerator(numberDAO);

        ReserveCollectionNumber number = strategy.buildNumber(location);
        numberDAO.createOrUpdate(number);
        return number;
    }

    @Override
    public boolean isCollectionExpiring(ReserveCollection collection) {
        int daysUntilFirstWarning = config.getInt(CONFIG_DAYS_TO_FIRST_WARNING);
        int daysUntilSecondWarning = config.getInt(CONFIG_DAYS_TO_SECOND_WARNING);

        LocalDate now = LocalDate.now();
        LocalDate validTo = LocalDate.fromDateFields(collection.getValidTo());

        return now.isEqual(validTo.minusDays(daysUntilFirstWarning))
                || now.isAfter(validTo.minusDays(daysUntilFirstWarning))
                || now.isEqual(validTo.minusDays(daysUntilSecondWarning))
                || now.isAfter(validTo.minusDays(daysUntilSecondWarning));
    }

    @Override
    public boolean isCollectionProlongable(ReserveCollection collection) {
        return collection.getProlongUsed() == null
                && collection.isActive();
    }

    @Override
    public String getActivationLink(final ReserveCollection collection) {
        return collection.getLibraryLocation().isPhysical()
               ? String.format("%s/collection/activate/%d", urlService.getApplicationURL(), collection.getId())
               : urlService.getViewCollectionURL(collection);
    }

    @Override
    public void setNumber(final Integer number, final ReserveCollection collection) throws CommitException {

        ReserveCollectionNumber collectionNumber = numberDAO.getNumber(number);
        if (collectionNumber == null) {
            collectionNumber = numberDAO.create(number);
        }
        collection.setNumber(collectionNumber);
        update(collection);
    }

    private DateTime getDateTime(String key) throws ConfigurationException {
        Setting sett = config.getSetting(key);
        DateTimeFormatter format = DateTimeFormat.forPattern(sett.getFormat());
        if (format == null)
            throw new ConfigurationException("invalid format for configuration key " + key);

        return format.parseDateTime(sett.getValue());
    }

    private void rebuildSearchIndex() {
        try {
            solrService.fullImport(SolrService.Core.ReserveCollection);
        } catch (SolrServerException | IOException e) {
            LOG.error("could not perform full import", e);
        }
    }

    private void updateCollectionToSolr(ReserveCollection reserveCollection) {
        deleteCollectionFromSolr(reserveCollection);
        commitCollectionToSolr(reserveCollection);
    }

    private void commitCollectionToSolr(ReserveCollection reserveCollection) {

        SolrCollectionView view = createSolrCollectionView(reserveCollection);

        solrService.addBean(view, SolrService.Core.ReserveCollection);
    }

    private void deleteCollectionFromSolr(ReserveCollection reserveCollection) {

        solrService.deleteByID(reserveCollection, SolrService.Core.ReserveCollection);
    }

    private SolrCollectionView createSolrCollectionView(ReserveCollection reserveCollection) {

        List<Participation> participations = participationDAO.getActiveParticipations(roleDAO.getRole(DefaultRole.DOCENT), reserveCollection);
        List<String> docentNames = participations.stream()
                .map(participation -> userDAO.getUserById(participation.getUserId()))
                .filter(user -> user != null)
                .map(user -> user.getRealname())
                .distinct()
                .collect(Collectors.toList());
        SolrCollectionView view = new SolrCollectionView();
        view.setCollectionID(reserveCollection.getId().toString());
        view.setTitle(reserveCollection.getTitle());
        view.setStatus(reserveCollection.getStatus().getDatabaseValue().toString());
        view.setCollectionNumber(reserveCollection.getNumber().getNumber().toString());
        view.setCollectionNumberNumeric(reserveCollection.getNumber().getNumber());
        view.setLocation(reserveCollection.getLibraryLocation().getName());
        view.setLocationID(reserveCollection.getLibraryLocation().getId());
        view.setValidTo(reserveCollection.getValidTo());
        view.setCollectionComment(reserveCollection.getComment());
        view.setAuthors(docentNames);

        return view;
    }

    private void sendCollectionMail(ReserveCollection collection) {

        User currentUser = securityService.getCurrentUser();
        Optional<Integer> authorID = collection.getParticipations().stream().map(p -> p.getUserId()).findFirst();

        VelocityContext context = new VelocityContext();
        context.put("collectionListURL", urlService.getApplicationURL());
        context.put("collection", collection);
        context.put("collectionLink", urlService.getViewCollectionURL(collection));
        context.put("authors", mailService.buildAuthors(collection));
        context.put("origin", mailService.buildOrigin(collection));

        if (currentUser != null) {
            context.put("user", currentUser);
            context.put("userOrigin", mailService.buildOrigin(currentUser));
            context.put("roles", roleDAO.getRoles(currentUser));
        }
        if (authorID.isPresent())
            context.put("editUserLink", urlService.getEditUserLink(userDAO.getUserById(authorID.get())));

        List<OrderMailRecipient> recipients = mailRecipientDAO.getRecipients(collection.getLibraryLocation(), ReserveCollection.class);
        String from = config.getString("mail.from");
        String subject;
        switch (collection.getStatus()) {
            case NEW:
                subject = String.format(messages.get("new.collection"), collection.getLibraryLocation().getName());
                break;
            case DEACTIVATED:
                subject = String.format(messages.get("deactivated.collection"), collection.getLibraryLocation()
                        .getName());
                break;
            default:
                subject = String.format(messages.get("updated.collection"), collection.getLibraryLocation().getName());
                break;
        }

        try {
            Mail mail = mailService.builder("/vt/mail.collection.vm")
                    .from(from)
                    .subject(subject)
                    .context(context)
                    .addRecipients(recipients.stream().map(OrderMailRecipient::getMail).toArray(String[]::new))
                    .create();
            mailService.sendMail(mail);
        } catch (CommitException e) {
            LOG.error("could not save mail", e);
        } catch (EmailException e) {
            LOG.error("could not send mail", e);
        } catch (IOException e) {
            LOG.error("could not create mail", e);
        }
    }

    private void createDefaultEntry(ReserveCollection collection) {

        // create template
        try (StringWriter writer = new StringWriter()) {

            VelocityContext velocityContext = new VelocityContext();
            Template template = Velocity.getTemplate("/vt/default.entry.vm", "UTF-8");
            template.merge(velocityContext, writer);

            Html defaultEntry = new Html();
            defaultEntry.setText(writer.toString());
            entryDAO.createEntry(defaultEntry, collection);
        } catch (IOException | CommitException e) {
            LOG.error("could not add default entry to collection " + collection.getId(), e);
        }
    }
}
