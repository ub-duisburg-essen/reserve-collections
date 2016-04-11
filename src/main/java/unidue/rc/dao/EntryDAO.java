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


import unidue.rc.model.Entry;
import unidue.rc.model.EntryValue;
import unidue.rc.model.ReserveCollection;

import java.util.List;

/**
 * A <code>EntryDAO.java.java</code> should be used as default access object to load, update and delete {@link Entry}
 * objects from backend.
 *
 * @author Marcus Koesters
 * @see EntryDAOImpl
 */
public interface EntryDAO extends BaseDAO {

    String SERVICE_NAME = "EntryDAO";

    /**
     * Creates a new entry that connects the value to target collection.
     *
     * @param collection contains the reserve collection that target value should be associated to
     * @return the new created entry for given collection
     * @throws CommitException when one of the objects could not be created
     */
    Entry createEntry(ReserveCollection collection) throws CommitException;

    /**
     * Creates target {@link EntryValue} and a new {@link Entry} inside target {@link ReserveCollection}.
     *
     * @param value      value that should be used in target collection
     * @param collection collection which is used
     * @throws CommitException when one of the objects could not be created
     */
    void createEntry(EntryValue value, ReserveCollection collection) throws CommitException;

    /**
     * Returns a list with all {@link Entry} objects.
     *
     * @param rc the collection which entries should be returned
     * @return a list with all reserve collections or an empty list.
     */
    List<Entry> getEntries(ReserveCollection rc);

    /**
     * Moves target entry below base entry.
     *
     * @param target entry that should be moved
     * @param base   base where the target should be moved to
     * @throws CommitException thrown if one of the objects could not be saved
     */
    void move(Entry target, Entry base) throws CommitException;
}
