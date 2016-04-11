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
package unidue.rc.ui;


import org.apache.commons.configuration.Configuration;
import org.apache.tapestry5.Binding;
import org.apache.tapestry5.ioc.internal.util.TapestryException;
import unidue.rc.system.SystemConfigurationService;

import java.lang.annotation.Annotation;

/**
 * A <code>SystemConfigurationBinding</code> is able to load properties of a
 * apache commons {@link Configuration} by a given key.
 *
 * @author Nils Verheyen
 * @see <a
 *      href="http://commons.apache.org/proper/commons-configuration//overview.html#Using_Configuration">Apache Commons Index</a>
 */
public class SystemConfigurationBinding implements Binding {

    private SystemConfigurationService configService;

    private String key;

    public SystemConfigurationBinding(SystemConfigurationService configService, String key) {
        this.configService = configService;
        this.key = key;
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return null;
    }

    @Override
    public Object get() {
        return configService.getString(key);
    }

    @Override
    public void set(Object value) {
        throw new TapestryException("binding is read only", this, null);
    }

    @Override
    public boolean isInvariant() {
        return true;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Class getBindingType() {
        return String.class;
    }

}
