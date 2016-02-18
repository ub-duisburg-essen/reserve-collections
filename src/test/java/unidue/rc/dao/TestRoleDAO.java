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

import org.apache.cayenne.BaseContext;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import unidue.rc.DbTestUtils;
import unidue.rc.model.Action;
import unidue.rc.model.ActionDefinition;
import unidue.rc.model.Role;

public class TestRoleDAO extends Assert {

    private static final Logger LOG = LoggerFactory
            .getLogger(TestRoleDAO.class);

    private RoleDAOImpl roleDAO;
    private DbTestUtils dbTestUtils;
    private Action action;

    @BeforeClass
    public void setup() throws Exception {

        LOG.info("running " + this.getClass().getName() + " tests");
        try {

            dbTestUtils = new DbTestUtils();
            dbTestUtils.setupdb();

            roleDAO = new RoleDAOImpl();

            action = new Action();
            action.setResource(ActionDefinition.CREATE_RESERVE_COLLECTION.getResource());
            action.setName(ActionDefinition.CREATE_RESERVE_COLLECTION.getName());
            createAction(action);

        } catch (DatabaseException | ValidationException e) {
            LOG.error("could not setup " + this.getClass().getSimpleName()
                    + " tests: " + e.getMessage());
            throw e;
        }
    }

    @Test
    public void testCreateRoleInvalidName() {
        Role role = new Role();
        try {
            roleDAO.create(role);
        } catch (Exception e) {
            assertTrue(e instanceof ValidationException);
        }
    }

    @Test
    public void testCreateRoleValidName() throws CommitException {
        Role role = new Role();
        role.setName("Test");
        roleDAO.create(role);
    }

    @Test(dependsOnMethods = { "testCreateRoleValidName" })
    public void testgetActions() throws CommitException {
        Role role = roleDAO.getRoles().get(0);
        role.addToActions(action);
        roleDAO.update(role);
        assertTrue(roleDAO.getActions(role).size() > 0);
    }

    @Test(dependsOnMethods = { "testCreateRoleValidName" })
    public void testUpdateRole() throws CommitException {
        Role role = roleDAO.getRoles().get(0);
        role.setName("Update");
        roleDAO.update(role);
        Role update = roleDAO.getRoles().get(0);
        assertTrue(update.getName().equals("Update"));
    }

    @Test(dependsOnMethods = { "testCreateRoleValidName" })
    public void testDeleteExistingRole() {
        Role delete = roleDAO.getRoles().get(0);
        try {
            roleDAO.delete(delete);
        } catch (DeleteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        assertFalse(roleDAO.getRoles().get(0) == null);
    }

    @Test(dependsOnMethods = { "testCreateRoleValidName" })
    public void testDeleteNonExistingRole() {
        Role notExists = new Role();
        try {
            // not existent role should just be garbage collected
            roleDAO.delete(notExists);
            assertTrue(true);
        } catch (DeleteException e) {
            assertTrue(e instanceof DeleteException);
        }
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
