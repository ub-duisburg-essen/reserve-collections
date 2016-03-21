package unidue.rc.system;

/*
 * #%L
 * Semesterapparate
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 Universitaet Duisburg Essen
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

import org.apache.cayenne.BaseContext;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.apache.cayenne.di.Binder;
import org.apache.cayenne.di.DIBootstrap;
import org.apache.cayenne.di.Injector;
import org.apache.cayenne.di.Module;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.SelectQuery;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.DatabaseConfiguration;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.TriggerFiredBundle;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unidue.rc.auth.*;
import unidue.rc.dao.*;
import unidue.rc.io.CollectionRSSWriter;
import unidue.rc.io.CollectionRSSWriterImpl;
import unidue.rc.migration.*;
import unidue.rc.model.*;
import unidue.rc.model.config.DefaultSetting;
import unidue.rc.model.config.DefaultSettingsList;
import unidue.rc.plugins.alephsync.AlephSynchronizer;
import unidue.rc.plugins.alephsync.AlephSynchronizerImpl;
import unidue.rc.plugins.moodle.services.MoodleRequestHandler;
import unidue.rc.plugins.moodle.services.MoodleRequestHandlerImpl;
import unidue.rc.plugins.moodle.services.MoodleService;
import unidue.rc.plugins.moodle.services.MoodleServiceImpl;
import unidue.rc.search.SolrService;
import unidue.rc.search.SolrServiceImpl;
import unidue.rc.security.*;
import unidue.rc.statistic.*;
import unidue.rc.ui.services.MimeService;
import unidue.rc.ui.services.MimeServiceImpl;
import unidue.rc.workflow.*;

import javax.sql.DataSource;
import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A <code>CayenneService</code> can be used when this applications starts to load for example default into db.
 *
 * @author Nils Verheyen
 */
public class CayenneServiceImpl implements CayenneService, Module {

    private static final Logger LOG = LoggerFactory.getLogger(CayenneServiceImpl.class);

    private final ServerRuntime serverRuntime;

    private Injector injector;

    public CayenneServiceImpl() {
        serverRuntime = new ServerRuntime("cayenne-unidue-reserve-collections.xml");
    }

    @Override
    public void init() throws DatabaseException, ConfigurationException {
        LOG.info("initializing cayenne");
        initInjector();
        writeDefaults();
    }

    @Override
    public void shutdown() {
        LOG.info("shutting down cayenne");
        serverRuntime.shutdown();
    }

    @Override
    public Injector getInjector() {
        initInjector();
        return injector;
    }

    private void initInjector() {
        if (injector == null) {
            LOG.info("creating cayenne dependency injector");
            injector = DIBootstrap.createInjector(this);
        }
    }

    /**
     * Checks if default values are inside the database and loads them if needed.
     */
    private void writeDefaults() throws ConfigurationException {

        LOG.info("checking if defaults exists");
        ObjectContext objectContext = serverRuntime.getContext();
        BaseContext.bindThreadObjectContext(objectContext);

        writeDefaultConfiguration(objectContext);
        writeDefaultActions(objectContext);
        writeDefaultRoles(objectContext);
        writeDefaultPermissionDefinitions(objectContext);
        writeDefaultLocations(objectContext);
    }

    private void writeDefaultLocations(ObjectContext context) {
        SelectQuery query = new SelectQuery(LibraryLocation.class);

        List<LibraryLocation> presentLocations = context.performQuery(query);
        List<DefaultLocation> unavailableLocations = Arrays.asList(DefaultLocation.values())
                .stream()
                .filter(location -> presentLocations.stream().noneMatch(presentLocation -> presentLocation.getId().equals(location.getId())))
                .collect(Collectors.toList());

        for (DefaultLocation l : unavailableLocations) {
            LibraryLocation location = context.newObject(LibraryLocation.class);
            location.setName(l.getName());
            location.setPhysical(l.isPhysical());
            location.setId(l.getId());

            if (l.getParent() != null) {
                LibraryLocation parent = Cayenne.objectForPK(context, LibraryLocation.class, l.getParent().getId());
                location.setParentLocation(parent);
            }
            context.commitChanges();
        }
    }

