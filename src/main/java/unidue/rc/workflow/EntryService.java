package unidue.rc.workflow;

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

import unidue.rc.dao.CommitException;
import unidue.rc.dao.DeleteException;
import unidue.rc.model.*;

/**
 * With an {@code EntryService} one is able to update and delete {@link Entry} objects and call appropriate services
 * that should be informed that an entry has been modified. Entries themselves should not be created directly, because
 * the logically belong to one {@link unidue.rc.model.EntryValue}. Therefore a specific service or dao should be used.
 *
 * @author Nils Verheyen
 * @see unidue.rc.workflow
 * @see unidue.rc.dao
 * @since 12.01.15 14:48
 */
public interface EntryService {

    /**
     * Deletes target entry. Note that a deleted entry is only marked as deleted and not removed from backend.
     *
     * @param entry entry to delete
     * @throws CommitException thrown if an error occured during the process.
     */
    void delete(Entry entry) throws CommitException;

    /**
     * Updates target entry in backend.
     *
     * @param entry entry to update
     * @throws CommitException thrown if an error occured during the process.
     */
    void update(Entry entry) throws CommitException;

    /**
     * Should be called after a {@link ReserveCollection} was updated in backend.
     *
     * @param collection collection that was update
     * @throws CommitException if any object could not be saved
     */
    void afterCollectionUpdate(ReserveCollection collection) throws CommitException;

    /**
     * Should be called before a {@link ReserveCollection} was deleted in backend.
     *
     * @param collection collection should be deleted
     * @throws DeleteException if any object could not be deleted
     */
    void beforeCollectionDelete(ReserveCollection collection) throws DeleteException;

    /**
     * Should be called before a {@link ReserveCollection} was deleted in backend.
     *
     * @param collection collection that was deleted
     */
    void afterCollectionDelete(ReserveCollection collection);
}
