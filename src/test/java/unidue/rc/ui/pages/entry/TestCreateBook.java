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
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
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
import java.util.function.Predicate;

/**
 * Created by mkoesters on 27.03.14.
 */
public class TestCreateBook extends SeleniumTestCase {
    private static final Logger LOG = LoggerFactory
            .getLogger(TestCreateBook.class);

    private DbTestUtils dbTestUtils;
    //private ReserveCollection rc;

    private EntryDAOImpl entryDAO;

    @BeforeClass
    public void setup() throws Exception {
        LOG.info("running " + this.getClass().getName() + " tests");
        try {
            dbTestUtils = new DbTestUtils();
            dbTestUtils.setupdb();

            entryDAO = new EntryDAOImpl();

        } catch (DatabaseException | ValidationException e) {
            LOG.error("could not setup " + this.getClass().getSimpleName() + " tests: " + e.getMessage());
            throw e;
        }
    }

    private Wait createWait(Predicate<String> p, String s) {
        return new Wait() {
            @Override
            public boolean until() {
                return p.test(s);
            }
        };
    }

    private Wait waitFor(String locator) {
        return createWait(this::isElementPresent, locator);
    }


    @Test
    public void testCreateBookViaCatalogue() throws Exception {

        final ReserveCollection rc;
        try {
            rc = dbTestUtils.createMockReserveCollection("TestC");
            open("http://localhost:9091/entry/createbook/" + rc.getId());
            waitFor("id=bookSearch").wait("#bookSearch should be visible", 5000);

            typeInField("bookSearch", "Chemisches Feuerwerk");
            clickAndWait(SUBMIT);

            waitFor("name=chosenBookDocNumber").wait("chosenBookDocNumber should be visible", 5000);
            click("name=chosenBookDocNumber");
            clickAndWait(SUBMIT);

            waitFor("id=signature").wait("#signature should be visible", 5000);
            clickAndWait(SUBMIT);

            assertTrue(
                    isElementPresent(String.valueOf(getEntryID(rc)))
                    , "ERROR: Entry should be displayed.");


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
            return entries.get(0).getId();

        }
        return 0;
    }


    @AfterClass
    public void shutdown() {
        dbTestUtils.shutdown();
        LOG.info("Test of " + this.getClass().getName() + " done...");
    }


}
