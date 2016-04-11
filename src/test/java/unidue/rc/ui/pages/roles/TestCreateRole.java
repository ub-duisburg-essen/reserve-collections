package unidue.rc.ui.pages.roles;


import org.apache.tapestry5.test.SeleniumTestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import unidue.rc.DbTestUtils;

import com.thoughtworks.selenium.Wait;

public class TestCreateRole extends SeleniumTestCase {

	private static final Logger LOG = LoggerFactory
			.getLogger(TestCreateRole.class);

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
	public void testCreateRoleValidName() {
		open("/roles/createrole");
		waitForPageToLoad();
		type("//input[@id='roleName']", "Test");
		submit("//form[@id='create_role_form']");

		new Wait() {

			@Override
			public boolean until() {
				return isElementPresent("id=availableActionsForm");
			}
		}.wait("role page should be visible", 5000);

	}

	@Test
	public void testCreateRoleInvalidName() {
		open("/roles/createrole");
		waitForPageToLoad();
		type("//input[@type='text'][@id='roleName']", " ");
		submit("//form[@id='create_role_form']");

		new Wait() {

			@Override
			public boolean until() {
				return isElementPresent("class=t-error-single");
			}
		}.wait("error should be visible", 5000);
	}

	@AfterClass
	public void shutdown() {
		dbTestUtils.shutdown();
		LOG.info("Test of " + this.getClass().getName() + " done...");
	}
}
