package unidue.rc.model;

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
