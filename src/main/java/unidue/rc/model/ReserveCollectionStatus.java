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

import org.apache.cayenne.ExtendedEnumeration;

/**
 * @author Nils Verheyen
 * @since 23.10.13 11:58
 */
public enum ReserveCollectionStatus implements ExtendedEnumeration {
    NEW(1),
    ACTIVE(2),
    EXPIRED(3),
    DEACTIVATED(4),
    ARCHIVED(5);

    private final Integer dbValue;

    ReserveCollectionStatus(Integer dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public Object getDatabaseValue() {
        return dbValue;
    }

    public Integer getValue() {
        return dbValue;
    }
}
