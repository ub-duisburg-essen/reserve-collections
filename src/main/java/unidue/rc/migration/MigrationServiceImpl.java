/**
 * Copyright (C) 2014 - 2016 Universitaet Duisburg-Essen (semapp|uni-due.de)
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
package unidue.rc.migration;


/**
 * The MigrationServiceImpl class migrates the Reserve-collections, Slots and Meta-Data into the new format and commits the
 * converted data into the database.
 *
 * @author Marcus Koesters

 */

import miless.model.*;
import org.apache.cayenne.BaseContext;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.DataRow;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.SQLTemplate;
import org.apache.cayenne.query.SelectQuery;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.mail.EmailException;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.velocity.VelocityContext;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import unidue.rc.dao.*;
import unidue.rc.migration.legacymodel.*;
import unidue.rc.model.*;
import unidue.rc.search.SolrService;
import unidue.rc.security.CollectionSecurityService;
import unidue.rc.system.BaseURLService;
import unidue.rc.system.MailService;
import unidue.rc.system.SystemConfigurationService;
import unidue.rc.system.SystemMessageService;
import unidue.rc.workflow.*;

import java.io.*;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class MigrationServiceImpl implements MigrationService, MigrationVisitor {

    private static final ResponseHandler<String> DEFAULT_RESPONSE_HANDLER = response -> {
        HttpEntity entity = response.getEntity();
        return entity != null
                ? EntityUtils.toString(entity, "UTF-8")
                : null;
    };

    private static final Logger LOG = Logger.getLogger(MigrationServiceImpl.class.getName());

    private static final String FILE_PATH_DIVIDER = "<>";

    private static final String[] MIGRATION_ADMINS = new String[]{
            "katrin.falkenstein-feldhoff@uni-due.de",
            "sonja.hendriks@uni-due.de",
            "nils.verheyen@uni-due.de"
    };

    @Inject
    private SystemConfigurationService config;

    @Inject
    private SystemMessageService messages;

    @Inject
    private CollectionSecurityService securityService;

    @Inject
    private CollectionService collectionService;

    @Inject
    private SolrService searchService;

    @Inject
    private BaseURLService urlService;

    @Inject
    private LegacyXMLService legacyXMLService;

    @Inject
    private ScanJobService scanJobService;

    @Inject
    private BookJobService bookJobService;

    @Inject
    private MailService mailService;

    @Inject
    private UserDAO userDAO;

    @Inject
    private RoleDAO roleDAO;

    @Inject
    private ParticipationDAO participationDAO;

    @Inject
    private PermissionDAO permissionDAO;

    @Inject
    private MigrationDAO migrationDAO;

    @Inject
    private ReserveCollectionDAO collectionDAO;

    @Inject
    private ReserveCollectionNumberDAO numberDAO;

    private String slotURL;
    private String duepublicoDocumentURL;

    private ObjectContext context;

    private File oldFileStoragePath;
    private File oldVideoStoragePath;
    private File oldSecureVideoStoragePath;
    private File newFileStoragePath;

    private File documentMetadataPath;

    public ReserveCollection migrateReserveCollection(Migration migration, String migrationCode) throws MigrationException, ConfigurationException {

        validate(migration, migrationCode);
        configure();
        context = BaseContext.getThreadObjectContext();

        CollectionImportData sourceData = initSourceData(migration);

        ReserveCollection collection = migrateCollection(sourceData);

        migration.setFinished(true);
        migration.setCodeUsedDate(new Date());
        migration.setReserveCollection(collection);
        try {
            migrationDAO.update(migration);
        } catch (CommitException e) {
            throw new MigrationException("could not update migration of document " + migration.getDocumentID() + " in db", e);
        }

        legacyXMLService.setMoved(sourceData.sourceXML, urlService.getViewCollectionURL(collection));

        sendMigrationSuccessMail(sourceData, collection);

        Arrays.stream(SolrService.Core.values())
                .forEach(core -> {
                    try {
                        searchService.fullImport(core);
                    } catch (SolrServerException | IOException e) {
                        LOG.warn("could not run full import of core " + core.name(), e);
                    }
                });
        return collection;
    }

    private CollectionImportData initSourceData(Migration migration) throws MigrationException {

        String documentID = migration.getDocumentID().toString();

        CollectionImportData sourceData = new CollectionImportData();

        try {
            loadSourceCollection(documentID, sourceData);

            loadSlotData(documentID, sourceData);
        } catch (IOException e) {
            throw new MigrationException("could not read document " + documentID, e);
        }
        return sourceData;
    }

    private void validate(Migration migration, String migrationCode) throws MigrationException {
        Integer documentID = migration.getDocumentID();
        if (documentID == null)
            throw new MigrationException("no document given in migration");

        if (migration.getCodeUsedDate() != null
                || migration.isFinished())
            throw new MigrationException("migration for document " + documentID + " already finished");

        if (!migration.getMigrationCode().equals(migrationCode))
            throw new MigrationException("invalid migration code for document " + documentID);
    }

    private void configure() throws ConfigurationException {

        String legacyFileStoragePath = getConfigStringOrThrow("legacy.file.storage.path");
        String legacyVideoStoragePath = getConfigStringOrThrow("legacy.video.storage.path");
        String legacySecureVideoStoragePath = getConfigStringOrThrow("legacy.secure.video.storage.path");
        String fileStore = getConfigStringOrThrow("files.store");
        String legacyDocumentMetaDataPath = getConfigStringOrThrow("legacy.document.metadata.path");
        String legacySlotURL = getConfigStringOrThrow("legacy.slot.url");
        String duepublicoDocumentURL = getConfigStringOrThrow("duepublico.document.url");

        oldFileStoragePath = getDirectoryOrThrow(legacyFileStoragePath);
        oldVideoStoragePath = getDirectoryOrThrow(legacyVideoStoragePath);
        oldSecureVideoStoragePath = getDirectoryOrThrow(legacySecureVideoStoragePath);
        newFileStoragePath = getDirectoryOrThrow(fileStore);
        documentMetadataPath = getDirectoryOrThrow(legacyDocumentMetaDataPath);
        slotURL = legacySlotURL;
        this.duepublicoDocumentURL = duepublicoDocumentURL;
    }

    private String getConfigStringOrThrow(String key) throws ConfigurationException {
        String value = config.getString(key);
        if (StringUtils.isEmpty(value))
            throw new ConfigurationException(String.format(messages.get("error.msg.invalid.setting"), key));
        return value;
    }

    private File getDirectoryOrThrow(String path) throws ConfigurationException {
        File directory = new File(path);
        if (!directory.exists() || !directory.isDirectory())
            throw new ConfigurationException("directory " + path + " does not exist");
        return directory;
    }

    /**
     * @param importData
     * @throws MigrationException
     */
    private void loadSlotData(String documentID, CollectionImportData importData) throws MigrationException, IOException {
        long time = System.currentTimeMillis();
        Serializer serializer = new Persister();

        String slotsXML;
        final HttpClient httpclient = HttpClientBuilder.create().build();
        HttpGet httpget = new HttpGet(slotURL);
        slotsXML = httpclient.execute(httpget, DEFAULT_RESPONSE_HANDLER);

        try {
            SlotsLocal slots = serializer.read(SlotsLocal.class, slotsXML, false);
            Optional<SlotLocal> slotOptional = slots.getSlots()
                    .stream()
                    .filter(slot -> documentID.equals(slot.getDocumentID()))
                    .findFirst();
            if (slotOptional.isPresent())
                importData.slot = slotOptional.get();
        } catch (Exception e) {
            throw new MigrationException("could not parse slots xml " + slotURL, e);
        }
        LOG.info("Imported Slots. Time used -> " + (System.currentTimeMillis() - time) + " ms");
    }

    /**
     * Reads recursively reserve collection xml files from target dir and builds {@link
     * ReserveCollectionLocal} objects from read files.
     */
    private void loadSourceCollection(String documentID, CollectionImportData output) throws IOException, MigrationException {

        SelectQuery query = new SelectQuery(Files.class);
        query.setQualifier(ExpressionFactory.matchExp(Files.PATH_PROPERTY, "index.msa")
                .andExp(ExpressionFactory.likeExp(Files.STORAGEID_PROPERTY, "%\\-" + documentID + "\\_%")));

        List<Files> indexFiles = context.performQuery(query);
        if (indexFiles.size() > 1)
            throw new MigrationException("multiple index.msa files found for document " + documentID);
        else if (indexFiles.isEmpty())
            throw new MigrationException("no index.msa found for document " + documentID);

        Files file = indexFiles.get(0);

        Serializer serializer = new Persister();
        File sourceXML = FileUtils.getFile(oldFileStoragePath, file.getStorageid());
        String xml = FileUtils.readFileToString(sourceXML);
        String derivateID = legacyXMLService.getDerivateID(sourceXML.getName());
        try {

            ReserveCollectionLocal collection = serializer.read(ReserveCollectionLocal.class, xml, false);
            collection.setDocID(documentID);

            output.documentID = documentID;
            output.derivateID = derivateID;
            output.collection = collection;
            output.sourceXML = sourceXML;
        } catch (Exception e) {

            throw new MigrationException("ERROR while creating reserve collection. document id = " + documentID + " " + e
                    .getMessage());
        }
    }

    /**
     * Creates a new {@link ReserveCollectionNumber} by target value, if it is necessary. If a number
     * exists with target value, the existing number is returned.
     *
     * @param value
     * @return
     */
    private ReserveCollectionNumber createNumber(Integer value) {
        ReserveCollectionNumber collectionNumber = Cayenne.objectForPK(context, ReserveCollectionNumber.class,
                value);
        if (collectionNumber == null) {
            collectionNumber = context.newObject(ReserveCollectionNumber.class);
            collectionNumber.setNumber(value);
            context.commitChanges();
        }
        return collectionNumber;
    }

    private ReserveCollection migrateCollection(CollectionImportData cid) throws MigrationException {
        context = BaseContext.getThreadObjectContext();
        ReserveCollection collection = new ReserveCollection();
        collection.setCreated(new Date());
        collection.setModified(new Date());
        try {

            // create objects needed by new reserve collection

            // retrieve library location
            LibraryLocation location = Cayenne.objectForPK(context, LibraryLocation.class, String.valueOf(cid.collection
                    .getCollectionID()).subSequence(0, 1).toString());

            // create number if necessary
            Integer numberValue = Integer.valueOf(cid.collection.getCollectionID().substring(1));
            ReserveCollectionNumber number = createNumber(numberValue);
            if (!number.isFree(location))
                number = buildNumber(location);

            context.registerNewObject(collection);
            // associated references to necessary objects
            collection.setLibraryLocation(location);
            collection.setNumber(number);

            // set status if it can be mapped
            ReserveCollectionStatus status = cid.collection.getIsActive()
                    ? ReserveCollectionStatus.ACTIVE
                    : ReserveCollectionStatus.DEACTIVATED;
            collection.setStatus(status);

            // set download allowed
            collection.setMediaDownloadAllowed(true);
            Derivates derivates = cid.derivateID.matches("[0-9]+")
                    ? collectionDAO.get(Derivates.class, Integer.valueOf(cid.derivateID))
                    : null;
            if (derivates != null) {
                String locator = derivates.getLocator();
                if (locator != null && locator.startsWith("false"))
                    collection.setMediaDownloadAllowed(false);
            }

            // set access keys
            setAccessKeys(cid.collection, collection);

            // set metadata according to slot
            if (cid.slot != null)
                setSlotData(cid.slot, collection);

            // set metadata by document
            setDocumentData(cid.documentID, collection, documentMetadataPath);

            Calendar lectureEnd = collectionService.getLectureEnd();
            Date expiryDate = collection.getValidTo();
            if (expiryDate == null
                    || (lectureEnd != null && expiryDate != null && expiryDate.before(lectureEnd.getTime()))) {
                lectureEnd.add(Calendar.WEEK_OF_YEAR, 2);
                collection.setValidTo(lectureEnd.getTime());
            } else {
                LOG.debug("keeping valid to: " + expiryDate);
            }

            // migrateEntryValue entries
            List<EntryLocal> entries = cid.collection.getEntries();
            if (entries != null && !entries.isEmpty()) {

                for (EntryLocal entry : entries) {
                    int position = entries.indexOf(entry);
                    migrateEntry(entry, position, collection, cid.derivateID);
                }

                // save collection
                context.commitChanges();
            } else {
                throw new MigrationException("collection " + cid.documentID + " has no entries");
            }

            // migrate permissions
            migratePermissions(cid.documentID, collection);

            // move all files used in collection to new store
            migrateFiles(collection);

            // create analytics

            return collection;
        } catch (Exception e) {

            context.rollbackChanges();
            LOG.error("ERROR while storing reserve collection. document id = " + cid.documentID, e);

            context.deleteObjects(collection);
            context.commitChanges();

            sendMigrationFailedMail(cid, e);

            throw new MigrationException(e);
        }
    }

    private ReserveCollectionNumber buildNumber(LibraryLocation location) throws CommitException {

        NumberGeneratorStrategy strategy = location.isPhysical()
                ? new InsertNumberGenerator(numberDAO)
                : new AppendNumberGenerator(numberDAO);

        ReserveCollectionNumber number = strategy.buildNumber(location);
        numberDAO.createOrUpdate(number);
        return number;
    }

    private void sendMigrationFailedMail(CollectionImportData cid, Throwable cause) {

        VelocityContext velocityContext = new VelocityContext();
        String templateFile = "/vt/mail.migration.vm";

        String subject = messages.get("migration.failed");
        String slotID = cid.slot != null ? cid.slot.getId() : "-";

        velocityContext.put("documentID", cid.documentID);
        velocityContext.put("slotID", slotID);
        velocityContext.put("errorMessage", cause.getMessage());
        velocityContext.put("errorStacktrace", ExceptionUtils.getStackTrace(cause));

        String from = config.getString("mail.from");
        try {
            Mail mail = mailService.builder(templateFile)
                    .from(from)
                    .subject(subject.toString())
                    .context(velocityContext)
                    .addRecipients(MIGRATION_ADMINS)
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

    private void sendMigrationSuccessMail(CollectionImportData sourceData, ReserveCollection collection) {
        VelocityContext velocityContext = new VelocityContext();
        String templateFile = "/vt/mail.migration.vm";

        String subject = messages.get("migration.finished");
        velocityContext.put("collection", collection);
        velocityContext.put("status", messages.get(collection.getStatus().name()));
        int oldNumber = sourceData.slot != null
                ? sourceData.slot.getNumber()
                : -1;
        int newNumber = collection.getNumber().getNumber();
        if (oldNumber != newNumber && oldNumber > 0) {
            velocityContext.put("oldNumber", oldNumber);
            velocityContext.put("newNumber", newNumber);
        }
        List<Participation> docentParticipations = participationDAO.getActiveParticipations(roleDAO.getRole(DefaultRole.DOCENT), collection);
        String[] docentMails = docentParticipations.stream()
                .map(participation -> userDAO.getUserById(participation.getUserId()))
                .filter(user -> user != null)
                .map(user -> user.getEmail())
                .filter(mail -> mail != null)
                .toArray(String[]::new);

        String from = config.getString("mail.from");
        try {
            Mail mail = mailService.builder(templateFile)
                    .from(from)
                    .subject(subject.toString())
                    .context(velocityContext)
                    .addRecipients(docentMails)
                    .addCc(MIGRATION_ADMINS)
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

    private void migratePermissions(String documentID, ReserveCollection collection) throws MigrationException {


        SelectQuery query = new SelectQuery(Permissions.class);
        query.setQualifier(ExpressionFactory.matchExp(Permissions.TID_PROPERTY, documentID)
                .andExp(ExpressionFactory.matchExp(Permissions.TTYPE_PROPERTY, 'D'))
                .andExp(ExpressionFactory.matchExp(Permissions.STYPE_PROPERTY, 'U')));

        List<Permissions> permissions = context.performQuery(query);

        List<Permissions> writePermissions = permissions.stream()
                .filter(p -> p.getAction().equalsIgnoreCase("W"))
                .collect(Collectors.toList());
        /*
        1. if a user object can be found to the legal entity that is used inside the old collection
        use it as docent. all other users that have write permission to the collection become assistants
        2. if no user could be found to legal entities all users that have write permission will get docent
        access rights.
         */
        List<Participation> docentParticipations = collection.getDocentParticipations();
        if (docentParticipations.isEmpty()) {

            Role docentRole = roleDAO.getRole(DefaultRole.DOCENT);
            for (Permissions p : writePermissions) {
                createParticipation(p.getSid(), collection, docentRole);
            }
        } else {
            Role assistantRole = roleDAO.getRole(DefaultRole.ASSISTANT);
            // collect all write permission that are not already in docent participations
            List<Permissions> filteredWritePermissions = writePermissions.stream()
                    .filter(p -> docentParticipations.stream()
                            .filter(dp -> !dp.getUserId().equals(p.getSid()))
                            .findAny()
                            .isPresent())
                    .collect(Collectors.toList());
            for (Permissions p : filteredWritePermissions) {
                createParticipation(p.getSid(), collection, assistantRole);
            }
        }

        Role studentRole = roleDAO.getRole(DefaultRole.STUDENT);
        List<Permissions> readPermissions = permissions.stream()
                .filter(p -> p.getAction().equalsIgnoreCase("R"))
                .collect(Collectors.toList());
        for (Permissions p : readPermissions) {
            createParticipation(p.getSid(), collection, studentRole);
        }
    }

    private void createParticipation(Integer userID, ReserveCollection collection, Role role) throws MigrationException {

        User user = userDAO.getUserById(userID);
        if (user == null)
            throw new MigrationException("user with id " + userID + " not found");

        try {
            /*
            similar to collectionservice#createParticipation. collection service
            is not used because for each participation the search index is updated.
             */
            participationDAO.createParticipation(user.getId(), collection, role);
            securityService.createInstancePermissions(userID, collection.getId(), ReserveCollection.class, role);
        } catch (CommitException e) {
            throw new MigrationException("could not create participation for user " + user.getId() +
                    " on collection " + collection.getId() + " with role " + role.getId(), e);
        }
    }

    private void migrateFiles(ReserveCollection collection) throws MigrationException {
        SQLTemplate sql = new SQLTemplate(Resource.class, String.format(
                "select r.id resourceid, r.filePath filepath, jae.id journalarticleid, bce.id bookchapterid, " +
                        "fe.id fileId" +
                        " from RESOURCE r" +
                        " left join JOURNAL_ARTICLE ja on ja.resourceID = r.id" +
                        " left join BOOK_CHAPTER bc on bc.resourceID = r.id" +
                        " left join FILE f on f.resourceID = r.id" +
                        " left join ENTRY jae on ja.id = jae.id" +
                        " left join ENTRY bce on bc.id = bce.id" +
                        " left join ENTRY fe on f.id = fe.id" +
                        " left join RESERVE_COLLECTION jac on jae.reserveCollectionId = jac.id" +
                        " left join RESERVE_COLLECTION bcc on bce.reserveCollectionId = bcc.id" +
                        " left join RESERVE_COLLECTION fc on fe.reserveCollectionId = fc.id" +
                        " WHERE (jac.id = %1$d OR bcc.id = %1$d OR fc.id = %1$d) and r.filepath is not null",
                collection.getId()));
        sql.setFetchingDataRows(true);

        List<DataRow> rows = context.performQuery(sql);
        for (DataRow row : rows) {
            Integer resourceId = (Integer) row.get("resourceid");
            String filePath = (String) row.get("filepath");
            Integer journalArticleId = (Integer) row.get("journalarticleid");
            Integer bookChapterId = (Integer) row.get("bookchapterid");
            Integer fileId = (Integer) row.get("fileid");
            Integer entryID = journalArticleId != null
                    ? journalArticleId
                    : bookChapterId != null
                            ? bookChapterId
                            : fileId;

            String[] filePathValues = filePath.split(FILE_PATH_DIVIDER);
            File oldFile = new File(filePathValues[0]);//new File(oldFileStoragePath, filePath);
            File newFile = FileUtils.getFile(newFileStoragePath, collection.getId().toString(), resourceId.toString(),
                    filePathValues[1]);

            try {
                FileUtils.copyFile(oldFile, newFile);
            } catch (IOException e) {
                throw new MigrationException("could not copy file " + oldFile.getAbsolutePath() + " to " + newFile
                        .getAbsolutePath(), e);
            }


            Resource resource = Cayenne.objectForPK(context, Resource.class, resourceId);

            String newPath = collection.getId().toString() +
                    File.separator + resource.getId().toString() +
                    File.separator + filePathValues[1];
            resource.setFilePath(newPath);
            resource.setMimeType(detectMimeType(newFile));
            context.commitChanges();
        }
    }

    private void migrateEntry(EntryLocal entrylocal, int position, ReserveCollection collection, String derivateID) throws MigrationException {
        Entry entry = context.newObject(Entry.class);
        entry.setReserveCollection(collection);
        entry.setPosition(position);
        entry.setCreated(entrylocal.getCreated());
        entry.setModified(entrylocal.getModified());

        List<DateTimeLocal> datelocs = entrylocal.getDates();
        if (datelocs != null) {
            DateTimeFormatter format = DateTimeFormat.forPattern("DD.MM.YYYY HH:mm:ss");

            for (DateTimeLocal timeloc : datelocs) {
                DateTime d = DateTime.parse(timeloc.getValue(), format);
                if ("modified".equals(timeloc.getType())) {
                    entry.setModified(d.toDate());
                }
                if ("created".equals(timeloc.getType())) {
                    entry.setCreated(d.toDate());
                }
            }
        }
        // save entry
        context.commitChanges();

        try {

            entrylocal.migrateEntryValue(this, collection, entry, entrylocal, derivateID);
        } catch (MigrationException e) {
            throw new MigrationException("could not migrate entry " + entrylocal.getEntryID(), e);
        }

    }

    private void setDocumentData(String docID, ReserveCollection collection, File documentMetadataPath) throws
            MigrationException {

        Serializer serializer = new Persister(new DefModsVisitor());
        String subpath = legacyXMLService.buildMetaDataFilePath(docID);
        File collectionXML = new File(documentMetadataPath, subpath);
        ReserveCollectionMetaDataLocal metaLocal;
        try {
            metaLocal = serializer.read(ReserveCollectionMetaDataLocal.class,
                    collectionXML, false);
        } catch (Exception e) {
            throw new MigrationException("could not parse document meta data from document " + docID, e);
        }

        // title
        collection.setTitle(buildTitle(metaLocal.getContainer()));

        // origin
        if (collection.getOriginId() == null) {
            String originID = metaLocal.getContainer().getOriginID();
            SelectQuery query = new SelectQuery(MCRCategory.class);
            query.setQualifier(ExpressionFactory.matchExp(MCRCategory.CATEGID_PROPERTY, originID).andExp(ExpressionFactory.matchExp(MCRCategory.CLASSID_PROPERTY, "ORIGIN")));
            MCRCategory origin = (MCRCategory) Cayenne.objectForQuery(context, query);
            if (origin == null)
                throw new MigrationException("could not get origin of collection " + docID);
            collection.setOriginId(origin.getInternalId());
        }

        // authors
        Role docentRole = roleDAO.getRole(DefaultRole.DOCENT);
        List<Integer> teacherIDs = metaLocal.getContainer().getTeacherIDs();
        for (Integer teacherID : teacherIDs) {
            List<User> users = userDAO.getUsersByLegalEntityId(teacherID);
            for (User user : users) {
                createParticipation(user.getId(), collection, docentRole);
            }
        }
    }

    private String buildTitle(DefModsContainer container) {
        StringBuilder titleBuilder = new StringBuilder();

        String nonSort = container.getNonSort();
        if (!StringUtils.isEmpty(nonSort)) {
            titleBuilder.append(nonSort);
            titleBuilder.append(" ");
        }

        titleBuilder.append(container.getTitle());

        String subTitle = container.getSubTitle();
        if (!StringUtils.isEmpty(subTitle)) {
            titleBuilder.append(": ");
            titleBuilder.append(subTitle);
        }
        return titleBuilder.toString();
    }

    private void setSlotData(SlotLocal slot, ReserveCollection collection) throws MigrationException {
        AlephLocal aleph = slot.getAleph();
        if (aleph != null) {
            collection.setAlephSystemId(aleph.getAlephsystemid());
            collection.setAlephUserId(aleph.getAlephuserid());
            collection.setComment(slot.getComment());

        }
        if (slot.getValidTo() != null) {
            DateTimeFormatter format = DateTimeFormat.forPattern("dd.MM.yyyy");
            DateTime d = DateTime.parse(slot.getValidTo(), format);
            collection.setValidTo(d.toDate());
        }
        String slotStatus = slot.getStatus();
        if (slotStatus != null) {
            switch (slotStatus) {
                case "active":
                    collection.setStatus(ReserveCollectionStatus.ACTIVE);
                    break;
                case "new":
                    collection.setStatus(ReserveCollectionStatus.NEW);
                    break;
                case "reserved":
                    // no status for collection but number is reserved
                    break;
                default:
                    collection.setStatus(ReserveCollectionStatus.DEACTIVATED);
                    break;
            }
        }
    }

    private void setAccessKeys(ReserveCollectionLocal source, ReserveCollection collection) {

        Accesskeys key = Cayenne.objectForPK(context, Accesskeys.class, source.getDocID());
        if (key != null) {
            collection.setReadKey(key.getReadkey());
            collection.setWriteKey(key.getWritekey());
        }
    }

    private Resource createResource(ResourceValue value, EntryLocal entryLocal, String derivateID) throws MigrationException {

        Resource resource = context.newObject(Resource.class);
        if (value.getPath() != null) {
            SelectQuery fileQuery = new SelectQuery(Files.class);
            String dbPath = entryLocal.getEntryID() + File.separator + value.getPath();
            fileQuery.setQualifier(ExpressionFactory.matchExp(Files.PATH_PROPERTY, dbPath).andExp(ExpressionFactory
                    .matchExp(Files.ID_PROPERTY, derivateID)));
            Files file = (Files) Cayenne.objectForQuery(context, fileQuery);

            if (file == null)
                throw new MigrationException("could not find file " + value.getPath() + " of entry " + entryLocal.getEntryID());

            File basePath;
            switch (file.getStoreid()) {
                case "REAL8":
                    basePath = oldVideoStoragePath;
                    break;
                case "SECURE":
                    basePath = oldSecureVideoStoragePath;
                    break;
                case "FS":
                    basePath = oldFileStoragePath;
                    break;
                default:
                    throw new MigrationException("invalid store id for file " + file.getId());
            }

            // join together real path (storageid) with user friendly file name
            String path = FileUtils.getFile(basePath, file.getStorageid()) + FILE_PATH_DIVIDER + value.getPath();
            resource.setFilePath(path);
        }
        resource.setFullTextURL(value.getUrl());
        resource.setCopyrightReviewStatus(CopyrightReviewMapping.get(value.getReviewStatus(),
                CopyrightReviewStatus.NOT_REVIEWED));

        return resource;
    }

    @Override
    public void migrate(ArticleLocal value, ReserveCollection collection, Entry entry, EntryLocal entryLocal,
                        String derivateID) throws MigrationException {

        JournalArticle article = context.newObject(JournalArticle.class);
        article.setEntry(entry);
        article.setArticleTitle(value.getTitle());
        article.setAuthors(value.getAuthor());
        article.setJournalTitle(value.getJournalTitle());
        article.setIssn(value.getJournalIssn());
        article.setIssue(value.getIssue());
        article.setPageStart(value.getPageFrom());
        article.setPageEnd(value.getPageTo());
        article.setPublisher(value.getJournalPublisher());
        article.setVolume(value.getVolume());
        article.setPlaceOfPublication(value.getJournalPlace());
        article.setComment(value.getComment());
        article.setSignature(value.getSignature());
        article.setReferenceNumber(value.getRefNo());
        context.commitChanges();

        Resource resource = createResource(value, entryLocal, derivateID);
        article.setResource(resource);

        if (scanJobService.isScanJobNeeded(article))
            createScanJob(article);

    }

    @Override
    public void migrate(BookLocal value, ReserveCollection collection, Entry entry, EntryLocal entryLocal,
                        String derivateID) throws MigrationException {

        Resource resource = null;
        if (value.getUrl() != null)
            resource = createResource(value, entryLocal, derivateID);

        String year = value.getYear();
        if (!value.isNonLendingCollection()) {

            Reference reference = context.newObject(Reference.class);

            reference.setEntry(entry);
            reference.setAuthors(value.getAuthor());
            reference.setIsbn(legacyXMLService.normalizeISBN(value.getIsbn()));
            reference.setPlaceOfPublication(value.getPlace());
            reference.setPublisher(value.getPublisher());
            reference.setTitle(value.getTitle());
            reference.setSignature(value.getSignature());
            if (NumberUtils.isDigits(year))
                reference.setYearOfPublication(Integer.valueOf(year));
            reference.setComment(value.getComment());
            reference.setEdition(value.getEdition());
            reference.setCollectionNumber(value.getCollectionRef());
            reference.setVolume(value.getVolume());

            reference.setResource(resource);
        } else {

            Book book = context.newObject(Book.class);
            book.setEntry(entry);
            book.setAuthors(value.getAuthor());
            book.setIsbn(legacyXMLService.normalizeISBN(value.getIsbn()));
            book.setPlaceOfPublication(value.getPlace());
            book.setPublisher(value.getPublisher());
            book.setTitle(value.getTitle());
            book.setSignature(value.getSignature());
            if (NumberUtils.isDigits(year))
                book.setYearOfPublication(Integer.valueOf(year));
            book.setComment(value.getComment());
            book.setEdition(value.getEdition());
            book.setCollectionNumber(value.getCollectionRef());
            book.setVolume(value.getVolume());
            book.setBookingStatus(BookingStatus.IS_BOOKED);
            book.setResource(resource);

            if (bookJobService.isBookJobNeeded(book))
                createBookJob(book);
        }
    }

    @Override
    public void migrate(ChapterLocal value, ReserveCollection collection, Entry entry, EntryLocal entryLocal,
                        String derivateID) throws MigrationException {

        BookChapter chapter = context.newObject(BookChapter.class);
        BookLocal book = value.getBook();

        chapter.setEntry(entry);
        chapter.setBookAuthors(book.getAuthor());
        chapter.setBookTitle(book.getTitle());
        chapter.setChapterAuthors(value.getAuthor());
        chapter.setChapterTitle(value.getTitle());
        chapter.setIsbn(legacyXMLService.normalizeISBN(book.getIsbn()));
        chapter.setPageEnd(value.getPageTo());
        chapter.setPageStart(value.getPageFrom());
        chapter.setPlaceOfPublication(book.getPlace());
        chapter.setPublisher(book.getPublisher());
        chapter.setSignature(book.getSignature());

        String year = book.getYear();
        if (NumberUtils.isDigits(year))
            chapter.setYearOfPublication(Integer.valueOf(year));

        chapter.setComment(value.getComment());
        chapter.setEdition(book.getEdition());

        Resource resource = createResource(value, entryLocal, derivateID);
        chapter.setResource(resource);

        if (scanJobService.isScanJobNeeded(chapter))
            createScanJob(chapter);
    }

    @Override
    public void migrate(FileLocal value, ReserveCollection collection, Entry entry, EntryLocal entryLocal,
                        String derivateID) throws MigrationException {

        unidue.rc.model.File rcfile = context.newObject(unidue.rc.model.File.class);
        rcfile.setEntry(entry);
        rcfile.setDescription(value.getLabel());

        Resource resource = createResource(value, entryLocal, derivateID);
        rcfile.setResource(resource);
    }

    @Override
    public void migrateHeadline(String headlineValue, ReserveCollection collection, Entry entry,
                                EntryLocal entryLocal, String derivateID) {

            Headline headline = context.newObject(Headline.class);
        headline.setText(headlineValue);
        headline.setEntry(entry);
    }

    @Override
    public void migrateHtml(String value, ReserveCollection collection, Entry entry, EntryLocal entryLocal,
                            String derivateID){

        Html html = context.newObject(Html.class);
        html.setText(value);
        html.setEntry(entry);
    }

    @Override
    public void migrateText(FreeTextLocal text, ReserveCollection collection, Entry entry, EntryLocal entryLocal,
                            String derivateID) {

        String value = "preformatted".equals(text.getFormat()) ? "<pre>" + text.getText() + "</pre>" : text.getText();
        migrateHtml(value, collection, entry, entryLocal, derivateID);
    }

    @Override
    public void migrate(WeblinkLocal value, ReserveCollection collection, Entry entry, EntryLocal entryLocal,
                        String derivateID) {

        WebLink link = context.newObject(WebLink.class);
        link.setEntry(entry);
        link.setUrl(value.getUrl());
        link.setName(value.getLabel());
    }

    @Override
    public void migrate(DocumentLinkLocal documentLink, ReserveCollection collection, Entry entry, EntryLocal
            entryLocal, String derivateID) {

        WebLink link = context.newObject(WebLink.class);
        link.setEntry(entry);
        link.setUrl(String.format(duepublicoDocumentURL, documentLink.getDocumentID()));
        link.setName(documentLink.getComment());
    }

    private String detectMimeType(File file) {
        try (InputStream is = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(is)) {

            AutoDetectParser parser = new AutoDetectParser();
            Detector detector = parser.getDetector();
            Metadata md = new Metadata();
            md.add(Metadata.RESOURCE_NAME_KEY, file.getName());
            MediaType mediaType = detector.detect(bis, md);
            String mimetype = mediaType.toString();
            LOG.debug("detected mime type \"" + mimetype + "\" from file " + file);
            return mimetype;
        } catch (IOException e) {
            LOG.info("could not detect mime type of file " + file);
            return null;
        }
    }

    private void createBookJob(Book book) throws MigrationException {

        BookJob job = context.newObject(BookJob.class);
        job.setStatus(BookJobStatus.NEW);
        job.setModified(new Date());
        job.setBook(book);
    }

    private void createScanJob(Scannable scannable) throws MigrationException {

        ScanJob job = context.newObject(ScanJob.class);
        job.setStatus(ScanJobStatus.NEW);
        job.setScannable(scannable);
    }

    private static class CollectionImportData extends HashSet<CollectionImportData> {

        SlotLocal slot;
        String documentID;
        String derivateID;
        ReserveCollectionLocal collection;
        File sourceXML;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;

            CollectionImportData that = (CollectionImportData) o;

            return documentID.equals(that.documentID);

        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + documentID.hashCode();
            return result;
        }
    }
}
