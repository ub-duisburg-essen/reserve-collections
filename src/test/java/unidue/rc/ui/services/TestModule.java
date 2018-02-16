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
package unidue.rc.ui.services;


import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.InjectService;
import org.slf4j.Logger;
import unidue.rc.dao.DatabaseException;
import unidue.rc.dao.TestCayenneServiceImpl;
import unidue.rc.system.CayenneService;

/**
 * @author Nils Verheyen
 */
public class TestModule {

    public static void bind(ServiceBinder binder) {
        binder.bind(CayenneService.class, TestCayenneServiceImpl.class);
    }


    public static void contributeApplicationDefaults(
            final Logger log,
            final @InjectService("CayenneService") CayenneService cayenneService,
            MappedConfiguration<String, Object> configuration) {
        try {
            cayenneService.init();
        } catch (DatabaseException e) {
            log.error("could not initialize cayenne: ", e);
        } catch (ConfigurationException e) {
            log.error("could not load default config: ", e);
        }

        // The factory default is true but during the early stages of an application
        // overriding to false is a good idea. In addition, this is often overridden
        // on the command line as -Dtapestry.production-mode=false
        configuration.add(SymbolConstants.PRODUCTION_MODE, false);

        // The application version number is incorprated into URLs for some
        // assets. Web browsers will cache assets because of the far future expires
        // header. If existing assets are changed, the version number should also
        // change, to force the browser to download new versions.
        configuration.add(SymbolConstants.APPLICATION_VERSION, "1.0-SNAPSHOT-TEST");
    }
}