package unidue.rc.dao;

/*
 * #%L
 * Semesterapparate
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 Universitaet Duisburg Essen
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

import miless.model.User;
import unidue.rc.model.*;

import java.time.LocalDate;
import java.util.List;

/**
 * A <code>ReserveCollectionDAO</code> should be used as default access object to load, update and delete {@link
 * ReserveCollection} objects from backend.
 *
 * @author Nils Verheyen
 * @see ReserveCollectionDAOImpl
 */
public interface ReserveCollectionDAO extends BaseDAO {

    String SERVICE_NAME = "ReserveCollectionDAO";

    /**
     * Returns a list with all {@link ReserveCollection} objects.
     *
     * @return a list with all reserve collections or an empty list.
     */
    List<ReserveCollection> getReserveCollections();

    /**
     * Returns a list with all {@link ReserveCollection} objects that match target location.
     *
     * @param location location for the collections
     * @return a list with all reserve collections by target location or an empty list.
     */
    List<ReserveCollection> getCollections(LibraryLocation location);

    /**
     * Returns a list of all reserve collections that target user participates and has permissions to perform target
     * action or an empty list if there are no such participations.
     *
     * @param user   see description
     * @param action see description
     * @return all evaluated collections or an empty list
     */
    List<ReserveCollection> getCollections(User user, ActionDefinition action);

    /**
     * Returns a list of collections that expire in a certain amount of days given by a base date. Collections
     * that contain a date to dissolve are excluded.
     *
     * @param baseDate            base operation date used to detect expiration
     * @param daysUntilExpiration days until expiration of the collection
     * @param status              status of the collection
     * @return a list with all collections that matches the conditions or an empty list.
     */
    List<ReserveCollection> getExpiringCollections(LocalDate baseDate, int daysUntilExpiration, ReserveCollectionStatus status);
}
