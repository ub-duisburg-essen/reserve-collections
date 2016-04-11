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
package unidue.rc.dao;


import unidue.rc.model.Migration;

import java.util.List;

/**
 * Created by nils on 06.07.15.
 */
public interface MigrationDAO extends BaseDAO {

    String SERVICE_NAME = "MigrationDAO";

    /**
     * Returns the {@link Migration} object that is associated with target document id, or <code>null</code> if
     * it does not exist.
     *
     * @param documentID document id of the migration
     * @return the migration or <code>null</code> if none was found
     */
    Migration getMigrationByDocID(String documentID);
}
