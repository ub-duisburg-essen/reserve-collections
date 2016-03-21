package unidue.rc.ui.services;

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

import io.buji.pac4j.ClientFilter;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.ioc.Configuration;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.*;
import org.apache.tapestry5.ioc.services.CoercionTuple;
import org.apache.tapestry5.ioc.services.RegistryShutdownHub;
import org.apache.tapestry5.ioc.services.ServiceOverride;
import org.apache.tapestry5.services.*;
import org.apache.tapestry5.services.javascript.JavaScriptStack;
import org.apache.tapestry5.services.javascript.JavaScriptStackSource;
import org.apache.tapestry5.util.StringToEnumCoercion;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.event.implement.IncludeRelativePath;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tynamo.security.Security;
import org.tynamo.security.SecuritySymbols;
import org.tynamo.security.services.SecurityFilterChainFactory;
import org.tynamo.security.services.impl.SecurityFilterChain;
import unidue.rc.auth.LDAPRealm;
import unidue.rc.dao.*;
import unidue.rc.io.CollectionRSSWriter;
import unidue.rc.io.OutputStreamResponse;
import unidue.rc.io.OutputStreamResponseResultProcessor;
import unidue.rc.migration.MigrationService;
import unidue.rc.model.ActionDefinition;
import unidue.rc.plugins.alephsync.AlephSynchronizer;
import unidue.rc.plugins.moodle.services.MoodleRequestHandler;
import unidue.rc.plugins.moodle.services.MoodleService;
import unidue.rc.search.SolrService;
import unidue.rc.security.CollectionSecurityService;
import unidue.rc.auth.LocalRealm;
import unidue.rc.statistic.DBStatService;
import unidue.rc.system.*;
import unidue.rc.ui.CollectionBaseURLSource;
import unidue.rc.ui.SystemConfigurationBindingFactory;
import unidue.rc.workflow.*;

import java.io.File;
import java.util.Properties;

/**
 * This module is automatically included as part of the Tapestry IoC Registry, it's a good place to configure and extend
 * Tapestry, or to place your own service definitions.
 */
public class AppModule {

    private static final Logger LOG = LoggerFactory.getLogger(AppModule.class);

    public static void bind(ServiceBinder binder) {

        /*
         * Make bind() calls on the binder object to define most IoC services.
         * Use service builder methods (example below) when the implementation
         * is provided inline, or requires more initialization than simply
         * invoking the constructor.
         */

        // bind daos
        binder.bind(RoleDAO.class, RoleDAOImpl.class);
        binder.bind(LibraryLocationDAO.class, LibraryLocationDAOImpl.class);
        binder.bind(UserDAO.class, UserDAOImpl.class);
        binder.bind(MCRCategoryDAO.class, MCRCategoryDAOImpl.class);
        binder.bind(LegalEntityDAO.class, LegalEntityXMLFileDAO.class);
        binder.bind(ReserveCollectionNumberDAO.class, ReserveCollectionNumberDAOImpl.class);
        binder.bind(QuartzService.class, QuartzServiceImpl.class);
        binder.bind(SecurityRequestFilter.class, SecurityRequestFilter.class);
        binder.bind(ClientFilter.class, ClientFilter.class);
    }

    @ServiceId(LDAPRealm.NAME)
    public static LDAPRealm buildLDAPRealm(@InjectService("CayenneService") CayenneService
                                                         cayenneService) {
        return cayenneService.getInjector().getInstance(LDAPRealm.class);
    }

    @ServiceId(LocalRealm.NAME)
    public static LocalRealm buildLocalRealm(@InjectService("CayenneService") CayenneService
                                                             cayenneService) {
        return cayenneService.getInjector().getInstance(LocalRealm.class);
    }

    @ServiceId(BaseDAO.SERVICE_NAME)
    public static BaseDAO buildBaseDAO(@InjectService("CayenneService") CayenneService
                                               cayenneService) {
        return cayenneService.getInjector().getInstance(BaseDAO.class);
    }

