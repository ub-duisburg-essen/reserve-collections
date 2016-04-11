package unidue.rc.model;


import com.fasterxml.jackson.annotation.JsonGetter;
import unidue.rc.model.auto._LibraryLocation;

import java.util.List;

public class LibraryLocation extends _LibraryLocation implements CollectionVisitable {

    private static final long serialVersionUID = 1L;

    @JsonGetter
    public Integer getId() {
        return (getObjectId() != null && !getObjectId().isTemporary())
                ? (Integer) getObjectId().getIdSnapshot().get(ID_PK_COLUMN)
                : null;
    }

    public boolean hasChildren() {
        List<LibraryLocation> childLocations = getChildLocations();
        return childLocations != null && !childLocations.isEmpty();
    }

    @JsonGetter
    @Override
    public String getName() {
        return super.getName();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof LibraryLocation))
            return false;

        LibraryLocation other = (LibraryLocation) obj;
        return objectId.equals(other.objectId);
    }

    @Override
    public int hashCode() {
        return objectId.hashCode();
    }

    @Override
    public void accept(CollectionVisitor visitor) {
        visitor.visit(this);
    }
}
