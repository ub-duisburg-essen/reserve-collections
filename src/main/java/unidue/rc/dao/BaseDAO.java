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


import org.apache.cayenne.Persistent;

import java.util.List;

/**
 * @author Nils Verheyen
 * @since 12.03.14 11:18
 */
public interface BaseDAO {

    String SERVICE_NAME = "BaseDAO";

    int MAX_RESULTS = 200;

    /**
     * Stores target object of this application data model in backend.
     *
     * @param o object to be saved in backend.
     * @throws CommitException thrown if any value inside given collection is invalid. take a look at the modeler to see
     *                         required values.
     * @see unidue.rc.model
     */
    void create(Object o) throws CommitException;

    /**
     * Updates target object inside backend.
     *
     * @param o object which was modified.
     * @throws CommitException thrown if any value inside given object is invalid. take a look at the modeler to see
     *                         required values.
     */
    void update(Object o) throws CommitException;

    /**
     * Deletes target object from backend.
     *
     * @param object object that should be deleted
     * @throws DeleteException when target object could not be deleted.
     */
    void delete(Persistent object) throws DeleteException;

    /**
     * Returns the object which got target class and target id.
     *
     * @param className the class object of the object which should be returned
     * @param id        the int primary key of target object
     * @param <T>       class
     * @return the object with target id and class if it could be found, <code>null</code> otherwise
     */
    <T> T get(Class<T> className, Integer id);

    /**
     * Executes a paging request on objects with target class between target offset and max result. Note that max
     * results must not be more than {@link #MAX_RESULTS}.
     *
     * @param className  the class object of the object which should be returned
     * @param offset     the offset for the paging request
     * @param maxResults max results to return.
     * @param <T>        class
     * @return all found objects or an empty list
     */
    <T> List<T> get(Class<T> className, int offset, int maxResults);

}
