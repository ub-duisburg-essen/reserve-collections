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
package unidue.rc.ui.valueencoder;

import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.services.ValueEncoderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by nils on 19.10.16.
 */
public class ClassNameValueEncoder implements ValueEncoder<Class>, ValueEncoderFactory<Class> {

    private static final Logger LOG = LoggerFactory.getLogger(ClassNameValueEncoder.class);

    @Override
    public String toClient(Class value) {
        return value.getName();
    }

    @Override
    public Class toValue(String clientValue) {
        try {
            return Class.forName(clientValue);
        } catch (ClassNotFoundException e) {
            LOG.error("could not load class " + clientValue, e);
            return null;
        }
    }

    @Override
    public ValueEncoder<Class> create(Class<Class> type) {
        return this;
    }
}
