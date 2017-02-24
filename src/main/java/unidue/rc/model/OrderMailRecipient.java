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

import unidue.rc.model.auto._OrderMailRecipient;

import java.util.Objects;

public class OrderMailRecipient extends _OrderMailRecipient implements IntPrimaryKey {

    @Override
    public Integer getId() {
        return (getObjectId() != null && !getObjectId().isTemporary())
               ? (Integer) getObjectId().getIdSnapshot().get(ID_PK_COLUMN)
               : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderMailRecipient that = (OrderMailRecipient) o;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getMail(), that.getMail()) &&
                Objects.equals(getLocation(), that.getLocation());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getMail(), getLocation());
    }
}
