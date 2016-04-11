package unidue.rc;


import miless.model.User;
import org.apache.cayenne.BaseContext;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.access.DataNode;
import org.apache.cayenne.access.dbsync.SchemaUpdateStrategy;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.SelectQuery;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.DatabaseConfiguration;
import org.h2.jdbcx.JdbcDataSource;
import org.osjava.sj.memory.MemoryContextFactory;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unidue.rc.dao.ActionDAOImpl;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.DatabaseException;
import unidue.rc.dao.EntryDAOImpl;
import unidue.rc.dao.LibraryLocationDAOImpl;
import unidue.rc.dao.ReserveCollectionDAOImpl;
import unidue.rc.dao.ReserveCollectionNumberDAOImpl;
import unidue.rc.dao.RoleDAOImpl;
import unidue.rc.dao.TestCayenneServiceImpl;
import unidue.rc.dao.UserDAO;
import unidue.rc.dao.UserDAOImpl;
import unidue.rc.model.Action;
import unidue.rc.model.ActionDefinition;
import unidue.rc.model.Book;
import unidue.rc.model.BookChapter;
import unidue.rc.model.DefaultPermissionDefinition;
import unidue.rc.model.DefaultRole;
import unidue.rc.model.File;
import unidue.rc.model.Headline;
import unidue.rc.model.Html;
import unidue.rc.model.JournalArticle;
import unidue.rc.model.LibraryLocation;
import unidue.rc.model.PermissionDefinition;
import unidue.rc.model.ReserveCollection;
import unidue.rc.model.ReserveCollectionNumber;
import unidue.rc.model.ReserveCollectionStatus;
import unidue.rc.model.Resource;
import unidue.rc.model.Role;
import unidue.rc.model.Setting;
import unidue.rc.model.SettingType;
import unidue.rc.model.WebLink;
import unidue.rc.model.config.DefaultSetting;
import unidue.rc.model.config.DefaultSettingsList;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.net.URL;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An instance of <code>DbTestUtils</code> can be used to create the data sources used by cayenne. Be sure to call
 * {@link DbTestUtils#setupdb()} to initialize all databases and bind them to the root jndi context. After all databases
 * where setup, they must be closed by {@link DbTestUtils#shutdown()}. Take a look at the cayennes configuration to get
 * the jndi names of all used databases.
 *
 * @author Nils Verheyen
 */
public class DbTestUtils {

    private static final String ROOT_JNDI_CONTEXT_NAME = "java:comp/env";

    private static final Logger LOG = LoggerFactory.getLogger(DbTestUtils.class);

    private ServerRuntime runtime;

    private Context jndiContext;

    private Set<String> jndiBindings;

    private ReserveCollectionNumberDAOImpl numberDAO;

    private ReserveCollectionDAOImpl collectionDAO;

    private LibraryLocationDAOImpl locationDAO;

    private EntryDAOImpl entryDAO;

    private RoleDAOImpl roleDAO;

    private ActionDAOImpl actionDAO;

    private User testUser;

    private Integer number_value = 0;

    private LibraryLocation online;

    private LibraryLocation physical;


    /**
     * Creates a new h2 in-memory database with target db name. username and password are <code>sa</code>
     *
     * @param dbName
     * @return a new {@link JdbcDataSource}
     */
    private static JdbcDataSource createH2MemoryDB(String dbName) {
        JdbcDataSource dataSource = new JdbcDataSource();

        /*
         * set ;DB_CLOSE_DELAY=-1 otherwise the db is lost after the first
         * connection is closed
         */
        dataSource.setURL("jdbc:h2:mem:" + dbName + ";DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("sa");
        return dataSource;
    }

    private static LibraryLocation createLocation(String name,
                                                  boolean isPhysical) {
        LibraryLocation location = new LibraryLocation();
        location.setName(name);
        location.setPhysical(isPhysical);
        return location;
    }

    /**
     * Initializes all databases used inside unit tests.
     *
     * @throws DatabaseException  thrown if the db could not been setup
     */
    public void setupdb() throws DatabaseException {

        this.jndiBindings = new HashSet<>();

        createJndiContext();

        createDatabase("miless", "jdbc/miless");
        createDatabase("reserve_collections", "jdbc/reserve_collections");
        createDatabase("rc_access_log", "jdbc/rc_access_log");
        initCayenne();
        initDAOs();
        try {
            writeDefaultConfiguration();

        } catch (ConfigurationException e) {
            e.printStackTrace();
        }



    }

    public void writeDefaultValues() {
        writeDefaultRoles();
        writeDefaultActions();
        writeDefaultPermissionDefinitions();
    }


    private void writeDefaultActions() {

        ObjectContext context = BaseContext.getThreadObjectContext();
        // read all actions available in db
        List<Action> actions = getActions(context);

        // extract action names which belong to no action in db
        Set<ActionDefinition> unavailableActions = getUnavailableActions(actions, ActionDefinition.values());

        // createResource and registerActions new actions in object context
        registerActions(unavailableActions, context);
    }

    private void writeDefaultConfiguration() throws ConfigurationException {

        ObjectContext context = BaseContext.getThreadObjectContext();
        // load config from classpath

        getDefaultSettings()
                .entrySet()
                .forEach(setting -> {
                    Setting s = context.newObject(Setting.class);
                    s.setKey(setting.getKey());
                    s.setValue(setting.getValue().getValue());

                    s.setFormat(setting.getValue().getFormat());
                    Optional<SettingType> optionalType = Arrays.stream(SettingType.values())
                            .filter(settingType -> settingType.getValue().equals(setting.getValue().getType()))
                            .findFirst();
                    s.setType(optionalType.orElse(SettingType.SYSTEM));

                });
        context.commitChanges();
    }

    private Map<String, DefaultSetting> getDefaultSettings() throws ConfigurationException {
        Serializer serializer = new Persister();
        URL configFile = TestCayenneServiceImpl.class.getResource("/sysconfig.xml");

        try {
            DefaultSettingsList defaultSettingsList = serializer.read(DefaultSettingsList.class, new java.io.File(configFile.getFile()));
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
        URL privateConfigFile = TestCayenneServiceImpl.class.getResource("/test_sysconfig_private.xml");
        if (privateConfigFile != null) {

            Serializer serializer = new Persister();
            try {

                DefaultSettingsList privateSettingsList = serializer.read(DefaultSettingsList.class, new java.io.File(privateConfigFile.getFile()));
                privateSettingsList.getDefaultSettings().forEach(s -> result.put(s.getKey(), s));
            } catch (Exception e) {
                LOG.error("could not read private settings", e);
            }
        }
        return result;
    }

    private void createMockLocations() {
        if (online == null) {
            online = createLocation("online", false);
            try {
                locationDAO.create(online);
            } catch (CommitException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        if (physical == null) {
            physical = createLocation("physical", true);
            try {
                locationDAO.create(physical);
            } catch (CommitException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    private void initDAOs() {
        entryDAO = new EntryDAOImpl();
        numberDAO = new ReserveCollectionNumberDAOImpl();
        collectionDAO = new ReserveCollectionDAOImpl();
        locationDAO = new LibraryLocationDAOImpl();
        roleDAO = new RoleDAOImpl();
        actionDAO = new ActionDAOImpl();

    }

    /**
     * Loads cayennes configuration, creates database schema and binds a new {@link ObjectContext} to the current
     * thread.
     *
     * @throws DatabaseException thrown if a schema of one {@link DataNode} could not be initialized.
     */
    private void initCayenne() throws DatabaseException {
        // initialize cayenne configuration
        runtime = new ServerRuntime("cayenne-unidue-reserve-collections.xml");

        Collection<DataNode> nodes = runtime.getDataDomain().getDataNodes();
        for (DataNode node : nodes) {
            // in memory db was just created, therefore the schema has to be created.
            SchemaUpdateStrategy updateStrategy = new CreateSchemaStrategy();
            try {
                updateStrategy.updateSchema(node);
                LOG.debug("node " + node.getName() + " updated");
            } catch (SQLException e) {
                throw new DatabaseException("could not update schema of node" + node.getName(), e);
            }
        }

        /*
         * bind ObjectContext to current thread, so test are able to use BaseContext.getThreadObjectContext()
         * to retrieve context.
         */
        ObjectContext dc = runtime.getContext();
        BaseContext.bindThreadObjectContext(dc);

        LOG.info("object context bound to current thread");
    }

    /**
     * Creates a jndi {@link Context} where databases can be bound to.
     *
     * @throws DatabaseException thrown if the jndi context could not be found or created.
     */
    private void createJndiContext() throws DatabaseException {
        /*
         * set factory to use when creating new jndi naming context to in memory
         * context factory
         */
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, MemoryContextFactory.class.getName());

        /*
         * will put the in-memory JNDI implementation into a mode whereby all
         * InitialContext's share the same memory. By default this is not set,
         * so two separate InitialContext's do not share the same memory and
         * what is bound to one will not be viewable in the other.
         */
        System.setProperty("org.osjava.sj.jndi.shared", "true");

        // create root node of this jndi naming context
        try {
            jndiContext = new InitialContext();
        } catch (NamingException e) {
            throw new DatabaseException("could not create jndi context", e);
        }
        try {

            /*
             * see
             * http://stackoverflow.com/questions/4099095/what-does-javacomp-env-do
             * or http://www.prozesse-und-systeme.de/jndiResourcen.html
             */
            jndiContext.lookup(ROOT_JNDI_CONTEXT_NAME);

            LOG.info("jndi context " + ROOT_JNDI_CONTEXT_NAME + " found");
        } catch (NamingException e) {
            try {
                jndiContext.createSubcontext(ROOT_JNDI_CONTEXT_NAME);
                LOG.info("jndi context " + ROOT_JNDI_CONTEXT_NAME + " created");
            } catch (NamingException createException) {
                throw new DatabaseException("could not create context " + ROOT_JNDI_CONTEXT_NAME, e);
            }
        }
    }

    /**
     * Creates a database according to {@link #createH2MemoryDB(String)} with target database name and binds it to
     * target jndi name, if it not already present.
     *
     * @param databaseName name of the database which should be created
     * @param jndiName     jndi resource name with which the database can be addressed.
     */
    private void createDatabase(String databaseName, String jndiName) {

        // create in memory databases
        JdbcDataSource dataSource = createH2MemoryDB(databaseName);

        try {
            /*
             * bind created databases to jndi. inside
             * "cayenne-unidue-reserve-collections.xml" is defined which jndi names
             * are used by cayenne
             */
            jndiContext.bind(jndiName, dataSource);
            jndiBindings.add(jndiName);
            LOG.info("database \"" + databaseName + "\" bound to jndi name \"" + jndiName + "\"");
        } catch (NamingException e) {
            LOG.warn("could not bind database \"" + databaseName + "\" to jndi name \"" + jndiName + "\". rebinding ." + "..");
            try {
                jndiContext.rebind(jndiName, dataSource);
            } catch (NamingException rebindException) {
                LOG.warn("could not rebind database \"" + databaseName + "\" to jndi name \"" + jndiName + "\". " +
                        rebindException.getMessage());
            }
        }
    }

    /**
     * Closes all resources initialized by {@link DbTestUtils#setupdb()}.
     */
    public void shutdown() {
        runtime.shutdown();
        try {
            for (String jndiBinding : jndiBindings) {

                LOG.info("unbinding " + jndiBinding);
                jndiContext.unbind(jndiBinding);
            }

            jndiContext.destroySubcontext(ROOT_JNDI_CONTEXT_NAME);
            jndiContext.close();
            LOG.info("jndi sub context " + ROOT_JNDI_CONTEXT_NAME + " successfully destroyed");
        } catch (NamingException e) {
            LOG.error("could not destroy jndi context " + ROOT_JNDI_CONTEXT_NAME);
        }
    }

    public void createMockUser() {
        if (testUser == null) {
            UserDAO userDAO = new UserDAOImpl();

            testUser = new User();
            testUser.setUsername("test");
            testUser.setPassword("test");
            testUser.setEmail("test@test.de");
            testUser.setRealname("User, Test");
            testUser.setRealm("test");
            testUser.setOrigin("test");
            try {
                userDAO.create(testUser);
            } catch (CommitException e) {
                e.printStackTrace();
            }
        }
    }

    public User createUser(String name) {
        UserDAO userDAO = new UserDAOImpl();
        User user = new User();
        user.setUsername(name);
        user.setPassword("test");
        user.setEmail(name+"@test.de");
        user.setRealname("User, Test");
        user.setRealm("test");
        user.setOrigin("test");
        try {
            userDAO.create(user);
            return user;
        } catch (CommitException e) {
            e.printStackTrace();
        }
        return null;
    }

    public DatabaseConfiguration getDatabaseConfiguration() {
        DataSource dataSource = runtime.getDataSource("reserve_collections_datanode");
        return new DatabaseConfiguration(dataSource, "setting", "key", "value");
    }

    public ReserveCollection createMockReserveCollection(String title) throws CommitException {
        createMockUser();
        createMockLocations();
        Long time = System.currentTimeMillis();
        number_value++;
        ReserveCollectionNumber number = numberDAO.create(number_value);
        ReserveCollection rc = createReserveCollection(online, number, title, testUser);

        collectionDAO.create(rc);

        return rc;
    }

    public ReserveCollection createMockReserveCollection(String title, LibraryLocation location, User user) throws CommitException {

        Long time = System.currentTimeMillis();
        number_value++;
        ReserveCollectionNumber number = numberDAO.create(number_value);
        ReserveCollection rc = createReserveCollection(location, number, title, user);

        collectionDAO.create(rc);

        return rc;
    }

    public Headline createMockHeadline(String title, ReserveCollection rc) throws CommitException {
        Headline headline = new Headline();
        headline.setText(title);

        entryDAO.createEntry(headline, rc);

        return headline;

    }

    public BookChapter createMockBookChapter(ReserveCollection rc) throws CommitException {
        BookChapter chapter = new BookChapter();
        chapter.setBookTitle("BookTitle");
        chapter.setChapterTitle("ChapterTitle");
        chapter.setPageStart("100");
        chapter.setPageEnd("110");
        chapter.setPlaceOfPublication("TestPlace");

        entryDAO.createEntry(chapter, rc);
        return chapter;
    }

    public Book createMockBook(ReserveCollection rc) throws CommitException {
        Book book = new Book();
        book.setTitle("BookTitle");

        entryDAO.createEntry(book, rc);
        return book;
    }

    public File createMockFile(ReserveCollection rc) throws CommitException {
        File file = new File();
        entryDAO.createEntry(file, rc);

        Resource resource = new Resource();
        resource.setFilePath("files/datei.tst");
        file.setResource(resource);
        entryDAO.update(file);
        return file;
    }

    public Html createMockHtml(ReserveCollection rc) throws CommitException {
        Html html = new Html();
        html.setText("TEXT");
        entryDAO.createEntry(html, rc);
        return html;
    }

    public JournalArticle createMockJournal(ReserveCollection rc) throws CommitException {
        JournalArticle article = new JournalArticle();
        article.setArticleTitle("TestArticleTitle");
        article.setJournalTitle("TestJournalTitle");
        article.setPageStart("100");
        article.setPageEnd("110");
        article.setPlaceOfPublication("TestPlace");
        entryDAO.createEntry(article, rc);
        return article;
    }

    public WebLink createMockWeblink(ReserveCollection rc) throws CommitException {
        WebLink weblink = new WebLink();
        weblink.setUrl("http://www.test.de");
        weblink.setName("TESTSEITE");
        entryDAO.createEntry(weblink, rc);
        return weblink;
    }

    private ReserveCollection createReserveCollection(LibraryLocation location,
        ReserveCollectionNumber number, String title, User user) {
        ReserveCollection rc = new ReserveCollection();
        rc.setValidTo(new Date());
        rc.setNumber(number);
        rc.setStatus(ReserveCollectionStatus.ACTIVE);
        rc.setLibraryLocation(location);
        rc.setTitle(title);
        return rc;
    }


    private void writeDefaultRoles() {
        ObjectContext context = BaseContext.getThreadObjectContext();
        // read all roles available in db
        List<Role> roles = getDefaultRoles(context);

        // extract roles which belong to no role in db
        Set<DefaultRole> unavailableRoles = getUnavailableRoles(roles);

        registerRoles(unavailableRoles, context);
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

    private void writeDefaultPermissionDefinitions() {

        ObjectContext context = BaseContext.getThreadObjectContext();
        SelectQuery query = new SelectQuery(PermissionDefinition.class);
        query.setFetchLimit(1);

        // search for definitions
        List<PermissionDefinition> definitions = context.performQuery(query);

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
        return roleDAO.getRole(role);
    }

    private Action getAction(ActionDefinition action) {
        return actionDAO.getAction(action);
    }
}
