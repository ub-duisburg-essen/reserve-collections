/*
 * Copyright 2014 Universitaet Duisburg Essen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import unidue.rc.model.Participation;
import unidue.rc.model.ReserveCollection;
import unidue.rc.model.Role;

import java.util.List;

/**
 * A <code>ParticipationDAO</code> can be used to retrieve information regarding a {@link unidue.rc.model.Participation}
 * of a user inside a {@link unidue.rc.model.ReserveCollection}
 *
 * @author Nils Verheyen
 * @since 14.08.14 10:38
 */
public interface ParticipationDAO extends BaseDAO {

    String SERVICE_NAME = "ParticipationDAO";

    /**
     * Returns the {@link unidue.rc.model.Participation} under which {@link unidue.rc.model.Role} an {@link User}
     * accesses a {@link unidue.rc.model.ReserveCollection}.
     *
     * @param user       contains the user which maybe participates in target collection
     * @param collection collection of the participation
     * @return the {@link unidue.rc.model.Participation} of target {@link User} or <code>null</code> if it is not
     * participating the collection
     */
    Participation getActiveParticipation(User user, ReserveCollection collection);

    /**
     * Returns the {@link unidue.rc.model.Participation} under  an {@link User} with target id
     * accesses a {@link unidue.rc.model.ReserveCollection} with target id.
     *
     * @param userID       id of the user
     * @param collectionID id of the collection
     * @return the {@link unidue.rc.model.Participation} of target {@link User} or <code>null</code> if it is not
     * participating the collection
     */
    Participation getActiveParticipation(Integer userID, Integer collectionID);

    /**
     * Returns a {@link java.util.List} of active {@link unidue.rc.model.Participation} where the enddate has not been set under which {@link unidue.rc.model.Role} an {@link User}
     * accesses a {@link unidue.rc.model.ReserveCollection}.
     *
     * @param role       contains the role which maybe participates in target collection
     * @param collection collection of the participation
     * @return the {@link unidue.rc.model.Participation} of target {@link User} or <code>null</code> if it is not
     * participating the collection
     */
    List<Participation> getActiveParticipations(Role role, ReserveCollection collection);

    /**
     * Returns a {@link java.util.List} of active {@link unidue.rc.model.Participation}s of target user.
     *
     * @param user see description
     * @return all active participations for target user
     */
    List<Participation> getActiveParticipations(User user);

    /**
     * Returns a {@link java.util.List} of active {@link unidue.rc.model.Participation}s on target collection.
     *
     * @param collection see description
     * @return all active participations of given collection
     */
    List<Participation> getActiveParticipations(ReserveCollection collection);

    /**
     * Returns a {@link java.util.List} of active participations that users have on a list of collections with a
     * specific {@link Role}.
     *
     * @param collections collections that users participates.
     * @param role        role to use
     * @return all active participations or an empty list
     */
    List<Participation> getActiveParticipations(List<ReserveCollection> collections, Role role);

    /**
     * Creates a new {@link unidue.rc.model.Participation} with target user, collection and role under which it
     * participates.
     *
     * @param userID     the user for the participation
     * @param collection the collection under which the user participates
     * @param role       the role under which the user participates in the collection
     * @throws CommitException thrown if the participation could not be deleted
     */
    void createParticipation(Integer userID, ReserveCollection collection, Role role) throws CommitException;
}
