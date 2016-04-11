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


import miless.model.User;
import org.apache.commons.configuration.ConfigurationException;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.DeleteException;
import unidue.rc.dao.NumberAssignedException;
import unidue.rc.model.*;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Nils Verheyen
 * @since 05.12.13 09:18
 */
public interface CollectionService {

    /**
     * Stores target {@link ReserveCollection} in backend.
     *
     * @param collection {@linkplain ReserveCollection} to be saved.
     * @throws CommitException thrown if any value inside given collection is invalid. take a look at the modeler to see
     *                         required values.
     */
    void create(ReserveCollection collection) throws CommitException;

    /**
     * Updates target {@link ReserveCollection} inside backend.
     *
     * @param collection {@link ReserveCollection} which was modified.
     * @throws CommitException thrown if any value inside given collection is invalid. take a look at the modeler to see
     *                         required values.
     */
    void update(ReserveCollection collection) throws CommitException;

    /**
     * Renews target {@linkplain ReserveCollection} in backend.
     *
     * @param collection {@linkplain ReserveCollection} to renew.
     * @throws unidue.rc.dao.CommitException thrown if the collection could not be renewed
     */
    void renew(ReserveCollection collection) throws CommitException;

    /**
     * Prolongs target {@linkplain ReserveCollection} in backend.
     *
     * @param collection collection to prolong
     * @param code       code to use for prolong
     * @param to         new valid to date
     * @throws unidue.rc.dao.CommitException thrown if the collection could not be prolonged
     * @throws IllegalStateException         thrown if the code has already been used
     * @throws IllegalArgumentException      thrown if the code does not match the prolong code inside given collection
     * @throws ConfigurationException        thrown if target collection could not be prolongend due to misconfiguration
     */
    void prolong(ReserveCollection collection, String code, Date to) throws CommitException, IllegalStateException,
            IllegalArgumentException, ConfigurationException;

    /**
     * Activates target {@linkplain ReserveCollection} in backend.
     *
     * @param collection {@linkplain ReserveCollection} to activate.
     * @throws unidue.rc.dao.CommitException thrown if the collection could not be activated
     */
    void activate(ReserveCollection collection) throws CommitException;

    /**
     * Activates target {@linkplain ReserveCollection} in backend and tries to use target number.
     *
     * @param collection {@linkplain ReserveCollection} to activate.
     * @param number     number to use for activation.
     * @throws unidue.rc.dao.CommitException         thrown if the collection could not be saved
     * @throws unidue.rc.dao.NumberAssignedException thrown if the number can not be used
     */
    void activate(ReserveCollection collection, Integer number) throws CommitException, NumberAssignedException;

    /**
     * Deactivates target {@linkplain ReserveCollection} in backend.
     *
     * @param collection {@linkplain ReserveCollection} to deactivate.
     * @throws unidue.rc.dao.CommitException thrown if the collection could not be deactivated
     */
    void deactivate(ReserveCollection collection) throws CommitException;

    /**
     * Archives target {@linkplain ReserveCollection} in backend.
     *
     * @param collection {@linkplain ReserveCollection} to archive.
     * @throws unidue.rc.dao.CommitException thrown if the collection could not be archived
     */
    void archive(ReserveCollection collection) throws CommitException;

    /**
     * Deletes target {@linkplain ReserveCollection} in backend
     *
     * @param collection {@linkplain ReserveCollection} that should be deleted.
     * @throws unidue.rc.dao.DeleteException thrown if the collection could not be deleted
     */
    void delete(ReserveCollection collection) throws DeleteException;

    /**
     * Should be called after a {@link LibraryLocation} was updated.
     *
     * @param location location that was updated
     * @throws CommitException if any object could not be saved
     * @see unidue.rc.workflow.LibraryLocationService#update(unidue.rc.model.LibraryLocation)
     */
    void afterLocationUpdate(LibraryLocation location) throws CommitException;

    /**
     * Should be called after a {@link LibraryLocation} was deleted.
     *
     * @param location location that is going to be deleted
     * @throws DeleteException if any object could not be deleted
     * @see unidue.rc.workflow.LibraryLocationService#delete(unidue.rc.model.LibraryLocation)
     */
    void beforeLocationDelete(LibraryLocation location) throws DeleteException;

    /**
     * Should be called after a {@link LibraryLocation} was deleted.
     *
     * @param location location that was deleted
     * @see unidue.rc.workflow.LibraryLocationService#delete(unidue.rc.model.LibraryLocation)
     */
    void afterLocationDelete(LibraryLocation location);

    /**
     * Returns <code>true</code> if target access key matches the read or write key of a {@link ReserveCollection},
     * <code>false</code> otherwise.
     *
     * @param accessKey  entered access key
     * @param collection collection to use
     * @return <code>true</code> if the key is valid
     */
    boolean isAccessKeyValid(String accessKey, ReserveCollection collection);

