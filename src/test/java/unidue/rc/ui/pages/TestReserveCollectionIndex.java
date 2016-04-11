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
package unidue.rc.ui.pages;


import com.thoughtworks.selenium.Wait;
import miless.model.User;
import org.apache.commons.configuration.DatabaseConfiguration;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.tapestry5.test.SeleniumTestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import unidue.rc.DbTestUtils;
import unidue.rc.dao.BaseDAOImpl;
import unidue.rc.dao.LibraryLocationDAOImpl;
import unidue.rc.dao.ReserveCollectionDAOImpl;
import unidue.rc.dao.ReserveCollectionNumberDAOImpl;
import unidue.rc.model.IntPrimaryKey;
import unidue.rc.model.LibraryLocation;
import unidue.rc.model.ReserveCollection;
import unidue.rc.model.ReserveCollectionNumber;
import unidue.rc.model.ReserveCollectionStatus;
import unidue.rc.model.solr.SolrCollectionView;
import unidue.rc.search.SolrService;
import unidue.rc.search.SolrServiceImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class TestReserveCollectionIndex extends SeleniumTestCase {

    private static final Logger LOG = LoggerFactory
            .getLogger(TestReserveCollectionIndex.class);

    private ReserveCollectionNumberDAOImpl numberDAO;
    private ReserveCollectionDAOImpl collectionDAO;
    private LibraryLocationDAOImpl locationDAO;
    private DbTestUtils dbTestUtils;
    private User testUserA;
    private User testUserB;
    private ReserveCollection A;
    private ReserveCollection B;
    private BaseDAOImpl baseDAO;
    private SolrServiceImpl solrService;

    @BeforeClass
    public void setup() throws Exception {
        LOG.info("running " + this.getClass().getName() + " tests");

        try {
            dbTestUtils = new DbTestUtils();
            dbTestUtils.setupdb();

            numberDAO = new ReserveCollectionNumberDAOImpl();
            collectionDAO = new ReserveCollectionDAOImpl();
            locationDAO = new LibraryLocationDAOImpl();
            solrService = new SolrServiceImpl();
            baseDAO = new BaseDAOImpl();
            testUserA = new User();
            testUserA.setUsername("UserA");
            testUserA.setPassword("test");
            testUserA.setEmail("testA@test.de");
            testUserA.setRealname("UserA, TestA");
            testUserA.setRealm("testA");
            testUserA.setOrigin("testA");
            testUserA.setUserid(1);
            baseDAO.create(testUserA);

            testUserB = new User();
            testUserB.setUsername("UserB");
            testUserB.setPassword("test");
            testUserB.setEmail("testB@test.de");
            testUserB.setRealname("UserB, TestB");
            testUserB.setRealm("testB");
            testUserB.setOrigin("testB");
            testUserB.setUserid(2);
            baseDAO.create(testUserB);

            LibraryLocation online = createLocation("A", false);
            locationDAO.create(online);

            LibraryLocation physical = createLocation("B", true);
            locationDAO.create(physical);

            ReserveCollectionNumber numberA = numberDAO.create(100);

            ReserveCollectionNumber numberB = numberDAO.create(200);

            A = createReserveCollection(online, numberA, "TestA", testUserA);
            collectionDAO.create(A);

            B = createReserveCollection(physical, numberB, "TestB", testUserB);
            collectionDAO.create(B);

            commitCollectionToSolr(A,testUserA);

            commitCollectionToSolr(B,testUserB);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void setQueryFilter() {
        openBaseURL();
        typeKeys("//input[@name='textfield']", "TestA");
        focus("name=textfield");
        keyPressNative("10");


    }

     @Test
    public void addLocationFilter() {
        openBaseURL();
        open("/index:addfilter/LOCATION_FILTER");
        select("//select[@name='locationFilter']", "A");

        new Wait() {

            @Override
            public boolean until() {
                return isTextPresent("TestA") && !isTextPresent("TestB");
            }
        }.wait("location A should be filtered", 5000);

        open("/index:removefilter/LOCATION_FILTER");
    }

     @Test
    public void addStatusFilter() {
        openBaseURL();
        open("/index:addfilter/COLLECTION_STATUS_FILTER");
        select("//select[@name='collectionStatusFilter']", "Abgelaufen");

        new Wait() {

            @Override
            public boolean until() {
                return !isTextPresent("TestA") && !isTextPresent("TestB");
            }
        }.wait("reservecollection A and B should be not visible", 5000);
        open("/index:removefilter/COLLECTION_STATUS_FILTER");
    }

    @Test
    public void sortByLocation() {
        openBaseURL();
        click("name=sortByLocation");
        click("name=sortByLocation");
        new Wait() {
            @Override
            public boolean until() {
                return getText(
                        "//div[@id='reserveCollectionZone']/table/tbody/tr[1]/td[1]/")
                        .contains("B");
            }
        }.wait("location B should be in first Row", 5000);
        click("name=sortByLocation");
        click("name=sortByLocation");
    }



    @Test
    public void sortByNumber() {
        openBaseURL();
        click("name=sortByNumber");
        click("name=sortByNumber");
        new Wait() {

            @Override
            public boolean until() {
                return getText(
                        "//div[@id='reserveCollectionZone']/table/tbody/tr[1]/td[2]/")
                        .contains("200");
            }
        }.wait("reservecollection B with number 200 should be in first Row", 5000);

        click("name=sortByNumber");
        click("name=sortByNumber");
    }

    @Test
    public void sortByTitle() {
        openBaseURL();
        click("name=sortByTitle");
        click("name=sortByTitle");

        new Wait() {

            @Override
            public boolean until() {
                return getText(
                        "//div[@id='reserveCollectionZone']/table/tbody/tr[1]/td/a/")
                        .contains("TestB");
            }
        }.wait("reservecollection TestB should be in first Row", 5000);
        click("name=sortByTitle");
        click("name=sortByTitle");
    }



    protected void typeInField(final String fieldId, String value) {
        typeKeys("//input[@type='text'][@id='" + fieldId + "']", value);
    }


    @AfterClass
    public void shutdown() {
        deleteCollectionFromSolr(A);
        deleteCollectionFromSolr(B);

        dbTestUtils.shutdown();

        LOG.info("Test of " + this.getClass().getName() + " done...");
    }

    private static LibraryLocation createLocation(String name,
                                                  boolean isPhysical) {
        LibraryLocation location = new LibraryLocation();
        location.setName(name);
        location.setPhysical(isPhysical);
        return location;
    }

    private ReserveCollection createReserveCollection(LibraryLocation location,
                                                      ReserveCollectionNumber number, String title, User user) {
        ReserveCollection rc = new ReserveCollection();
        Calendar cal = new GregorianCalendar();
        cal.add(Calendar.DAY_OF_YEAR,+30);
        rc.setValidTo(cal.getTime());

        rc.setNumber(number);
        rc.setStatus(ReserveCollectionStatus.ACTIVE);
        rc.setLibraryLocation(location);
        rc.setTitle(title);
        return rc;
    }

    private SolrCollectionView createSolrCollectionView(ReserveCollection reserveCollection, User user) {
        List<String> docentNames = new ArrayList<String>();
        docentNames.add(user.getRealname());
        SolrCollectionView view = new SolrCollectionView();
        view.setCollectionID(reserveCollection.getId().toString());
        view.setTitle(reserveCollection.getTitle());
        view.setStatus(reserveCollection.getStatus().getDatabaseValue().toString());
        view.setCollectionNumber(reserveCollection.getNumber().getNumber().toString());
        view.setCollectionNumberNumeric(reserveCollection.getNumber().getNumber());
        view.setLocation(reserveCollection.getLibraryLocation().getName());
        view.setLocationID(reserveCollection.getLibraryLocation().getId());
        view.setValidTo(reserveCollection.getValidTo());
        view.setCollectionComment(reserveCollection.getComment());
        return view;
    }


    private void commitCollectionToSolr(ReserveCollection reserveCollection, User user) {

        SolrCollectionView view = createSolrCollectionView(reserveCollection, user);

        addBean(view, SolrService.Core.ReserveCollection);
    }

    private void deleteCollectionFromSolr(ReserveCollection reserveCollection) {

        deleteByID(reserveCollection, SolrService.Core.ReserveCollection);
    }


    public void deleteByID(IntPrimaryKey id, SolrService.Core core) {

        try {
            SolrClient client = getClient(core);
            client.deleteById(id.getId().toString());
            client.commit();
        } catch (IOException e) {
            LOG.error("i/o error on solr", e);
        } catch (SolrServerException e) {
            LOG.error("solr server error", e);
        }
    }

    private SolrClient getClient(SolrService.Core core) {
        DatabaseConfiguration config = dbTestUtils.getDatabaseConfiguration();
        String coreURL = config.getString("solr.core." + core.value);

        LOG.debug("SOLR CORE URL IS "+coreURL+" KEY IS "+"solr.core."+core.value);

        return new HttpSolrClient(coreURL);

    }

    private void addBean(Object bean, SolrService.Core core) {
       try {
                SolrClient client = getClient(core);
                client.addBean(bean);
                client.commit();
            } catch (IOException e) {
                LOG.error("i/o error on solr", e);
            } catch (SolrServerException e) {
                LOG.error("solr server error", e);
            }
    }
}
