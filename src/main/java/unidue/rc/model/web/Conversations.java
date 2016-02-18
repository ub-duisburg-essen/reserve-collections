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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Conversations<T> {

    private Map<String, Integer> counters = new HashMap<>();
    private Map<String, Conversation<T>> conversations = new HashMap<>();

    public synchronized String startConversation(String conversationIdPrefix) {
        int conversationNumber = incrementCounter(conversationIdPrefix);
        String conversationId = conversationIdPrefix + Integer.toString(conversationNumber);

        startConversationForId(conversationId);

        return conversationId;
    }

    public synchronized void startConversationForId(String conversationId) {
        Conversation conversation = new Conversation(conversationId);
        add(conversation);
    }

    public void saveToConversation(String conversationId, Object key, T value) {
        Conversation conversation = get(conversationId);
        // Save a new reference to the object, just in case Tapestry cleans up the other one as we leave the page.
        T valueNewRef = value;
        conversation.setObject(key, valueNewRef);
    }

    public T restoreFromConversation(String conversationId, Object key) {
        Conversation<T> conversation = get(conversationId);
        return conversation == null
                ? null
                : conversation.getObject(key);
    }

    public void endConversation(String conversationId) {
        remove(conversationId);
    }

    public Collection<Conversation<T>> getAll() {
        return conversations.values();
    }

    public boolean isEmpty() {
        return conversations.isEmpty();
    }

    private synchronized void add(Conversation conversation) {
        if (conversations.containsKey(conversation.getId())) {
            throw new IllegalArgumentException("Conversation already exists. conversationId = " + conversation.getId());
        }
        conversations.put(conversation.getId(), conversation);
    }

    public Conversation<T> get(String conversationId) {
        return conversations.get(conversationId);
    }

    private void remove(String conversationId) {
        Object obj = conversations.remove(conversationId);
        if (obj == null) {
            throw new IllegalArgumentException("Conversation did not exist. conversationId = " + conversationId);
        }
    }

    public synchronized int incrementCounter(String counterKey) {

        if (counters == null) {
            counters = new HashMap<>(2);
        }

        Integer counterValue = counters.get(counterKey);

        if (counterValue == null) {
            counterValue = 1;
        } else {
            counterValue++;
        }

        counters.put(counterKey, counterValue);
        return counterValue;
    }

    public String toString() {
        final String DIVIDER = ", ";

        StringBuilder buf = new StringBuilder();
        buf.append(this.getClass().getSimpleName() + ": ");
        buf.append("[ ");
        buf.append("counters=");
        if (counters == null) {
            buf.append("null");
        } else {
            buf.append("{");
            for (Iterator<String> iterator = counters.keySet().iterator(); iterator.hasNext(); ) {
                String key = iterator.next();
                buf.append("(" + key + ", " + counters.get(key) + ")");
            }
            buf.append("}");
        }
        buf.append(DIVIDER);
        buf.append("conversations=");
        if (conversations == null) {
            buf.append("null");
        } else {
            buf.append("{");
            for (Iterator<String> iterator = conversations.keySet().iterator(); iterator.hasNext(); ) {
                String key = iterator.next();
                buf.append("(" + key + ", " + conversations.get(key) + ")");
            }
            buf.append("}");
        }
        buf.append("]");
        return buf.toString();
    }

}
