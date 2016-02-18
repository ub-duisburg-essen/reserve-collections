package unidue.rc.ui.pages.entry;

/*
 * #%L
 * Semesterapparate
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 - 2015 Universitaet Duisburg Essen
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


import org.apache.cayenne.validation.ValidationException;
import org.apache.tapestry5.test.SeleniumTestCase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
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

/**
 * Created by marcus.koesters on 27.02.15.
 */

   public class TestCreateHTML extends SeleniumTestCase {
        private static final Logger LOG = LoggerFactory
                .getLogger(TestCreateHTML.class);

        private DbTestUtils dbTestUtils;
        //private ReserveCollection rc;

        private EntryDAOImpl entryDAO;

        private WebDriver driver;


        @BeforeClass
        public void setup() throws Exception {
            LOG.info("running " + this.getClass().getName() + " tests");

            try {
                dbTestUtils = new DbTestUtils();
                dbTestUtils.setupdb();
                entryDAO = new EntryDAOImpl();
                DesiredCapabilities capabilities = DesiredCapabilities.htmlUnit();
                capabilities.setJavascriptEnabled(false);
                driver = new HtmlUnitDriver(capabilities);

            } catch (DatabaseException | ValidationException e) {
                LOG.error("could not setup " + this.getClass().getSimpleName() + " tests: " + e.getStackTrace());
                throw e;
            }
        }

    @Test
    public void testCreateHTML() {
        final ReserveCollection rc;
        try {
            rc = dbTestUtils.createMockReserveCollection("TestA");

            driver.get("http://localhost:9091/entry/createhtml/"+rc.getId());

            WebElement element = (new WebDriverWait(driver, 10))
                    .until(ExpectedConditions.presenceOfElementLocated(By.id("html")));

            element.sendKeys("TEST");

            element.submit();

            driver.get("http://localhost:9091/collection/view/"+ rc.getId());

            assertTrue(
                    ((HtmlUnitDriver) driver).findElementById(String.valueOf(getEntryID(rc))).isDisplayed()
                    ,"ERROR: HTML Entry should be displayed.");

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
        driver.close();
        dbTestUtils.shutdown();

        LOG.info("Test of " + this.getClass().getName() + " done...");
    }
}