    private void writeDefaultPermissionDefinitions(ObjectContext objectContext) {
        SelectQuery query = new SelectQuery(PermissionDefinition.class);
        query.setFetchLimit(1);

        // search for definitions
        List<PermissionDefinition> definitions = objectContext.performQuery(query);

        // if list is empty create default permission definitions
        if (definitions.isEmpty()) {

            // create admin permissions
            final Role adminRole = getRole(DefaultRole.ADMINISTRATOR);
            Arrays.asList(ActionDefinition.values())
                    .forEach(definition -> createPermissionDefinition(getAction(definition), false, adminRole));

            // create collection admin permissions
            final Role collectionAdminRole = getRole(DefaultRole.COLLECTION_ADMIN);
            Arrays.stream(ActionDefinition.values())
                    .filter(definition -> definition != ActionDefinition.EDIT_SETTINGS
                            && definition != ActionDefinition.EDIT_LOCATIONS
                            && definition != ActionDefinition.EDIT_ROLES
                            && definition != ActionDefinition.MIGRATE_OLD_COLLECTIONS)
                    .forEach(definition -> createPermissionDefinition(getAction(definition), false, collectionAdminRole));

            // create all other rights
            Arrays.asList(DefaultPermissionDefinition.values())
                    .forEach(d -> createPermissionDefinition(getAction(d.getAction()), d.isInstanceBound(), getRole(d.getRole())));
        }
    }

    private void createPermissionDefinition(Action action, boolean isInstanceBound, Role role) {

        ObjectContext objectContext = BaseContext.getThreadObjectContext();
        PermissionDefinition definition = objectContext.newObject(PermissionDefinition.class);
        definition.setAction(action);
        definition.setIsInstanceBound(isInstanceBound);
        definition.setRole(role);
        objectContext.commitChanges();
    }

    private Role getRole(DefaultRole role) {
        RoleDAO roleDAO = injector.getInstance(RoleDAO.class);
        return roleDAO.getRole(role);
    }

    private Action getAction(ActionDefinition action) {
        ActionDAO actionDAO = injector.getInstance(ActionDAO.class);
        return actionDAO.getAction(action);
    }

    /**
     * @param objectContext
     */
    private void writeDefaultConfiguration(ObjectContext objectContext) throws ConfigurationException {
        // load config from classpath

        Map<String, DefaultSetting> defaultSettings = getDefaultSettings();

        Collection<DefaultSetting> unavailableSettings = getUnvailableSettings(defaultSettings, objectContext);

        registerSettings(unavailableSettings, objectContext);

    }

    private Collection<DefaultSetting> getUnvailableSettings(Map<String, DefaultSetting> defaultSettings, ObjectContext objectContext) {

        List<Setting> settings = getSettings(objectContext);

        for (Setting setting : settings) {
            defaultSettings.remove(setting.getKey());
        }

        return defaultSettings.values();
    }

    private Map<String, DefaultSetting> getDefaultSettings() throws ConfigurationException {
        Serializer serializer = new Persister();
        URL configFile = CayenneServiceImpl.class.getResource("/sysconfig.xml");

        try {
            DefaultSettingsList defaultSettingsList = serializer.read(DefaultSettingsList.class, new File(configFile.getFile()));
            Map<String, DefaultSetting> settings = defaultSettingsList.getDefMap();
            getPrivateSettings().forEach((key, setting) -> settings.put(key, setting));
            return settings;
        } catch (Exception e) {
            LOG.error("Error opening default config", e);

            throw new ConfigurationException("Could not read default settings!"+e.getMessage());
        }
    }

    private Map<String, DefaultSetting> getPrivateSettings() {

        Map<String, DefaultSetting> result = new HashMap<>();
        URL privateConfigFile = CayenneServiceImpl.class.getResource("/sysconfig_private.xml");
        if (privateConfigFile != null) {

            Serializer serializer = new Persister();
            try {

                DefaultSettingsList privateSettingsList = serializer.read(DefaultSettingsList.class, new File(privateConfigFile.getFile()));
                privateSettingsList.getDefaultSettings().forEach(s -> result.put(s.getKey(), s));
            } catch (Exception e) {
                LOG.error("could not read private settings", e);
            }
        }
        return result;
    }

