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
import unidue.rc.model.*;

/**
 * @author Nils Verheyen
 * @since 04.12.14 10:45
 */
public interface ScanJobService {

    /**
     * Update target {@link ScanJob} in backend.
     *
     * @param scanJob  scanjob to update
     * @throws CommitException  thrown if the job could not be updated
     */
    void update(ScanJob scanJob) throws CommitException;

    /**
     * Should be called after a {@link Scannable} was created in backend.
     *
     * @param scannable scannable that was created
     * @throws CommitException thrown if any object regarding given scannable could not be saved
     */
    void onScannableCreated(Scannable scannable) throws CommitException;

    /**
     * Should be called if a {@link Scannable} was updated in backend.
     *
     * @param scannable scannable that was updated
     * @throws CommitException thrown if any object regarding given scannable could not be saved
     */
    void onScannableUpdated(Scannable scannable) throws CommitException;

    /**
     * Should be called <b>before</b> a {@link Scannable} is deleted in backend.
     *
     * @param scannable scannable is going to be deleted
     * @throws DeleteException thrown if any object regarding given scannable could not be deleted
     */
    void beforeScannableDelete(Scannable scannable) throws DeleteException;

    /**
     * Should be called <b>after</b> a {@link Scannable} was deleted in backend.
     *
     * @param scannable scannable that was deleted
     */
    void afterScannableDelete(Scannable scannable);

    /**
     * Should be called <b>after</b> a {@link ReserveCollection} was updated in backend.
     *
     * @param collection collection that was updated
     */
    void afterCollectionUpdate(ReserveCollection collection);

    /**
     * Should be called after a file of a resource of a scannable was deleted.
     *
     * @param scannable scannable that references the file
     */
    void afterFileDeleted(Scannable scannable);

    /**
     * Should be called after an {@link Entry} of a collection was updated.
     *
     * @param entry entry that was updated
     */
    void onEntryUpdated(Entry entry);

    /**
     * Returns <code>true</code> if a {@link ScanJob} is needed for target scannable. It does not check if
     * there is an existing scan job, but checks if a scan job is needed in general.
     *
     * @param scannable scannable which should be checked
     * @return <code>true</code> if a scan job is needed
     */
    boolean isScanJobNeeded(Scannable scannable);

    /**
     * <p>Validates the different states if a scan job must be created, finished or updated for target scannable.</p>
     * <ul>
     * <li>no scan job + scan job needed -&gt; create scan job
     * <li>scan job + scan job needed -&gt; update scan job meta
     * <li>scan job + scan job not needed -&gt; scan job done (update)
     * <li>no scan job + scan job not needed -&gt; nothing</li>
     * </ul>
     *
     * @param scannable scannable which should be checked
     */
    void checkScanJob(Scannable scannable);

    /**
     * Returns the barcode that must be used to upload a file for a scannable.
     *
     * @param scannable  scannable that should be used
     * @return the barcode that must be used for upload
     */
    String getUploadBarcodeContent(Scannable scannable);
}
