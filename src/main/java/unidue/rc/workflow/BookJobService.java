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
package unidue.rc.workflow;


import unidue.rc.dao.CommitException;
import unidue.rc.model.*;

/**
 * With a {@code BookJobService} crud operations on {@link BookJob} objects should be performed. All necessary
 * operations regarding a book job are executed.
 *
 * @author Nils Verheyen
 * @since 04.12.14 11:00
 */
public interface BookJobService {

    /**
     * Should be called after a {@link Book} was created in backend.
     *
     * @param book book that was created
     * @throws CommitException thrown if any object regarding the book could not be saved
     */
    void onBookCreated(Book book) throws CommitException;

    /**
     * Should be called <b>after</b> a {@link Book} is updated in backend.
     *
     * @param book updated book
     */
    void onBookUpdated(Book book);

    /**
     * Should be called <b>before</b> a {@link Book} is deleted in backend.
     *
     * @param book book that should be deleted
     */
    void beforeBookDelete(Book book);

    /**
     * Should be called <b>after</b> a {@link Book} was deleted in backend.
     *
     * @param book book that was deleted
     */
    void afterBookDelete(Book book);

    /**
     * Should be called <b>after</b> a {@link ReserveCollection} was updated in backend.
     *
     * @param collection collection that was updated
     */
    void afterCollectionUpdate(ReserveCollection collection);

    /**
     * Should be called after an {@link Entry} of a collection was updated.
     *
     * @param entry entry that was updated
     */
    void afterEntryUpdate(Entry entry);

    /**
     * Returns <code>true</code> if a {@link BookJob} is needed for target book. There is no status check given
     * of existing book jobs.
     *
     * @param book book that should be checked
     * @return <code>true</code> if a book job is needed
     */
    boolean isBookJobNeeded(Book book);

    /**
     * <p>Validates the different states if a book job must be created, finished or updated for target book.</p>
     * <ul>
     * <li>no book job + book job needed -&gt; create book job</li>
     * <li>book job + book job needed -&gt; update book job</li>
     * <li>book job + book job not needed -&gt; finish book job (update)</li>
     * <li>no book job + book job not needed -&gt; nothing</li>
     * </ul>
     *
     * @param book book that should be checked
     */
    void checkBookJob(Book book);
}