    /**
     * @param objectContext
     */
    private void writeDefaultRoles(ObjectContext objectContext) {

        // read all roles available in db
        List<Role> roles = getDefaultRoles(objectContext);

        // extract roles which belong to no role in db
        Set<DefaultRole> unavailableRoles = getUnavailableRoles(roles);

        registerRoles(unavailableRoles, objectContext);
    }

    /**
     * Checks backend for default actions and registers them if not available.
     *
     * @param objectContext
     */
    private void writeDefaultActions(ObjectContext objectContext) {

        // read all actions available in db
        List<Action> actions = getActions(objectContext);

        // extract action names which belong to no action in db
        Set<ActionDefinition> unavailableActions = getUnavailableActions(actions, ActionDefinition.values());

        // createResource and registerActions new actions in object context
        registerActions(unavailableActions, objectContext);
    }

    /**
     * Stores target roles as new default roles in backend.
     *
     * @param unavailableRoles
     * @param context
     */
    private void registerRoles(Set<DefaultRole> unavailableRoles, ObjectContext context) {

        for (DefaultRole role : unavailableRoles) {
            LOG.info("registering default role " + role.getName());
            Role newRole = context.newObject(Role.class);
            newRole.setName(role.getName());
            newRole.setIsDefault(Boolean.TRUE);
        }

        // write changes to db
        context.commitChanges();
    }

    /**
     * Returns a new set with all {@link unidue.rc.model.DefaultRole}s in backend which are not marked as default.
     *
     * @param availableRoles
     * @return
     */
    private Set<DefaultRole> getUnavailableRoles(final List<Role> availableRoles) {

        return Arrays.stream(DefaultRole.values())
                .filter(defaultRole ->
                        // default role must not be present in available roles
                        availableRoles.stream()
                                .noneMatch(availableRole -> availableRole.getName().equals(defaultRole.getName())
                                        && availableRole.getIsDefault())
                ).collect(Collectors.toSet());
    }

    private List<Role> getDefaultRoles(ObjectContext context) {

        SelectQuery query = new SelectQuery(Role.class);
        query.setQualifier(ExpressionFactory.matchExp(Role.IS_DEFAULT_PROPERTY, Boolean.TRUE));

        return context.performQuery(query);

    }

    /**
     * Returns all available {@link Action} objects from database.
     *
     * @param context {@link ObjectContext} which is used to load data
     * @return a list with all available actions
     */
    @SuppressWarnings("unchecked")
    private List<Action> getActions(ObjectContext context) {

        SelectQuery query = new SelectQuery(Action.class);

        return context.performQuery(query);
    }

    /**
     * Returns all available {@link Setting} objects from database.
     *
     * @param context {@link ObjectContext} which is used to load data
     * @return a list with all available settings
     */
    @SuppressWarnings("unchecked")
    private List<Setting> getSettings(ObjectContext context) {

        SelectQuery query = new SelectQuery(Setting.class);
        return context.performQuery(query);
    }


    /**
     * Registers new {@link Action} objects with target action names inside target {@link ObjectContext}.
     *
     * @param actionDefinitions names which should be registered in actions
     * @param objectContext
     */
    private void registerActions(Set<ActionDefinition> actionDefinitions, ObjectContext objectContext) {
        for (ActionDefinition actionDefinition : actionDefinitions) {

            LOG.info("registering action " + actionDefinition.getName());
            Action action = objectContext.newObject(Action.class);
            action.setName(actionDefinition.getName());
            action.setResource(actionDefinition.getResource());
        }

        // write changes to db
        objectContext.commitChanges();
    }

    /**
     * Returns a list of all action names, which are not packed inside an action
     *
     * @param availableActions
     * @param actionDefinitions
     * @return a set of action names which are not inside target actions but in available action names
     */
    private Set<ActionDefinition> getUnavailableActions(final List<Action> availableActions, ActionDefinition[] actionDefinitions) {

        return Arrays.asList(actionDefinitions)
                .stream()
                .filter(definition -> availableActions
                        .stream()
                        .noneMatch(action -> action.getName().equals(definition.getName())
                                && action.getResource().equals(definition.getResource())))
                .collect(Collectors.toSet());
    }

