/**
 * Copyright (C) 2014 - 2016 Universitaet Duisburg-Essen (semapp|uni-due.de)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package unidue.rc.model;


import java.util.Date;

/**
 * @author Nils Verheyen
 * @since 28.11.13 11:50
 */
public interface Scannable extends EntryValue, ResourceContainer, Cloneable {

    /**
     * Returns the id of this scannable object.
     *
     * @return the id of the object
     */
    Integer getId();

    /**
     * Returns <code>true</code> if content for this scannable is available.
     *
     * @return see description
     */
    Boolean isContentAvailable();

    /**
     * Returns the start index from where this scannable should be scanned.
     *
     * @return see description
     */
    String getPageStart();

    /**
     * Returns the end index to which position this scannable should be scanned.
     *
     * @return see description
     */
    String getPageEnd();

    /**
     * Returns the title of scannable, either a JournalArticle title or a BookChapter title.
     *
     * @return see description
     */
    String getTitle();

    /**
     * Returns the {@link unidue.rc.model.ScanJob} associated or <code>null</code> if there is none.
     *
     * @return see description
     */
    ScanJob getScanJob();

    /**
     * Returns the {@link ReserveCollection} that this object belongs to.
     *
     * @return see description
     */
    ReserveCollection getReserveCollection();

    /**
     * Returns the {@link Resource} to this scannable object, if one is present <code>null</code> otherwise.
     *
     * @return see description
     */
    Resource getResource();

    /**
     * Returns <code>true</code> if a file in a resource is available for this scannable and not deleted.
     *
     * @return see description
     * @see Resource#getFileDeleted()
     */
    boolean isFileAvailable();

    /**
     * Sets the {@link unidue.rc.model.Resource} of this object.
     *
     * @see unidue.rc.model.JournalArticle#setResource(Resource)
     * @see unidue.rc.model.BookChapter#setResource(Resource)
     */
    void setResource(Resource resource);

    /**
     * Sets the modified date of this scannable instance
     *
     * @param modified  date which is set
     */
    void setModified(Date modified);

    /**
     * Clones this scannable with all metadata, but does not preserve any relationship to other objects.
     *
     * @return see description
     */
    Scannable clone();
}
