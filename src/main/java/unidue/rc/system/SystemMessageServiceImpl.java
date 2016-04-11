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
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * Created by nils on 25.06.15.
 */
public class SystemMessageServiceImpl implements SystemMessageService {

    private final PropertiesConfiguration properties;

    public SystemMessageServiceImpl() throws ConfigurationException {
        this.properties = new PropertiesConfiguration(SystemMessageServiceImpl.class.getResource("/system.messages_de.properties"));
    }

    @Override
    public String get(String key) {
        return properties.getString(key);
    }
}