    /**
     * Registers new {@link Setting} objects with target setting names and values inside target {@link ObjectContext}.
     *
     * @param defaultSettings which should be registered in settings
     * @param objectContext
     */
    private void registerSettings(Collection<DefaultSetting> defaultSettings, ObjectContext objectContext) {
        for (DefaultSetting defaultSetting : defaultSettings) {

            LOG.info("registering action " + defaultSetting.getKey());
            Setting setting = objectContext.newObject(Setting.class);
            setting.setKey(defaultSetting.getKey());
            setting.setValue(defaultSetting.getValue());
            setting.setLabel(defaultSetting.getLabel());
            setting.setFormat(defaultSetting.getFormat());

            Optional<SettingType> optionalType = Arrays.stream(SettingType.values())
                    .filter(settingType -> settingType.getValue().equals(defaultSetting.getType()))
                    .findFirst();
            setting.setType(optionalType.orElse(SettingType.SYSTEM));
        }
        // write changes to db
        objectContext.commitChanges();
    }


    @Override
    public void configure(Binder binder) {

        LOG.info("binding interfaces to implementations");
        SettingDAO settingDAO = new SettingDAOImpl();
        binder.bind(SettingDAO.class).toInstance(settingDAO);
        try {
            SystemConfigurationServiceImpl config = new SystemConfigurationServiceImpl(getDatabaseConfiguration(), settingDAO);
            binder.bind(SystemConfigurationService.class).toInstance(config);
        } catch (ConfigurationException e) {
            LOG.error("could not create configuration service", e);
        }
        binder.bind(SystemMessageService.class).to(SystemMessageServiceImpl.class);

        // daos
        binder.bind(BaseDAO.class).to(BaseDAOImpl.class);
        binder.bind(EntryDAO.class).to(EntryDAOImpl.class);
        binder.bind(ActionDAO.class).to(ActionDAOImpl.class);
        binder.bind(LegalEntityDAO.class).to(LegalEntityXMLFileDAO.class);
        binder.bind(LibraryLocationDAO.class).to(LibraryLocationDAOImpl.class);
        binder.bind(MCRCategoryDAO.class).to(MCRCategoryDAOImpl.class);
        binder.bind(OriginDAO.class).to(OriginDAOImpl.class);
        binder.bind(ReserveCollectionDAO.class).to(ReserveCollectionDAOImpl.class);
        binder.bind(ReserveCollectionNumberDAO.class).to(ReserveCollectionNumberDAOImpl.class);
        binder.bind(RoleDAO.class).to(RoleDAOImpl.class);
        binder.bind(UserDAO.class).to(UserDAOImpl.class);
        binder.bind(BookDAO.class).to(BookDAOImpl.class);
        binder.bind(HeadlineDAO.class).to(HeadlineDAOImpl.class);
        binder.bind(ResourceDAO.class).to(ResourceDAOImpl.class);
        binder.bind(BookJobDAO.class).to(BookJobDAOImpl.class);
        binder.bind(ScanJobDAO.class).to(ScanJobDAOImpl.class);
        binder.bind(ParticipationDAO.class).to(ParticipationDAOImpl.class);
        binder.bind(PermissionDAO.class).to(PermissionDAOImpl.class);
        binder.bind(MigrationDAO.class).to(MigrationDAOImpl.class);
        binder.bind(MailDAO.class).to(MailDAOImpl.class);
        binder.bind(GoogleBooksDAO.class).to(GoogleBooksDAOImpl.class);
        binder.bind(StatisticDAO.class).to(StatisticDAOImpl.class);
        binder.bind(AlephDAO.class).to(AlephDAOImpl.class);
        binder.bind(WarningDAO.class).to(WarningDAOImpl.class);

        // other services
        binder.bind(OpacFacadeService.class).to(OpacFacadeServiceImpl.class);
        binder.bind(ShiroPermissionUtils.class).to(ShiroPermissionUtilsImpl.class);
        binder.bind(LocalRealm.class).to(LocalRealmImpl.class);
        binder.bind(LDAPRealm.class).to(LDAPRealmImpl.class);
        binder.bind(MigrationService.class).to(MigrationServiceImpl.class);
        binder.bind(LegacyXMLService.class).to(LegacyXMLServiceImpl.class);
        binder.bind(CollectionRSSWriter.class).to(CollectionRSSWriterImpl.class);

        // workflow services
        binder.bind(BookService.class).to(BookServiceImpl.class);
        binder.bind(ScannableService.class).to(ScannableServiceImpl.class);
        binder.bind(ResourceService.class).to(ResourceServiceImpl.class);
        binder.bind(CollectionService.class).to(CollectionServiceImpl.class);
        binder.bind(CollectionWarningService.class).to(CollectionWarningServiceImpl.class);
        binder.bind(ScanJobService.class).to(ScanJobServiceImpl.class);
        binder.bind(BookJobService.class).to(BookJobServiceImpl.class);
        binder.bind(EntryService.class).to(EntryServiceImpl.class);
        binder.bind(LibraryLocationService.class).to(LibraryLocationServiceImpl.class);
        binder.bind(DBStatService.class).to(DBStatServiceImpl.class);

        // system services
        binder.bind(CollectionSecurityService.class).to(CollectionSecurityServiceImpl.class);
        binder.bind(MimeService.class).to(MimeServiceImpl.class);
        binder.bind(SolrService.class).to(SolrServiceImpl.class);
        binder.bind(QuartzService.class).to(QuartzServiceImpl.class);
        binder.bind(CryptService.class).to(CryptServiceImpl.class);
        binder.bind(MoodleService.class).to(MoodleServiceImpl.class);
        binder.bind(MoodleRequestHandler.class).to(MoodleRequestHandlerImpl.class);
        binder.bind(MailService.class).to(MailServiceImpl.class);
        binder.bind(BaseURLService.class).to(BaseURLServiceImpl.class);

        // quartz jobs
        binder.bind(AlephSynchronizer.class).to(AlephSynchronizerImpl.class);
        binder.bind(MailCronJob.class).to(MailCronJobImpl.class);
        binder.bind(MigrationCodeCronJob.class).to(MigrationCodeCronJobImpl.class);
        binder.bind(ScanJobSyncCronJob.class).to(ScanJobSyncCronJobImpl.class);
        binder.bind(StatisticCronJob.class).to(StatisticCronJobImpl.class);
        binder.bind(AccessLogCronJob.class).to(AccessLogCronJobImpl.class);
        binder.bind(DurableCollectionMigrationCronJob.class).to(DurableCollectionMigrationCronJobImpl.class);
        binder.bind(CollectionWarningCronJob.class).to(CollectionWarningCronJobImpl.class);

        // as default .inSingletonScope();
    }

    @Override
    public Job newJob(TriggerFiredBundle bundle, Scheduler scheduler) throws SchedulerException {
        JobDetail jobDetail = bundle.getJobDetail();
        Class<? extends Job> jobClass = jobDetail.getJobClass();
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug(
                        "Producing instance of Job '" + jobDetail.getKey() +
                                "', class=" + jobClass.getName());
            }
            return injector.getInstance(jobClass);
        } catch (Exception e) {
            SchedulerException se = new SchedulerException(
                    "Problem instantiating class '"
                            + jobDetail.getJobClass().getName() + "'", e);
            throw se;
        }
    }

    protected DatabaseConfiguration getDatabaseConfiguration() {
        DataSource source = getCollectionDataSource();
        DatabaseConfiguration config = new DatabaseConfiguration(source, "setting", "key", "value");
        return config;
    }

    protected DataSource getCollectionDataSource() {
        return serverRuntime.getDataSource("reserve_collections_datanode");
    }

    @Override
    public void createContext() {
        ObjectContext context = serverRuntime.getContext();
        BaseContext.bindThreadObjectContext(context);
    }

}
