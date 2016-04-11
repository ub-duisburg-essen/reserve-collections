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
package unidue.rc.dao;


import org.apache.cayenne.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import unidue.rc.DbTestUtils;
import unidue.rc.model.Setting;

/**
 * Created with IntelliJ IDEA. User: mkoesters Date: 20.02.14 Time: 11:08 To change this template use File | Settings |
 * File Templates.
 */
public class TestSetting extends Assert {
    private static final Logger LOG = LoggerFactory.getLogger(TestSetting.class);


    private SettingDAO settingDAO;

    private DbTestUtils dbTestUtils;

    @BeforeClass
    public void setup() throws Exception {

        try {

            dbTestUtils = new DbTestUtils();
            dbTestUtils.setupdb();

            settingDAO = new SettingDAOImpl();


        } catch (DatabaseException | ValidationException e) {
            LOG.error("could not setup " + this.getClass().getSimpleName() + " tests: " + e.getMessage());
            throw e;
        }
    }

    @AfterClass
    public void shutdown() {

        dbTestUtils.shutdown();
    }

    @Test
    public void testGetSetting() {
        Setting setting =  settingDAO.getSetting("duepublico.url");
        assertTrue(setting.getKey().equals("duepublico.url"));
    }

    @Test
    public void testUpdateSetting() {
        Setting setting =  settingDAO.getSetting("duepublico.url");
        setting.setValue("test");
        try {
            settingDAO.update(setting);
        } catch (CommitException e) {
            e.printStackTrace();
        }
        Setting settingnew = settingDAO.getSetting("duepublico.url");
        assertTrue(setting.getValue().equals("test"));
    }

}