    /**
     * Creates a new {@link unidue.rc.model.Participation} for target user and reserve collection that is appropriate
     * for target access key. If there is an existing participation it is ended. Also adds necessary objects
     * along the participation.
     *
     * @param user       user that participates the collection
     * @param collection collection to use
     * @param accessKey  access key that was entered
     * @throws DeleteException thrown if there was an active participation that could not be ended
     * @throws CommitException thrown if the new participation could not be created
     */
    void createParticipation(User user, ReserveCollection collection, String accessKey) throws CommitException, DeleteException;

    /**
     * Creates a new {@link unidue.rc.model.Participation} for target user and reserve collection and a given role.
     * If there is an existing participation it is ended. Also adds necessary objects along the participation.
     *
     * @param user       user that participates the collection
     * @param collection collection to use
     * @param role       role for the participation
     * @throws DeleteException thrown if there was an active participation that could not be ended
     * @throws CommitException thrown if the new participation could not be created
     */
    void createParticipation(User user, ReserveCollection collection, Role role) throws CommitException, DeleteException;

    /**
     * Ends target {@link unidue.rc.model.Participation} and calls all necessary related services.
     *
     * @param participation participation that should be ended
     * @throws CommitException if any object could not be saved
     * @throws DeleteException if any object could not be deleted
     */
    void endParticipation(Participation participation) throws CommitException, DeleteException;

    /**
     * Returns <code>true</code> if target {@link Participation} can be removed for the current user, <code>false</code>
     * otherwise.
     *
     * @param participation participation that should be ended
     * @return <code>true</code> if ending is possible
     */
    boolean isParticipationEndingAllowed(Participation participation);

    /**
     * Returns a map with all calendar values until a reserve collection expires and their configuration key. This
     * must be a map with a size of three (lecture end, semester end, no end).
     *
     * @return map with all configured expiration dates
     * @throws ConfigurationException if any value is mis configured
     */
    Map<Calendar, String> getCollectionExpiryDates() throws ConfigurationException;

    /**
     * Returns a map with all calendar values that can be used to prolong a colection. This
     * must be a map with a size of two (lecture end, semester end).
     *
     * @return map with all possible prolong dates
     * @throws ConfigurationException if any value is mis configured
     */
    Map<Calendar, String> getCollectionProlongDates() throws ConfigurationException;

    /**
     * Returns the current lecture end date according to system time if one is given, <code>null</code> otherwise
     *
     * @return current lecture end
     * @throws ConfigurationException if any value is mis configured
     */
    Calendar getLectureEnd() throws ConfigurationException;

    /**
     * Returns the current semester end date according to system time if one is given, <code>null</code> otherwise
     *
     * @return current semester end
     * @throws ConfigurationException if any value is mis configured
     */
    Calendar getSemesterEnd() throws ConfigurationException;

    /**
     * Returns the next lecture end that is after the current lecture end or <code>null</code> if there
     * is no next lecture end.
     *
     * @return next lecture end
     * @throws ConfigurationException thrown if one of the lecture end dates could not be loaded from config
     */
    Calendar getNextLectureEnd() throws ConfigurationException;

    /**
     * Returns the next semester end that is after the current lecture end or <code>null</code> if there
     * is no next semester end.
     *
     * @return next semester end
     * @throws ConfigurationException thrown if one of the lecture end dates could not be loaded from config
     */
    Calendar getNextSemesterEnd() throws ConfigurationException;

    /**
     * Returns a list of all real names of users that have a active docent participation in target collection.
     *
     * @param collection collection to use
     * @return a list of all docent names
     */
    List<String> getDocents(ReserveCollection collection);

    /**
     * Builds a new {@link ReserveCollectionNumber} for a {@link ReserveCollection} that can be used inside
     * target {@link LibraryLocation}. The build number can be an existing number that is usable or a new number.
     *
     * @param location location to build the number for
     * @return the new created number
     * @throws CommitException thrown if a creation of new number failed
     */
    ReserveCollectionNumber buildNumber(LibraryLocation location) throws CommitException;

    /**
     * Returns <code>true</code> if target {@link ReserveCollection} is nearly its expiration date. A collection
     * is expiring when its validation date reaches the current date minus the configured days in
     * <code>days.until.first.warning</code> or <code>days.until.second.warning</code>.
     *
     * @param collection collection to check
     * @return <code>true</code> if the collection is expiring
     */
    boolean isCollectionExpiring(ReserveCollection collection);

    /**
     * Returns <code>true</code> if target {@link ReserveCollection} can be prolonged.
     *
     * @param collection collection to check
     * @return <code>true</code> if the collection is prolongable
     */
    boolean isCollectionProlongable(ReserveCollection collection);
}
