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
package unidue.rc.model;


import com.fasterxml.jackson.annotation.JsonGetter;
import unidue.rc.model.auto._LibraryLocation;

import java.util.List;
import java.util.Objects;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LibraryLocation that = (LibraryLocation) o;
        return isPhysical() == that.isPhysical() &&
                Objects.equals(getId(), that.getId()) &&
                Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), isPhysical());
    }

    @Override
    public void accept(CollectionVisitor visitor) {
        visitor.visit(this);
    }
}
