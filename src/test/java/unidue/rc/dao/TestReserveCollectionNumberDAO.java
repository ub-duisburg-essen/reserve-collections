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

import miless.model.User;
import org.apache.cayenne.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import unidue.rc.DbTestUtils;
import unidue.rc.model.LibraryLocation;
import unidue.rc.model.ReserveCollection;
import unidue.rc.model.ReserveCollectionNumber;
import unidue.rc.model.ReserveCollectionStatus;
import unidue.rc.workflow.AppendNumberGenerator;
import unidue.rc.workflow.InsertNumberGenerator;
import unidue.rc.workflow.NumberGeneratorStrategy;

import java.util.Date;
import java.util.List;

/**
 * @author Nils Verheyen
 * @since 17.02.14 09:54
 */
public class TestReserveCollectionNumberDAO {
    private static final Logger LOG = LoggerFactory.getLogger(TestReserveCollectionNumberDAO.class);

    private ReserveCollectionNumberDAO numberDAO;
    private ReserveCollectionDAO collectionDAO;
    private LibraryLocationDAO locationDAO;
    private DbTestUtils dbTestUtils;
    private User testUser;

    @BeforeClass
    public void setup() throws Exception {

        LOG.info("running " + this.getClass().getName() + " tests");
        try {

            dbTestUtils = new DbTestUtils();
            dbTestUtils.setupdb();

            numberDAO = new ReserveCollectionNumberDAOImpl();
            collectionDAO = new ReserveCollectionDAOImpl();
            locationDAO = new LibraryLocationDAOImpl();

            UserDAO userDAO = new UserDAOImpl();
            testUser = new User();
            testUser.setUsername("test");
            testUser.setPassword("test");
            testUser.setEmail("test@test.de");
            testUser.setRealname("User, Test");
            testUser.setRealm("test");
            testUser.setOrigin("test");
            userDAO.create(testUser);

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
    public void testAppendNumberStrategy() throws CommitException {
        LibraryLocationDAO locationDAO = new LibraryLocationDAOImpl();
        NumberGeneratorStrategy strategy = new AppendNumberGenerator(numberDAO);

        LibraryLocation location = createLocation("testAppendNumberStrategy location", false);
        locationDAO.create(location);

        // first number = 1
        ReserveCollectionNumber number = strategy.buildNumber(location);
        numberDAO.create(number);
        Assert.assertEquals(number.getNumber(), new Integer(1));

        ReserveCollection collection = createReserveCollection(location, number);
        collectionDAO.create(collection);

        // update collection to use target number
        collection.setNumber(number);
        collectionDAO.update(collection);

        // create second number = 2
        number = strategy.buildNumber(location);
        numberDAO.create(number);
        Assert.assertEquals(number.getNumber(), new Integer(2));

        // update number that there is an active collection with number 100
        number.setNumber(100);
        numberDAO.createOrUpdate(number);

        // should be 101
        number = strategy.buildNumber(location);
        numberDAO.createOrUpdate(number);
        Assert.assertEquals(number.getNumber(), new Integer(101));
    }

    @Test
    public void testInsertNumberStrategy() throws CommitException {
        LibraryLocationDAO locationDAO = new LibraryLocationDAOImpl();
        NumberGeneratorStrategy strategy = new InsertNumberGenerator(numberDAO);

        LibraryLocation location = createLocation("testInsertNumberStrategy location", false);
        locationDAO.create(location);

        for (int i = 1; i < 10; i++)
            numberDAO.create(i);

        // create first collection to use number 1
        ReserveCollection collection = createReserveCollection(location, numberDAO.getNumber(1));
        collectionDAO.create(collection);

        // create second collection to use number 5
        collection = createReserveCollection(location, numberDAO.getNumber(5));
        collectionDAO.create(collection);

        // should be 2
        ReserveCollectionNumber number = strategy.buildNumber(location);
        numberDAO.createOrUpdate(number);
        Assert.assertEquals(number.getNumber(), new Integer(2));
    }

    @AfterMethod
    private void afterTest() throws DeleteException {

        try {

            List<ReserveCollection> collections = collectionDAO.getReserveCollections();
            for (ReserveCollection collection : collections) {
                collectionDAO.delete(collection);
            }

            List<LibraryLocation> locations = locationDAO.getLocations();
            for (LibraryLocation location : locations)
                locationDAO.delete(location);

            List<ReserveCollectionNumber> numbers = numberDAO.getNumbers();
            for (ReserveCollectionNumber number : numbers)
                numberDAO.delete(number);
        } catch (DeleteException e) {
            LOG.error("after test failed", e);
        }
    }

    private static LibraryLocation createLocation(String name, boolean isPhysical) {
        LibraryLocation location = new LibraryLocation();
        location.setName(name);
        location.setPhysical(isPhysical);
        return location;
    }


    private ReserveCollection createReserveCollection(LibraryLocation location, ReserveCollectionNumber number) {
        ReserveCollection rc = new ReserveCollection();
        rc.setValidTo(new Date());
        rc.setNumber(number);
        rc.setStatus(ReserveCollectionStatus.ACTIVE);
        rc.setLibraryLocation(location);
        rc.setTitle("Test reserve collection");
        return rc;
    }
}
