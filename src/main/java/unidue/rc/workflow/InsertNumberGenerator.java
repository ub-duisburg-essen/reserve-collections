package unidue.rc.workflow;


import miless.model.User;
import org.apache.cayenne.query.NamedQuery;
import unidue.rc.dao.ReserveCollectionNumberDAO;
import unidue.rc.model.LibraryLocation;
import unidue.rc.model.ReserveCollectionNumber;

import java.util.List;

/**
 * An <code>InsertNumberGenerator</code> searches for the first unused number when generating new numbers for
 * {@linkplain unidue.rc.model.ReserveCollection} objects. See {@link NamedQuery} "select_numbers_by_location" in
 * cayennes modeler for query information.
 *
 * @author Nils Verheyen
 * @see <a href="http://redmine.ub.uni-due.de/projects/rc-refactoring/wiki/Nummernvergabe">Number assignment (de)</a>
 */
public class InsertNumberGenerator implements NumberGeneratorStrategy {

    private final ReserveCollectionNumberDAO numberDAO;

    public InsertNumberGenerator(ReserveCollectionNumberDAO dao) {
        this.numberDAO = dao;
    }

    @Override
    public ReserveCollectionNumber buildNumber(LibraryLocation location) {

        // find free numbers assigned for current location
        List<ReserveCollectionNumber> numbers = numberDAO.getFreeNumbers(location);

        ReserveCollectionNumber number = numbers != null && !numbers.isEmpty() ? numbers.get(0) : null;

        if (number != null) {

            return number;
        } else {

            NumberGeneratorStrategy generator = new AppendNumberGenerator(numberDAO);
            return generator.buildNumber(location);
        }
    }
}