    @ServiceId(ActionDAO.SERVICE_NAME)
    public static ActionDAO buildActionDAO(@InjectService("CayenneService") CayenneService
                                                   cayenneService) {
        return cayenneService.getInjector().getInstance(ActionDAO.class);
    }

    @ServiceId(EntryDAO.SERVICE_NAME)
    public static EntryDAO buildEntryDAO(@InjectService("CayenneService") CayenneService
                                                 cayenneService) {
        return cayenneService.getInjector().getInstance(EntryDAO.class);
    }

    @ServiceId(HeadlineDAO.SERVICE_NAME)
    public static HeadlineDAO buildHeadlineDAO(@InjectService("CayenneService") CayenneService
                                                       cayenneService) {
        return cayenneService.getInjector().getInstance(HeadlineDAO.class);
    }

    @ServiceId(BookJobDAO.SERVICE_NAME)
    public static BookJobDAO buildBookJobDAO(@InjectService("CayenneService") CayenneService
                                                     cayenneService) {
        return cayenneService.getInjector().getInstance(BookJobDAO.class);
    }

    @ServiceId(ScanJobDAO.SERVICE_NAME)
    public static ScanJobDAO buildScanJobDAO(@InjectService("CayenneService") CayenneService
                                                     cayenneService) {
        return cayenneService.getInjector().getInstance(ScanJobDAO.class);
    }

    @ServiceId(StatisticDAO.SERVICE_NAME)
    public static StatisticDAO buildStatisticsDAO(@InjectService("CayenneService") CayenneService
                                                     cayenneService) {
        return cayenneService.getInjector().getInstance(StatisticDAO.class);
    }

    @ServiceId(ReserveCollectionDAO.SERVICE_NAME)
    public static ReserveCollectionDAO buildReserveCollectionDAO(@InjectService("CayenneService") CayenneService
                                                                         cayenneService) {
        return cayenneService.getInjector().getInstance(ReserveCollectionDAO.class);
    }

    @ServiceId(ParticipationDAO.SERVICE_NAME)
    public static ParticipationDAO buildParticipationDAO(@InjectService("CayenneService") CayenneService
                                                                 cayenneService) {
        return cayenneService.getInjector().getInstance(ParticipationDAO.class);
    }

    @ServiceId(BookDAO.SERVICE_NAME)
    public static BookDAO buildBookDAO(@InjectService("CayenneService") CayenneService
                                               cayenneService) {
        return cayenneService.getInjector().getInstance(BookDAO.class);
    }

    @ServiceId(ResourceDAO.SERVICE_NAME)
    public static ResourceDAO buildResourceDAO(@InjectService("CayenneService") CayenneService
                                                       cayenneService) {
        return cayenneService.getInjector().getInstance(ResourceDAO.class);
    }

    @ServiceId(PermissionDAO.SERVICE_NAME)
    public static PermissionDAO buildPermissionDAO(@InjectService("CayenneService") CayenneService
                                                           cayenneService) {
        return cayenneService.getInjector().getInstance(PermissionDAO.class);
    }

    @ServiceId(SettingDAO.SERVICE_NAME)
    public static SettingDAO buildSettingDAO(@InjectService("CayenneService") CayenneService cayenneService) {
        return cayenneService.getInjector().getInstance(SettingDAO.class);
    }

    public static OriginDAO buildOriginDAO(@InjectService("CayenneService") CayenneService
                                                   cayenneService) {
        return cayenneService.getInjector().getInstance(OriginDAO.class);
    }

    @ServiceId(MigrationDAO.SERVICE_NAME)
    public static MigrationDAO buildMigrationDAO(@InjectService("CayenneService") CayenneService
                                                         cayenneService) {
        return cayenneService.getInjector().getInstance(MigrationDAO.class);
    }

    @ServiceId(AlephDAO.SERVICE_NAME)
    public static AlephDAO buildAlephDAO(@InjectService("CayenneService") CayenneService
                                                         cayenneService) {
        return cayenneService.getInjector().getInstance(AlephDAO.class);
    }

