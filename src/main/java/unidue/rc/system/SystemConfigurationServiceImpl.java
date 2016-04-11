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
package unidue.rc.system;


import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.DatabaseConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import unidue.rc.dao.SettingDAO;
import unidue.rc.model.Setting;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Default implementation for {@link SystemConfigurationService}.
 *
 * @author Nils Verheyen
 * @see PropertiesConfiguration
 */
public class SystemConfigurationServiceImpl implements SystemConfigurationService {

    private final DatabaseConfiguration config;
    private final SettingDAO settingDAO;

    public SystemConfigurationServiceImpl(DatabaseConfiguration config, SettingDAO settingDAO) throws ConfigurationException {
        this.config = config;
        this.settingDAO = settingDAO;
    }

    public String getString(String key) {
        return config.getString(key);
    }

    @Override
    public String getString(String key, String defaultValue) {
        return config.getString(key, defaultValue);
    }

    @Override
    public Integer getInt(String key) {
        return config.getInt(key);
    }

    @Override
    public Long getLong(String key, long defaultValue) {
        return config.getLong(key, defaultValue);
    }

    @Override
    public int getInt(String key, int defaultValue) {
        return config.getInt(key, defaultValue);
    }

    @Override
    public Iterator<String> getKeys(String prefix) {
        return this.config.getKeys(prefix);
    }

    @Override
    public boolean contains(String key) {
        return config.containsKey(key);
    }

    @Override
    public List<String> getStringArray(String key) {
        return Arrays.asList(config.getStringArray(key));
    }

    @Override
    public boolean getBoolean(String key) {
        return config.getBoolean(key, false);
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        return config.getBoolean(key, defaultValue);
    }

    @Override
    public Setting getSetting(String key) {
        return settingDAO.getSetting(key);
    }
}
