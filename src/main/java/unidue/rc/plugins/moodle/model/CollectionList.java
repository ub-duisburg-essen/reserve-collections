package unidue.rc.plugins.moodle.model;


import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Created by nils on 15.06.15.
 */
@Root(name = "collections")
public class CollectionList {

    @ElementList(required = false, empty = true, inline = true)
    private List<Collection> collections;

    public void setCollections(List<Collection> collections) {
        this.collections = collections;
    }
}
