package unidue.rc.model;


import com.fasterxml.jackson.annotation.JsonGetter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import unidue.rc.model.auto._ReserveCollectionNumber;

import java.util.Optional;

public class ReserveCollectionNumber extends _ReserveCollectionNumber implements Comparable<ReserveCollectionNumber> {

    @Override
    public int compareTo(ReserveCollectionNumber o) {
        return this.getNumber().compareTo(o.getNumber());
    }

    /**
     * Returns <code>true</code> if a reserve collection is free according to
     * <a href="http://redmine.ub.uni-due.de/projects/rc-refactoring/wiki/Nummernvergabe">documentation</a>.
     * A number is free if it does not belong to target location and has no reference to an active {@link ReserveCollection}.
     *
     * @param location  location to use for the check
     * @return <code>true</code> if this number can be used for a reserve collection
     */
    public boolean isFree(LibraryLocation location) {

        // number is free if it is free in target location and not reserved by any user
        Optional<ReserveCollection> usedByCollection = getCollections().stream()
                // is number relevant for given location
                .filter(collection -> location.equals(collection.getLibraryLocation()))
                // filter for active collection
                .filter(collection -> collection.getStatus().equals(ReserveCollectionStatus.ACTIVE))
                .findAny();
        return !usedByCollection.isPresent();
    }

    @JsonGetter
    @Override
    public Integer getNumber() {
        return super.getNumber();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("number", getNumber())
                .toString();
    }
}