    @ServiceId(WarningDAO.SERVICE_NAME)
    public static WarningDAO buildWarningDAO(@InjectService("CayenneService") CayenneService
                                                         cayenneService) {
        return cayenneService.getInjector().getInstance(WarningDAOImpl.class);
    }

    public static BookService buildBookService(@InjectService("CayenneService") CayenneService
                                                       cayenneService) {
        return cayenneService.getInjector().getInstance(BookService.class);
    }

    public static ScannableService buildScannableService(@InjectService("CayenneService") CayenneService
                                                                 cayenneService) {
        return cayenneService.getInjector().getInstance(ScannableService.class);
    }

    public static ScanJobService buildScanJobService(@InjectService("CayenneService") CayenneService
                                                             cayenneService) {
        return cayenneService.getInjector().getInstance(ScanJobService.class);
    }

    public static ResourceService buildResourceService(@InjectService("CayenneService") CayenneService
                                                               cayenneService) {
        return cayenneService.getInjector().getInstance(ResourceService.class);
    }

    public static OpacFacadeService buildOpacFacadeService(@InjectService("CayenneService") CayenneService
                                                                   cayenneService) {
        return cayenneService.getInjector().getInstance(OpacFacadeService.class);
    }

    public static CollectionService buildCollectionService(@InjectService("CayenneService") CayenneService
                                                                   cayenneService) {
        return cayenneService.getInjector().getInstance(CollectionService.class);
    }

    public static LibraryLocationService buildLibraryLocationService(@InjectService("CayenneService") CayenneService
                                                                             cayenneService) {
        return cayenneService.getInjector().getInstance(LibraryLocationService.class);
    }

    public static EntryService buildEntryService(@InjectService("CayenneService") CayenneService
                                                         cayenneService) {
        return cayenneService.getInjector().getInstance(EntryService.class);
    }

    public static CollectionSecurityService buildCollectionSecurityService(@InjectService("CayenneService") CayenneService
                                                                                   cayenneService) {
        return cayenneService.getInjector().getInstance(CollectionSecurityService.class);
    }

    public static MimeService buildMimeService(@InjectService("CayenneService") CayenneService
                                                       cayenneService) {
        return cayenneService.getInjector().getInstance(MimeService.class);
    }

    public static SolrService buildSolrService(@InjectService("CayenneService") CayenneService
                                                       cayenneService) {
        return cayenneService.getInjector().getInstance(SolrService.class);
    }

    public static SystemConfigurationService buildSystemConfigurationService(@InjectService("CayenneService") CayenneService
                                                                                     cayenneService) {
        return cayenneService.getInjector().getInstance(SystemConfigurationService.class);
    }


    @ServiceId(DBStatService.SERVICE_NAME)
    public static DBStatService buildDBStatService(@InjectService("CayenneService") CayenneService
                                                             cayenneService) {
        return cayenneService.getInjector().getInstance(DBStatService.class);
    }

    public static CryptService buildCryptService(@InjectService("CayenneService") CayenneService
                                                         cayenneService) {
        return cayenneService.getInjector().getInstance(CryptService.class);
    }

    public static MoodleService buildMoodleService(@InjectService("CayenneService") CayenneService
                                                           cayenneService) {
        return cayenneService.getInjector().getInstance(MoodleService.class);
    }

    public static MoodleRequestHandler buildMoodleRequestHandler(@InjectService("CayenneService") CayenneService
                                                           cayenneService) {
        return cayenneService.getInjector().getInstance(MoodleRequestHandler.class);
    }

    public static AlephSynchronizer buildAlephSynchronizer(@InjectService("CayenneService") CayenneService
                                                                   cayenneService) {
        return cayenneService.getInjector().getInstance(AlephSynchronizer.class);
    }

    public static MigrationService buildImporterService(@InjectService("CayenneService") CayenneService
                                                                cayenneService) {
        return cayenneService.getInjector().getInstance(MigrationService.class);
    }

    public static MailService buildMailService(@InjectService("CayenneService") CayenneService
                                                                cayenneService) {
        return cayenneService.getInjector().getInstance(MailService.class);
    }

