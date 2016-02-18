package unidue.rc.plugins.alephsync;

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

import org.quartz.Job;
import org.quartz.JobExecutionException;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.DeleteException;
import unidue.rc.model.ReserveCollection;

import java.sql.SQLException;

/**
 * Created by nils on 29.06.15.
 */
public interface AlephSynchronizer extends Job {

    /**
     * Synchronizes all {@link unidue.rc.model.Book} objects and according entries inside active reserve collections
     * with books that are loaned in aleph with the system data given inside the collection.
     *
     * @throws JobExecutionException thrown if any error occured during job synchronization
     */
    void syncAleph() throws JobExecutionException;

    /**
     * Synchronizes all books inside target reserve collection with alephs data.
     *
     * @param collection contains the collection that  should be synchronized
     * @throws SQLException    thrown if data could not be fetched for items used inside given collection
     * @throws CommitException thrown if items of given collection could not be saved
     * @throws DeleteException thrown if items of given collection could not be deleted
     * @see #syncAleph()
     */
    void syncCollection(ReserveCollection collection) throws SQLException, CommitException, DeleteException;
}
