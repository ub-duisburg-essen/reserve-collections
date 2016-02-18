package unidue.rc.model.web;

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

/**
 * Created with IntelliJ IDEA. User: mkoesters Date: 08.11.13 Time: 08:52 To change this template use File | Settings |
 * File Templates.
 */

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Conversation<T> {
    private String id;
    private Map<Object, T> objectsByKey = null;

    public Conversation(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setObject(Object key, T obj) {
        if (objectsByKey == null) {
            objectsByKey = new HashMap<>(1);
        }
        objectsByKey.put(key, obj);
    }

    public T getObject(Object key) {
        return objectsByKey == null
                ? null
                : objectsByKey.get(key);
    }

    public String toString() {
        final String DIVIDER = ", ";

        StringBuilder buf = new StringBuilder();
        buf.append(this.getClass().getSimpleName() + ": ");
        buf.append("[");
        buf.append("id=" + id + DIVIDER);
        buf.append("objectsByKey=");
        if (objectsByKey == null) {
            buf.append("null");
        } else {
            buf.append("{");
            for (Iterator<Object> iterator = objectsByKey.keySet().iterator(); iterator.hasNext(); ) {
                Object key = (Object) iterator.next();
                buf.append("(" + key + "," + "<" + objectsByKey.get(key) == null ? "null" : objectsByKey.get(key)
                        .getClass()
                        .getSimpleName()
                        + ">" + ")");
            }
            buf.append("}");
        }
        buf.append("]");
        return buf.toString();
    }
}