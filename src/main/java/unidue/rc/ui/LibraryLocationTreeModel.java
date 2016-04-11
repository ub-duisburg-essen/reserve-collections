package unidue.rc.ui;


import org.apache.tapestry5.tree.TreeModelAdapter;
import unidue.rc.model.LibraryLocation;

import java.util.List;

/**
 * @author Nils Verheyen
 * @since 29.11.13 13:47
 */
public class LibraryLocationTreeModel implements TreeModelAdapter<LibraryLocation> {

    @Override
    public boolean isLeaf(LibraryLocation value) {
        return !value.hasChildren();
    }

    @Override
    public boolean hasChildren(LibraryLocation value) {
        return value.hasChildren();
    }

    @Override
    public List<LibraryLocation> getChildren(LibraryLocation value) {
        return value.getChildLocations();
    }

    @Override
    public String getLabel(LibraryLocation value) {
        return value.getName();
    }
}
