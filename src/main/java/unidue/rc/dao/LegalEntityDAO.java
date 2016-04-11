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


import miless.model.LegalEntity;

/**
 * A <code>LegalEntityDAO</code> should be used as default access object to load, update and delete {@link LegalEntity}
 * objects from backend.
 *
 * @author Nils Verheyen
 * @see LegalEntityXMLFileDAO
 */
public interface LegalEntityDAO {

    /**
     * Returns the {@linkplain LegalEntity} with target id or <code>null</code> if no entity could be found
     *
     * @param id id of the {@linkplain LegalEntity} to retrieve
     * @return the found {@linkplain LegalEntity} or <code>null</code> if it does not exist
     */
    LegalEntity getLegalEntityById(Integer id);
}
