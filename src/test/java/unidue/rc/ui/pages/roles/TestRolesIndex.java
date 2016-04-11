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
package unidue.rc.ui.pages.roles;


import com.thoughtworks.selenium.Wait;
import org.apache.cayenne.BaseContext;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.validation.ValidationException;
import org.apache.tapestry5.test.SeleniumTestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import unidue.rc.DbTestUtils;
import unidue.rc.dao.ActionDAO;
import unidue.rc.dao.ActionDAOImpl;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.RoleDAO;
import unidue.rc.dao.RoleDAOImpl;
import unidue.rc.model.Action;
import unidue.rc.model.Role;

import java.util.List;

public class TestRolesIndex extends SeleniumTestCase {

	private static final Logger LOG = LoggerFactory
			.getLogger(TestRolesIndex.class);

	private DbTestUtils dbTestUtils;
	private RoleDAO roleDAO;
	private Action action;
	private ActionDAO actionDAO;

	@BeforeClass
	public void setup() throws Exception {
		LOG.info("running " + this.getClass().getName() + " tests");

		try {
			dbTestUtils = new DbTestUtils();
			dbTestUtils.setupdb();
			dbTestUtils.writeDefaultValues();
			roleDAO = new RoleDAOImpl();

			actionDAO = new ActionDAOImpl();

			Role role = new Role();
			role.setName("Testrole");
			roleDAO.create(role);

			List<Role> roles = roleDAO.getRoles();

			for(Role rolex:roles) {
				LOG.debug("Role in DB: "+rolex.getName()+" - "+rolex.getId() + " - " + rolex.getIsDefault());

			}

			List<Action> actions = actionDAO.getActions();
			for(Action actionx : actions) {
				LOG.debug("Action  in DB: "+actionx.getName()+" - "+actionx.getId() + " - " + actionx.getResource());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testRoleSelectable() {
		open("/roles");

		new Wait() {

			@Override
			public boolean until() {
				return isElementPresent("id=availableActionsForm");
			}
		}.wait("role page should be visible", 5000);

		focus("//select[@id='roles']");
		click("//select[@id='roles']");

		new Wait() {

			@Override
			public boolean until() {
				return isTextPresent("Testrole");
			}
		}.wait("Testrole should be visible in Rolelist", 5000);

	}

	@Test(dependsOnMethods = { "testRoleSelectable" })
	public void testSelectRole() {
		focus("//select[@id='roles']");
		select("//select[@id='roles']", "Testrole");

		new Wait() {
			@Override
			public boolean until() {
				return isElementPresent("class=table");
			}
		}.wait("roleActionRelation table should be visible", 10000);
	}

	@Test(dependsOnMethods = { "testSelectRole" })
	public void testSetRelationship() {
		focus("//select[@name='availableActions']");
		select("//select[@name='availableActions']", "Semesterapparat erstellen");

		click("class=button addAction");

		new Wait() {

			@Override
			public boolean until() {
				return getText(
						"class=table")
						.contains("Semesterapparat erstellen");
			}
		}.wait("relationship 'Semesterapparat erstellen' should be set for role 'Testrole'",
				5000);
	}

	@AfterClass
	public void shutdown() {
		dbTestUtils.shutdown();
		LOG.info("Test of " + this.getClass().getName() + " done...");
	}

	public void createAction(Action action) throws CommitException {
		ObjectContext objectContext = BaseContext.getThreadObjectContext();
		objectContext.registerNewObject(action);

		try {
			objectContext.commitChanges();

			LOG.info("new action committed: " + action);
		} catch (ValidationException e) {
			LOG.error("could not persist entry: " + e.getMessage());
			objectContext.rollbackChanges();
			throw new CommitException("could not create entry " + action, e);
		}
	}

}
