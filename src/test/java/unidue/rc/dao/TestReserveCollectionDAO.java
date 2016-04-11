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
package unidue.rc.dao;


import org.apache.cayenne.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import unidue.rc.DbTestUtils;
import unidue.rc.model.LibraryLocation;
import unidue.rc.model.ReserveCollection;
import unidue.rc.model.ReserveCollectionNumber;
import unidue.rc.model.ReserveCollectionStatus;

import java.util.Date;

/**
 * An instance of <code>ReserveCollectionDAOTest</code> tests the implementation of {@link ReserveCollectionDAO} and its
 * default implementation {@link ReserveCollectionDAOImpl}
 *
 * @author Nils Verheyen
 */
public class TestReserveCollectionDAO extends Assert {

    private static final Logger LOG = LoggerFactory.getLogger(TestReserveCollectionDAO.class);

    private ReserveCollectionDAOImpl collectionDAO;
    private LibraryLocation location;
    private DbTestUtils dbTestUtils;

    @BeforeClass
    public void setup() throws Exception {

        LOG.info("running " + this.getClass().getName() + " tests");
        try {

            dbTestUtils = new DbTestUtils();
            dbTestUtils.setupdb();

            collectionDAO = new ReserveCollectionDAOImpl();
            LibraryLocationDAO libraryLocationDao = new LibraryLocationDAOImpl();

            location = new LibraryLocation();
            location.setName("online");
            libraryLocationDao.create(location);
        } catch (DatabaseException | ValidationException e) {
            LOG.error("could not setup " + this.getClass().getSimpleName() + " tests: " + e.getMessage());
            throw e;
        }
    }

    @AfterClass
    public void shutdown() {

        dbTestUtils.shutdown();
        LOG.info("Test of " + this.getClass().getName() + " done...");
    }

    @Test
    public void testCreateInvalidNumber() {
        ReserveCollection rc = createReserveCollection();

        rc.setNumber(null);
        try {
            collectionDAO.create(rc);
        } catch (Exception e) {
            assertTrue(e instanceof CommitException);
        }
    }

    @Test
    public void testCreateInvalidLocation() {
        ReserveCollection rc = createReserveCollection();
        rc.setLibraryLocation(null);
        try {
            collectionDAO.create(rc);
        } catch (CommitException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testCreateInvalidTitle() {
        ReserveCollection rc = createReserveCollection();
        rc.setTitle(null);
        try {
            collectionDAO.create(rc);
        } catch (CommitException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testCreateInvalidValidityDate() {
        ReserveCollection rc = createReserveCollection();
        rc.setValidTo(null);
        try {
            collectionDAO.create(rc);
        } catch (CommitException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testCreateValid() throws CommitException {

        ReserveCollectionNumberDAO numberDAO = new ReserveCollectionNumberDAOImpl();
        ReserveCollectionNumber number = numberDAO.create(1);
        ReserveCollection rc = createReserveCollection();
        rc.setNumber(number);

        collectionDAO.create(rc);

        LOG.info("selecting rc with id " + rc.getObjectId());
        ReserveCollection reserveCollection = collectionDAO.get(ReserveCollection.class, rc.getId());
        assertTrue(reserveCollection != null);
    }

    private ReserveCollection createReserveCollection() {
        ReserveCollection rc = new ReserveCollection();
        rc.setValidTo(new Date());
        rc.setStatus(ReserveCollectionStatus.ACTIVE);
        rc.setLibraryLocation(location);
        rc.setTitle("Test reserve collection");
        return rc;
    }

}
