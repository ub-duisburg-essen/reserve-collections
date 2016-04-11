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


import unidue.rc.model.Setting;

import java.util.Iterator;
import java.util.List;

/**
 * An instance of <code>SystemConfigurationService</code> can be used inside components and pages to get values from the
 * current system configuration.
 *
 * @author Nils Verheyen
 */
public interface SystemConfigurationService {

    /**
     * Returns the value specified by target key.
     *
     * @param key key of the config value
     * @return The value according to target key or <code>null</code> if it does not exist
     * @throws IllegalArgumentException thrown if the key is <code>null</code>
     */
    String getString(String key);

    /**
     * Returns the value specified by target key.
     *
     * @param key          key of the config value
     * @param defaultValue default value if the key could not be found
     * @return The value according to target key or target default if it does not exist
     */
    String getString(String key, String defaultValue);

    /**
     * Returns the value specified by target key as int.
     *
     * @param key key of the config value
     * @return The value according to target key or <code>null</code> if it does not exist
     * @throws IllegalArgumentException thrown if the key is <code>null</code>
     * @see org.apache.commons.configuration.Configuration#getInt(String)
     */
    Integer getInt(String key);

    /**
     * Returns the value specified by target key as long.
     *
     * @param key          key of the config value
     * @param defaultValue default value if the key could not be found
     * @return The value according to target key or target default if it does not exist
     * @throws IllegalArgumentException thrown if the key is <code>null</code>
     * @see org.apache.commons.configuration.Configuration#getLong(String)
     */
    Long getLong(String key, long defaultValue);

    /**
     * Returns the value specified by target key as int.
     *
     * @param key          key of the config value
     * @param defaultValue default value if the key could not be found
     * @return The value according to target key or target default if it does not exist
     * @throws IllegalArgumentException thrown if the key is <code>null</code>
     * @see org.apache.commons.configuration.Configuration#getInt(String, int)
     */
    int getInt(String key, int defaultValue);

    /**
     * Returns all properties that contains target string as prefix.
     *
     * @param prefix key prefix which values should be returned for
     * @return an {@code Iterator} with the keys of this configuration
     * @see org.apache.commons.configuration.Configuration#getKeys(String)
     */
    Iterator<String> getKeys(String prefix);

    /**
     * Returns <code>true</code> if this config contains a property with target key.
     *
     * @param key lookup key
     * @return <code>true</code> if this config contains the key, <code>false</code> otherwise
     */
    boolean contains(String key);

    /**
     * Returns an array of strings that belong to target key or an empty list if the key could not be found.
     *
     * @param key key of the config value
     * @return see description
     * @see org.apache.commons.configuration.PropertiesConfiguration
     */
    List<String> getStringArray(String key);

    /**
     * Returns the <code>boolean</code> value that belongs to target key by the use of
     * {@link org.apache.commons.configuration.PropertyConverter#toBoolean(Object)}.
     *
     * @param key key of the config value
     * @return see description
     */
    boolean getBoolean(String key);

    /**
     * Returns the <code>boolean</code> value that belongs to target key by the use of
     * {@link org.apache.commons.configuration.PropertyConverter#toBoolean(Object)}.
     *
     * @param key          key of the config value
     * @param defaultValue default value if the key could not be found
     * @return see description
     */
    boolean getBoolean(String key, boolean defaultValue);

    /**
     * Returns the {@link Setting} that belongs to target key, or <code>null</code> if it does not exist.
     *
     * @param key key of the config value
     * @return the setting if one could be found
     */
    Setting getSetting(String key);
}
