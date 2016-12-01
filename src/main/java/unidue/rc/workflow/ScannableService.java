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
import unidue.rc.dao.DeleteException;
import unidue.rc.model.Entry;
import unidue.rc.model.ReserveCollection;
import unidue.rc.model.Resource;
import unidue.rc.model.Scannable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.BiConsumer;

/**
 * @author Nils Verheyen
 * @since 05.12.13 09:25
 */
public interface ScannableService {

    /**
     * Creates an {@link Entry} and target {@link Scannable} in backend.
     *
     * @param scannable  scannable to create
     * @param collection collection for the scannable
     * @throws CommitException thrown if the entry and/or scannable could not be saved.
     */
    void create(Scannable scannable, ReserveCollection collection) throws CommitException;

    /**
     * Creates an {@link Entry} and target {@link Scannable} with given {@link Resource} in backend.
     *
     * @param scannable  scannable to create
     * @param collection collection for the scannable
     * @param resource   resource of the scannable
     * @throws CommitException thrown if the entry, scannable and/or resource could not be saved.
     */
    void create(Scannable scannable, ReserveCollection collection, Resource resource) throws CommitException;

    /**
     * Creates a new {@link Scannable}, {@link Entry} and {@link Resource} in backend with given fullTextURL that
     * is bound to target collection.
     *
     * @param scannable   scannable to create
     * @param collection  collection for the scannable
     * @param fullTextURL fulltext for the resource
     * @return the created {@link Resource}
     * @throws CommitException thrown if any object could not be saved in backend
     */
    Resource create(Scannable scannable, ReserveCollection collection, String fullTextURL) throws CommitException;

    /**
     * Creates a new {@link Scannable}, {@link Entry} and {@link Resource} in backend with given input and filename
     * that is bound to target collection.
     *
     * @param scannable  scannable to create
     * @param collection collection for the scannable
     * @param filename   filename for the file
     * @param input      input source
     * @return the created {@link Resource}
     * @throws CommitException thrown if any object could not be saved in backend
     * @throws IOException     thrown during save of the file
     */
    Resource create(Scannable scannable, ReserveCollection collection, String filename, InputStream input) throws CommitException, IOException;

    /**
     * Updates the {@link Resource} of given scannable and sets target full text url if it has changed. If the
     * scannable contains no resource one is created.
     *
     * @param scannable   scannable to update
     * @param fullTextURL fulltext for the resource
     * @return contains the updated or new created resource
     * @throws CommitException thrown if any object could not be saved in backend
     */
    Resource update(Scannable scannable, String fullTextURL) throws CommitException;

    /**
     * Updates the {@link Resource} of given scannable and sets target input and file name. If the scannable contains no
     * resource one is created.
     *
     * @param scannable scannable to create
     * @param filename  filename for the file
     * @param input     input source
     * @return contains the updated or new created resource
     * @throws CommitException thrown if any object could not be saved in backend
     * @throws IOException     thrown during save of the file
     */
    Resource update(Scannable scannable, String filename, InputStream input) throws CommitException, IOException;

    /**
     * Updates target scannable in backend
     *
     * @param scannable scannable to update
     * @throws CommitException thrown if any object could not be saved in backend
     */
    void update(Scannable scannable) throws CommitException;

    /**
     * Should be called after a {@link ReserveCollection} was updated. Any necessary operations regarding the update
     * of given collection are run via this call.
     *
     * @param collection collection that was update
     * @throws CommitException thrown if any object could not be saved in backend
     */
    void afterCollectionUpdate(ReserveCollection collection) throws CommitException;

    /**
     * Should be called before an {@link Entry} is deleted (not marked as {@link Entry#getDeleted()}).
     *
     * @param entry entry that is going to be deleted
     * @throws DeleteException thrown if any object could not be deleted in backend.
     */
    void beforeEntryDelete(Entry entry) throws DeleteException;

    /**
     * Should be called after an {@link Entry} was deleted (not marked as {@link Entry#getDeleted()}).
     *
     * @param entry entry that was deleted
     */
    void afterEntryDelete(Entry entry);

    /**
     * Should be called after an {@link Entry} was updated in backend.
     *
     * @param entry entry that was updated
     * @throws CommitException thrown if any object could not be saved in backend
     */
    void afterEntryUpdate(Entry entry) throws CommitException;

    /**
     * Creates a clone of target {@link Scannable} with all copied meta data. References to other objects are not
     * cloned. Cloning is currently only allowed inside the same {@link ReserveCollection}.
     *
     * @param scannable  scannable to duplicate
     * @param collection target collection
     * @param <T>        scannable to duplicate
     * @return the cloned scannable
     * @throws UnsupportedOperationException if the collection of given scannable and given collection do not match.
     */
    <T extends Scannable> T duplicate(T scannable, ReserveCollection collection);

    /**
     * Deletes the {@link java.io.File} that may be referenced through the {@link Resource} of target {@link Scannable}.
     * Additional workflow operations are called cause of deletion of the file.
     *
     * @param scannable scannable to use
     * @throws CommitException thrown if any object could not be saved in backend
     */
    void setFileDeleted(Scannable scannable) throws CommitException;

    /**
     * Removes all files that belong of any {@link Scannable} object, that is stored inside the database. Proceed with
     * care, as the operation can not be reversed! All files are permanently deleted. During delete a log file
     * is written configured through <code>scannable.file.delete.log</code>.
     *
     * @param authorizationCode      contains the code that must match, otherwise the delete can not be executed
     * @param updateProgressObserver observer that listens for progress updates
     * @return the log file that was written
     * @throws IllegalArgumentException thrown if given authorization code is invalid
     * @throws IOException              thrown if any error occurred during accessing the delete log
     */
    File deleteAllFiles(String authorizationCode,
                        BiConsumer<Integer, Integer> updateProgressObserver) throws IllegalArgumentException, IOException;
}