    public static CollectionRSSWriter buildCollectionRSSWriter(@InjectService("CayenneService") CayenneService
                                                                cayenneService) {
        return cayenneService.getInjector().getInstance(CollectionRSSWriter.class);
    }

    public static BaseURLSource buildCollectionBaseURLService(@InjectService("Request") Request request,
                                                              @InjectService("SystemConfigurationService") SystemConfigurationService config,
                                                              @Inject @Symbol(SymbolConstants.HOSTNAME) String hostname,
                                                              @Symbol(SymbolConstants.HOSTPORT) int hostPort,
                                                              @Symbol(SymbolConstants.HOSTPORT_SECURE) int hostPortSecure) {

        return new CollectionBaseURLSource(request, hostname, hostPort, hostPortSecure, config);
    }

    @Contribute(ServiceOverride.class)
    public static void contributeServiceOverride(
            MappedConfiguration<Class, Object> configuration,
            @Local BaseURLSource baseURLSource) {
        configuration.add(BaseURLSource.class, baseURLSource);
    }

    public static void contributeFactoryDefaults(MappedConfiguration<String, Object> configuration) {
        /*
         * The application version number is incorprated into URLs for some
         * assets. Web browsers will cache assets because of the far future
         * expires header. If existing assets are changed, the version number
         * should also change, to force the browser to download new versions.
         * This overrides Tapesty's default (a random hexadecimal number), but
         * may be further overriden by WebModule or QaModule.
         */
        configuration.override(SymbolConstants.APPLICATION_VERSION, "1.0-SNAPSHOT");
    }

    public static void contributeApplicationDefaults(MappedConfiguration<String, Object> configuration,
                                                     @InjectService("SystemConfigurationService")
                                                     SystemConfigurationService sysConfig) {
        /*
         * Contributions to ApplicationDefaults will override any contributions
         * to FactoryDefaults (with the same key). Here we're restricting the
         * supported locales to just "en" (English). As you add localised
         * message catalogs and other assets, you can extend this list of
         * locales (it's a comma separated series of locale names; the first
         * locale name is the default when there's no reasonable match).
         */
        configuration.add(SymbolConstants.SUPPORTED_LOCALES, "de,en");
        configuration.add(SymbolConstants.HMAC_PASSPHRASE, "developerPassphrase");

        configuration.add(SymbolConstants.HOSTNAME, sysConfig.getString("server.name"));
        configuration.add(SymbolConstants.HOSTPORT, sysConfig.getInt("server.port"));

        configuration.add(SecuritySymbols.LOGIN_URL, "/login");
        configuration.add(SecuritySymbols.SUCCESS_URL, "/login");

        String securePages = sysConfig.getString("secure.pages");
        if (securePages != null && Boolean.valueOf(securePages))
            configuration.add(SymbolConstants.SECURE_ENABLED, "true");
    }

    /**
     * This is a contribution to the RequestHandler service configuration. This is how we extend Tapestry using the
     * timing filter. A common use for this kind of filter is transaction management or security. The @Local annotation
     * selects the desired service by type, but only from the same module. Without @Local, there would be an error due
     * to the other service(s) that implement RequestFilter (defined in other modules).
     *
     * @param configuration configuration to add instances to
     */
    public void contributeRequestHandler(OrderedConfiguration<RequestFilter> configuration) {
        /*
         * Each contribution to an ordered configuration has a name, When
         * necessary, you may set constraints to precisely control the
         * invocation order of the contributed filter within the pipeline.
         */

//        configuration.addInstance("Timing", TimingFilter.class);
        configuration.addInstance("Security", SecurityRequestFilter.class);
    }

    /**
     * Tell Tapestry how to detect and protect pages that require security. We do this by contributing a custom
     * ComponentRequestFilter to Tapestry's ComponentRequestHandler service.
     * - ComponentRequestHandler is shown in
     * http://tapestry.apache.org/request-processing.html#RequestProcessing-Overview
     * - Based on http://tapestryjava.blogspot.com/2009/12/securing-tapestry-pages-with.html
     *
     * @param configuration configuration to add instances to
     */
    public void contributeComponentRequestHandler(OrderedConfiguration<ComponentRequestFilter> configuration) {
        configuration.addInstance("CollectionRequestFilter", CollectionRequestFilter.class);
    }

