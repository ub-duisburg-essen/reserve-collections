package unidue.rc.workflow;

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
import org.apache.cayenne.query.NamedQuery;
import unidue.rc.dao.ReserveCollectionNumberDAO;
import unidue.rc.model.LibraryLocation;
import unidue.rc.model.ReserveCollectionNumber;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;


/**
 * An <code>AppendNumberGenerator</code> searches for the last number that is not assigned when generating new numbers
 * for {@linkplain unidue.rc.model.ReserveCollection} objects. See {@link NamedQuery} "select_numbers_by_location" in
 * cayennes modeler for query information.
 *
 * @author Nils Verheyen
 * @see <a href="http://redmine.ub.uni-due.de/projects/rc-refactoring/wiki/Nummernvergabe">Number assignment (de)</a>
 */
public class AppendNumberGenerator implements NumberGeneratorStrategy {

    private final ReserveCollectionNumberDAO numberDAO;

    public AppendNumberGenerator(ReserveCollectionNumberDAO dao) {
        this.numberDAO = dao;
    }

    @Override
    public ReserveCollectionNumber buildNumber(LibraryLocation location) {
        // select all numbers, that are larger than the highest number for target location
        List<ReserveCollectionNumber> numberCandidates = numberDAO.getNumbersLargerThanMax(location);

        // filter higher numbers for a free number
        Optional<ReserveCollectionNumber> number = numberCandidates.stream()
                .filter(candidate -> candidate.isFree(location))
                .findFirst();

        // use first number free number if one is present
        ReserveCollectionNumber result;
        if (number.isPresent()) {

            result = number.get();
        } else {
            // otherwise a new number has to be created
            ReserveCollectionNumber highestNumber = numberDAO.getHighestNumber();

            Integer newNumberValue = highestNumber != null
                    ? highestNumber.getNumber() + 1
                    : 1;

            ReserveCollectionNumber newNumber = new ReserveCollectionNumber();
            newNumber.setNumber(newNumberValue);
            result = newNumber;
        }
        return result;
    }
}
