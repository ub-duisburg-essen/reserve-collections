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

import unidue.rc.model.LibraryLocation;
import unidue.rc.model.ReserveCollectionNumber;

import java.util.Collection;
import java.util.List;

/**
 * @author Nils Verheyen
 * @since 10.07.13 14:17
 */
public interface ReserveCollectionNumberDAO extends BaseDAO {

    /**
     * Returns all free numbers in range for target location or an empty collection if there are no free numbers.
     * <p>
     * See: <a href="http://redmine.ub.uni-due.de/projects/rc-refactoring/wiki/Nummernvergabe">Nummernvergabe</a>
     *
     * @param end      end index
     * @param start    starting index
     * @param location location for the numbers
     * @return see description
     */
    Collection<Integer> getFreeNumbers(Integer start, Integer end, LibraryLocation location);

    /**
     * <p> Checks if a {@link ReserveCollectionNumber} with target number as value can be used or not. A number is
     * considered as free if it meets the following aspects. </p> <ul> <li>There is no number with target value</li>
     * <li> When a number with target value exists it must: <ul> <li>... not be associated to any reserve collection
     * or</li> <li>... free in target {@link LibraryLocation}</li> </ul> </li> </ul>
     * See: <a href="http://redmine.ub.uni-due.de/projects/rc-refactoring/wiki/Nummernvergabe">Nummernvergabe</a>
     *
     * @param number   number candidate
     * @param location location for the number
     * @return <code>true</code> if the number is free
     */
    boolean isNumberFree(Integer number, LibraryLocation location);

    /**
     * Retrieves the {@link ReserveCollectionNumber} with target value
     *
     * @param number value of number to retrieve
     * @return the number with target value, or <code>null</code> if it does not exist
     */
    ReserveCollectionNumber getNumber(Integer number);

    /**
     * Returns a list of all numbers in db
     *
     * @return a list with all numbers or an empty list.
     */
    List<ReserveCollectionNumber> getNumbers();

    /**
     * Selects {@link ReserveCollectionNumber}s with values larger than the maximum number used inside target {@link
     * LibraryLocation}.
     *
     * @param location location to use for number retrieval
     * @return numbers with the higher values or <code>null</code> if there are not numbers
     */
    List<ReserveCollectionNumber> getNumbersLargerThanMax(LibraryLocation location);

    /**
     * Retrieves the highest number available in backend.
     *
     * @return the {@link ReserveCollectionNumber} with the highest value or <code>null</code> if it does not exist.
     */
    ReserveCollectionNumber getHighestNumber();

    /**
     * Creates a new {@link ReserveCollectionNumber} with target value.
     *
     * @param number int value to use for the number
     * @return the new created number
     * @throws CommitException if the number could not be saved
     */
    ReserveCollectionNumber create(Integer number) throws CommitException;

    /**
     * Creates target {@link ReserveCollectionNumber} in backend, if it does not exists. Otherwise it is just updated.
     *
     * @param number number which should be created or updated
     * @throws CommitException thrown if the number could not be saved
     */
    void createOrUpdate(ReserveCollectionNumber number) throws CommitException;

    /**
     * Selects all free {@link ReserveCollectionNumber} objects, that are free by definition.
     * See: <a href="http://redmine.ub.uni-due.de/projects/rc-refactoring/wiki/Nummernvergabe">Nummernvergabe</a>
     *
     * @param location contains the location to which the numbers should be checked for
     * @return a list of all free numbers
     * @see ReserveCollectionNumber#isFree(LibraryLocation)
     */
    List<ReserveCollectionNumber> getFreeNumbers(LibraryLocation location);
}
