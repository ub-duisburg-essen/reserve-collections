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
