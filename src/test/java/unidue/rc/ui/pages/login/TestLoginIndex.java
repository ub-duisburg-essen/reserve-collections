package unidue.rc.ui.pages.login;

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

import com.thoughtworks.selenium.Wait;
import org.apache.tapestry5.test.SeleniumTestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import unidue.rc.DbTestUtils;

/**
 * The <code>TestLoginIndex</code> runs selenium test cases on the page "login/Index". See also {@link
 * com.thoughtworks.selenium.Selenium} for locator documentation.
 *
 * @author Nils Verheyen
 */
public class TestLoginIndex extends SeleniumTestCase {

    private static final Logger LOG = LoggerFactory
                    .getLogger(TestLoginIndex.class);

            private DbTestUtils dbTestUtils;

            @BeforeClass
            public void setup() throws Exception {
                LOG.info("running " + this.getClass().getName() + " tests");

                try {
                    dbTestUtils = new DbTestUtils();
                    dbTestUtils.setupdb();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void loginPage() {

        open("/login");
        new Wait() {

            @Override
            public boolean until() {
                return isElementPresent("id=register");
            }
        }.wait("login page should be visible", 5000);


    }

    @Test
    public void selectRegister() {

        open("/login");

        new Wait() {

            @Override
            public boolean until() {
               return  isElementPresent("id=register");
            }
        }.wait("login page should be visible", 15000);

        click("id=register");
        new Wait() {

            @Override
            public boolean until() {
                return isElementPresent("id=registerForm");
            }
        }.wait("register form should be visible", 15000);
    }

    @Test(dependsOnMethods = {"selectRegister"})
    public void register() throws InterruptedException {
      //  waitForPageToLoad();
        typeInField("newUsername", "User");
        typeInField("newPassword", "pw");
        typeInField("passwordRepetition", "pw");
        typeInField("forename", "Forename");
        typeInField("surname", "Surname");
        //selectElement("origin", "4182");
        typeInField("mail", "user@mail.com");
        click(SUBMIT);

        new Wait() {

            @Override
            public boolean until() {
                return isElementPresent("class=t-error-single");
            }
        }.wait("error should be visible", 15000);

    }

    protected void typeInField(final String fieldId, String value) {
        type("id=" + fieldId, value);
    }

    @AfterClass
    public void shutdown() {
        dbTestUtils.shutdown();
        LOG.info("Test of " + this.getClass().getName() + " done...");
    }
}