    /**
     * Add additional binding prefixes to target configuration, so they can be used inside tapestrys tml files.
     *
     * @param configuration configuration to add instances to
     * @param configService configuration service
     */
    public static void contributeBindingSource(MappedConfiguration<String, BindingFactory> configuration,
                                               @InjectService("SystemConfigurationService")
                                               SystemConfigurationService configService) {
        configuration.add("sys", new SystemConfigurationBindingFactory(configService));
    }

    /**
     * Add additional type coercers to target configuration.
     *
     * @param configuration configuration to add instances to
     */
    public static void contributeTypeCoercer(Configuration<CoercionTuple> configuration) {
        configuration.add(CoercionTuple.create(String.class,
                ActionDefinition.class,
                StringToEnumCoercion.create(ActionDefinition.class)));
    }

    /**
     * Adds ComponentEventResultProcessors
     *
     * @param configuration the configuration where new ComponentEventResultProcessors are registered by the type they are processing
     * @param response      the response that the event result processor handles
     */
    public void contributeComponentEventResultProcessor(MappedConfiguration<Class<?>, ComponentEventResultProcessor<?>> configuration, Response response) {
        configuration.add(OutputStreamResponse.class, new OutputStreamResponseResultProcessor(response));
    }

    @Contribute(WebSecurityManager.class)
    public static void addRealms(Configuration<Realm> config,
                                 @InjectService(LocalRealm.NAME) LocalRealm localRealm,
                                 @InjectService(LDAPRealm.NAME) LDAPRealm ldapRealm) {

        config.add(localRealm);
        config.add(ldapRealm);
    }

    @Contribute(HttpServletRequestFilter.class)
    @Marker(Security.class)
    public static void setupSecurity(Configuration<SecurityFilterChain> configuration,
                                     SecurityFilterChainFactory factory,
                                     ClientFilter clientFilter) {

        configuration.add(factory.createChain("/login/**").add(factory.anon()).build());
        configuration.add(factory.createChain("/**").add(factory.anon()).build());

    }

    /**
     * Adds the {@link GlobalJavaScriptStack} to target config so it can be used in any page or component in {@link
     * Import} statements as stack.
     *
     * @param config configuration to add instances to
     */
    @Contribute(JavaScriptStackSource.class)
    public static void addGlobalScriptStack(MappedConfiguration<String, JavaScriptStack> config) {
        config.addInstance("GlobalScriptStack", GlobalJavaScriptStack.class);
    }

    @Startup
    public static void initApplication(
            RegistryShutdownHub registryShutdown,
            @InjectService("SystemConfigurationService") SystemConfigurationService config,
            @InjectService("CayenneService") CayenneService cayenneService
    )
            throws Exception {

        LOG.info("initializing application requirements");

        File uploadRepoDirectory = new File(config.getString("upload.repository-location"));
        if (!uploadRepoDirectory.exists() && !uploadRepoDirectory.mkdirs())
            throw new Exception("upload.repository-location does not exists or could be be created. check system " +
                    "configuration");
        LOG.info("upload directory initialized at: " + uploadRepoDirectory.getAbsolutePath());

        // init velocity
        Properties velocityProperties = new Properties();

        // load templates from classpath
        velocityProperties.put(Velocity.RESOURCE_LOADER, "class");
        velocityProperties.put("class.resource.loader.class", ClasspathResourceLoader.class.getName());

        // set include handler that #parse and #include directives can be used
        velocityProperties.setProperty(Velocity.EVENTHANDLER_INCLUDE, IncludeRelativePath.class.getName());

        // default log may cause problems in production
        velocityProperties.setProperty(Velocity.RUNTIME_LOG, config.getString("velocity.log"));
        Velocity.init(velocityProperties);
    }

}
