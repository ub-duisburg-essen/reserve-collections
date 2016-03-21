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

import org.apache.commons.lang3.builder.ToStringBuilder;
import unidue.rc.model.auto._Participation;

public class Participation extends _Participation implements IntPrimaryKey, CollectionVisitable {

    @Override
    public Integer getId() {
        return (getObjectId() != null && !getObjectId().isTemporary())
               ? (Integer) getObjectId().getIdSnapshot().get(ID_PK_COLUMN)
               : null;
    }

    /**
     * Do NOT use this method to set the reserve collection id inside this participation. Use
     * {@link #setReserveCollection(ReserveCollection)} instead.
     *
     * @param collectionID see desc.
     * @throws UnsupportedOperationException everytime
     */
    @Override
    public void setCollectionID(Integer collectionID) {
        throw new UnsupportedOperationException("set of collection id is not allowed, use setReserveCollection instead");
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("userId", getUserId())
                .append("role", getRole())
                .append("collection", getReserveCollection())
                .toString();
    }

    @Override
    public void accept(CollectionVisitor visitor) {
        visitor.visit(this);
    }
}
