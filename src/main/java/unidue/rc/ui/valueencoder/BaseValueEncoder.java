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
import unidue.rc.dao.BaseDAO;
import unidue.rc.model.IntPrimaryKey;

/**
 * @author Nils Verheyen
 * @since 29.08.13 11:15
 */
public class BaseValueEncoder<T extends IntPrimaryKey> implements ValueEncoder<T>, ValueEncoderFactory<T> {

    private final BaseDAO dao;
    private final Class<T> clazz;

    public BaseValueEncoder(Class<T> clazz, BaseDAO dao) {
        this.clazz = clazz;
        this.dao = dao;
    }

    @Override
    public String toClient(T value) {
        return value.getId().toString();
    }

    @Override
    public T toValue(String clientValue) {
        return dao.get(clazz, Integer.valueOf(clientValue));
    }

    @Override
    public ValueEncoder<T> create(Class<T> type) {
        return this;
    }
}
