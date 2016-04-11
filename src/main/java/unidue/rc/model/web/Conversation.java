package unidue.rc.model.web;


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