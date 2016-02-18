package unidue.rc.model;

/*
 * #%L
 * Semesterapparate
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 - 2015 Universitaet Duisburg Essen
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

import unidue.rc.model.auto._Permission;

public class Permission extends _Permission implements IntPrimaryKey{

    @Override
    public Integer getId() {
        return CayenneUtils.getID(this, ID_PK_COLUMN);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Permission that = (Permission) o;

        if (getInstanceID() != null ? !getInstanceID().equals(that.getInstanceID()) : that.getInstanceID() != null) return false;
        if (!getUserID().equals(that.getUserID())) return false;
        return getAction().equals(that.getAction());

    }

    @Override
    public int hashCode() {
        int result = getInstanceID() != null ? getInstanceID().hashCode() : 0;
        result = 31 * result + getUserID().hashCode();
        result = 31 * result + getAction().hashCode();
        return result;
    }
}
