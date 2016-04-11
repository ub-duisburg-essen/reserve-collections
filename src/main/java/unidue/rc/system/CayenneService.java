package unidue.rc.system;


import org.apache.cayenne.BaseContext;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.di.Injector;
import org.apache.commons.configuration.ConfigurationException;
import org.quartz.spi.JobFactory;
import unidue.rc.dao.DatabaseException;

import javax.sql.DataSource;

/**
 * A <code>CayenneStartupService</code> can be used to initialize data which should be available on start of this
 * application.
 *
 * @author Nils Verheyen
 */
public interface CayenneService extends JobFactory {

    String CONTEXT_BUILDER = "cayenne.context.builder";

    /**
     * Initializes all data necessary for this application to run.
     *
     * @throws DatabaseException      thrown if there is any error initializing the database
     * @throws ConfigurationException thrown on misconfiguration
     */
    void init() throws DatabaseException, ConfigurationException;

    /**
     * As cayenne starts for example the {@link org.apache.cayenne.configuration.server.ServerRuntime} it should be
     * properly shut down.
     */
    void shutdown();

    /**
     * <p>
     * Returns the {@link Injector} that should be used on instantiation of cayenne service classes like dao
     * implementations.
     * </p>
     * See <a href="http://cayenne.apache.org/docs/3.1/cayenne-guide/customizing-cayenne-runtime.html#depdendency-injection-container"> Cayenne Dependency Injection Container</a>
     *
     * @return the {@link Injector} instance
     */
    Injector getInjector();

    /**
     * Creates a new {@link BaseContext} that can be used inside current thread
     * using {@link BaseContext#getThreadObjectContext()}
     */
    void createContext();
}
