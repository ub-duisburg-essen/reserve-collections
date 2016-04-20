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


import unidue.rc.model.*;

import java.util.List;

/**
 * A <code>ScanJobDAO</code> should be used as default access object to load, update and delete {@link
 * unidue.rc.model.ScanJob} objects from backend.
 *
 * @author Nils Verheyen
 * @since 11.12.13 16:45
 */
public interface ScanJobDAO extends BaseDAO {

    String SERVICE_NAME = "ScanJobDAO";

    /**
     * Retrieves a {@link java.util.List} of all available {@link unidue.rc.model.ScanJob} objects in backend.
     *
     * @return a list with all jobs or an empty list
     */
    List<ScanJob> getJobs();

    /**
     * Returns the {@link unidue.rc.model.Scannable} that is assigned to target scan job.
     *
     * @param scanJob scan job of the item
     * @return returns the scannable object that the job is assigned to, <code>null</code> otherwise.
     * @see unidue.rc.model.JournalArticle
     * @see unidue.rc.model.BookChapter
     */
    Scannable getScannable(ScanJob scanJob);
}
