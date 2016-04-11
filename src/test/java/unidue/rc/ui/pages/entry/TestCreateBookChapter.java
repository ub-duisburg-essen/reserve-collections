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
package unidue.rc.ui.pages.entry;


import com.thoughtworks.selenium.Wait;
import org.apache.cayenne.validation.ValidationException;
import org.apache.tapestry5.test.SeleniumTestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import unidue.rc.DbTestUtils;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.DatabaseException;
import unidue.rc.dao.EntryDAOImpl;
import unidue.rc.model.Entry;
import unidue.rc.model.ReserveCollection;

import java.util.List;

/**
 * Created by mkoesters on 27.03.14.
 */
public class TestCreateBookChapter extends SeleniumTestCase {
    private static final Logger LOG = LoggerFactory
            .getLogger(TestCreateBookChapter.class);

    private DbTestUtils dbTestUtils;
    //private ReserveCollection rc;

    private EntryDAOImpl entryDAO;


    @BeforeClass
    public void setup() throws Exception {
        LOG.info("running " + this.getClass().getName() + " tests");

        try {
            dbTestUtils = new DbTestUtils();
            dbTestUtils.setupdb();
            dbTestUtils.createMockUser();
            entryDAO = new EntryDAOImpl();

        } catch (DatabaseException | ValidationException e) {
            LOG.error("could not setup " + this.getClass().getSimpleName() + " tests: " + e.getMessage());
            throw e;
        }
    }

    @Test
    public void testCreateBookChapterViaUrl() {
        final ReserveCollection rc;

        try {
            rc = dbTestUtils.createMockReserveCollection("TestA");

        open("/entry/createbookchapter/" + rc.getId());

        new Wait() {

            @Override
            public boolean until() {
                return isElementPresent("id=source_url");

            }
        }.wait("error source_url should be visible", 5000);

            click("id=source_url");

            clickAndWait(SUBMIT);

            typeInField("bookurl", "http://www.uni-due.de");

            clickAndWait(SUBMIT);

            new Wait() {
                @Override
                public boolean until() {
                    return isElementPresent("id=chaptertitle");

                }
            }.wait("error chaptertitle should be visible", 10000);

            typeInField("chaptertitle", "TestA");

            typeInField("booktitle", "TestbuchA");

            typeInField("bookplace", "TestOrtA");

            typeInField("bookpagesfrom", "100");

            typeInField("bookpagesto", "200");

            clickAndWait(SUBMIT);

            new Wait() {

                @Override
                public boolean until() {

                    return isElementPresent("id=" + getEntryID(rc));
                }

            }.wait("BookChapter should be visible", 5000);

        } catch (CommitException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCreateBookChapterViaRefNo() {
        final ReserveCollection rc;

        try {
            rc = dbTestUtils.createMockReserveCollection("TestB");
            open("/entry/createbookchapter/" + rc.getId());

            new Wait() {

                @Override
                public boolean until() {
                    return isElementPresent("id=source_refno");

                }
            }.wait("error source_refno should be visible", 5000);

            click("id=source_refno");

            clickAndWait(SUBMIT);
            typeInField("refno", "abc123");
            clickAndWait(SUBMIT);
            new Wait() {
                @Override
                public boolean until() {
                    return isElementPresent("id=chaptertitle");

                }
            }.wait("error chaptertitle should be visible", 10000);

            typeInField("chaptertitle", "TestB");

            typeInField("booktitle", "TestbuchB");

            typeInField("bookplace", "TestOrtB");

            typeInField("bookpagesfrom", "100");

            typeInField("bookpagesto", "200");

            clickAndWait(SUBMIT);

            new Wait() {

                @Override
                public boolean until() {


                    return isElementPresent("id=" + getEntryID(rc));
                }

            }.wait("BookChapter should be visible", 5000);
        } catch (CommitException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testCreateBookChapterViaCatalogue() throws InterruptedException {
        final ReserveCollection rc;
        try {

            rc = dbTestUtils.createMockReserveCollection("TestC");

            open("/entry/createbookchapter/" + rc.getId());

            new Wait() {

                @Override
                public boolean until() {
                return isElementPresent("id=source_catalogue");

            }
        }.wait("error source_catalogue should be visible", 10000);

        click("id=source_catalogue");

        clickAndWait(SUBMIT);

        new Wait() {
            @Override
            public boolean until() {
                return isElementPresent("id=bookSearch");

            }
        }.wait("error bookSearch should be visible", 5000);


        typeInField("bookSearch", "Test");

        clickAndWait(SUBMIT);


        new Wait() {
            @Override
            public boolean until() {
                return isElementPresent("id=radio");

            }
        }.wait("error radio should be visible", 10000);

        click("id=radio");

        clickAndWait(SUBMIT);

        new Wait() {
            @Override
            public boolean until() {
                return isElementPresent("id=chaptertitle");

            }
        }.wait("error chaptertitle should be visible", 5000);
        
        typeInField("chaptertitle","TestC");

        typeInField("bookpagesfrom", "100");

        typeInField("bookpagesto", "200");

        clickAndWait(SUBMIT);
        new Wait() {

            @Override
            public boolean until() {

                return isElementPresent("id=" + getEntryID(rc));
            }

        }.wait("BookChapter should be visible", 5000);
        } catch (CommitException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testCreateBookChapterViaFile() {
        final ReserveCollection rc;

        try {
            rc = dbTestUtils.createMockReserveCollection("TestD");
            open("/entry/createbookchapter/" + rc.getId());

            new Wait() {

                @Override
                public boolean until() {
                    return isElementPresent("id=source_file");

                }
            }.wait("error source_catalogue should be visible", 5000);

            click("id=source_file");

            clickAndWait(SUBMIT);

            new Wait() {
                @Override
                public boolean until() {
                    return isElementPresent("class=qq-upload-button");

                }
            }.wait("error qq-upload-button should be visible", 5000);

        } catch (CommitException e) {
            e.printStackTrace();
        }
    }

    protected void typeInField(final String fieldId, String value) {

        type("id=" + fieldId, value);
    }


    private int getEntryID(ReserveCollection rc) {
        List<Entry> entries = entryDAO.getEntries(rc);
        if (!entries.isEmpty()) {
            LOG.info("Entry  found " + entries.get(0).getId());
            return entries.get(0).getId();

        }
        LOG.info("Entry not found " + this.getClass().getName());
        return 0;

    }

    @AfterClass
    public void shutdown() {
        dbTestUtils.shutdown();
        LOG.info("Test of " + this.getClass().getName() + " done...");
    }

}
