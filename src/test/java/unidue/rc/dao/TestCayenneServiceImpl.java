package unidue.rc.dao;

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

import org.apache.cayenne.di.Binder;
import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unidue.rc.DbTestUtils;
import unidue.rc.system.CayenneServiceImpl;
import unidue.rc.system.OpacFacadeService;

/**
 * @author Nils Verheyen
 * @since 17.06.13 10:47
 */
public class TestCayenneServiceImpl extends CayenneServiceImpl {

    private static final Logger LOG = LoggerFactory.getLogger(TestCayenneServiceImpl.class);

    @Override
    public void configure(Binder binder) {
        // call before! configure as the system configuration service needs db access on create
        DbTestUtils dbTestUtils = new DbTestUtils();
        try {
            dbTestUtils.setupdb();
        } catch (DatabaseException e) {
            LOG.error("could not setup db");
            throw new IllegalStateException(e);
        }

        // configure dependency injection
        super.configure(binder);
        binder.bind(AlephDAO.class).to(TestAlephDAOImpl.class);
        binder.bind(OpacFacadeService.class).to(TestOpacFacadeImpl.class);

        // properly shutdown test utils as they are created during each test case
        dbTestUtils.shutdown();
    }
}
