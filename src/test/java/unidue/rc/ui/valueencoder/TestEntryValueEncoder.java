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
package unidue.rc.ui.valueencoder;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import unidue.rc.DbTestUtils;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.DatabaseException;
import unidue.rc.dao.EntryDAO;
import unidue.rc.dao.EntryDAOImpl;
import unidue.rc.dao.LibraryLocationDAO;
import unidue.rc.dao.LibraryLocationDAOImpl;
import unidue.rc.dao.ReserveCollectionDAO;
import unidue.rc.dao.ReserveCollectionDAOImpl;
import unidue.rc.dao.ReserveCollectionNumberDAO;
import unidue.rc.dao.ReserveCollectionNumberDAOImpl;
import unidue.rc.model.Headline;
import unidue.rc.model.IntPrimaryKey;
import unidue.rc.model.LibraryLocation;
import unidue.rc.model.ReserveCollection;
import unidue.rc.model.ReserveCollectionNumber;
import unidue.rc.model.ReserveCollectionStatus;

import java.util.Date;

/**
 * @author Nils Verheyen
 * @since 29.08.13 11:41
 */
public class TestEntryValueEncoder extends Assert {

    private static final Logger LOG = LoggerFactory.getLogger(TestEntryValueEncoder.class);

    private EntryDAO dao;

    private DbTestUtils dbTestUtils;
    private Headline headline;

    @BeforeClass
    public void setup() throws DatabaseException, CommitException {
        LOG.info("running " + this.getClass().getName() + " tests");
        try {

            dbTestUtils = new DbTestUtils();
            dbTestUtils.setupdb();

            LibraryLocationDAO llDAO = new LibraryLocationDAOImpl();
            LibraryLocation location = new LibraryLocation();
            location.setName("Test location");
            llDAO.create(location);

            ReserveCollectionNumberDAO numberDAO = new ReserveCollectionNumberDAOImpl();
            ReserveCollectionNumber number = numberDAO.create(1);

            ReserveCollectionDAO rcDAO = null;
            rcDAO = new ReserveCollectionDAOImpl();

            ReserveCollection rc = new ReserveCollection();
            rc.setTitle("Test collection");
            rc.setNumber(number);
            rc.setStatus(ReserveCollectionStatus.ACTIVE);
            rc.setLibraryLocation(location);
            rc.setValidTo(new Date());
            rcDAO.create(rc);

            dao = new EntryDAOImpl();

            headline = new Headline();
            headline.setText("Headline");
            dao.createEntry(headline, rc);
        } catch (DatabaseException | CommitException e) {
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
    public void testToValue() {
        BaseValueEncoder encoder = new BaseValueEncoder(Headline.class, dao);

        IntPrimaryKey value = encoder.toValue(headline.getId().toString());

        Assert.assertEquals(value.getClass(), Headline.class);
    }

    @Test
    public void testToClient() {

        BaseValueEncoder encoder = new BaseValueEncoder(Headline.class, dao);
        Assert.assertEquals(Long.valueOf(encoder.toClient(headline)).intValue(), headline.getId().intValue());
    }
}
