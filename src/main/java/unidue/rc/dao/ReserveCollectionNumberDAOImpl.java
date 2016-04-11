package unidue.rc.dao;


import org.apache.cayenne.BaseContext;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.PersistenceState;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.NamedQuery;
import org.apache.cayenne.query.SelectQuery;
import org.apache.cayenne.query.SortOrder;
import org.apache.cayenne.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unidue.rc.model.LibraryLocation;
import unidue.rc.model.ReserveCollectionNumber;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Nils Verheyen
 * @since 10.07.13 14:17
 */
public class ReserveCollectionNumberDAOImpl extends BaseDAOImpl implements ReserveCollectionNumberDAO {

    private static final Logger LOG = LoggerFactory.getLogger(ReserveCollectionNumberDAOImpl.class);

    @Override
    public Collection<Integer> getFreeNumbers(Integer start, Integer end, LibraryLocation location) {

        ObjectContext context = BaseContext.getThreadObjectContext();

        SelectQuery query = new SelectQuery(ReserveCollectionNumber.class);
        query.setQualifier(ExpressionFactory.greaterOrEqualExp(ReserveCollectionNumber.NUMBER_PROPERTY, start)
                .andExp(ExpressionFactory.lessOrEqualExp(ReserveCollectionNumber.NUMBER_PROPERTY, end)));

        List<ReserveCollectionNumber> numbersInDB = context.performQuery(query);
        List<Integer> usedNumbersInDB = numbersInDB.stream()
                .filter(number -> !number.isFree(location))
                .map(number -> number.getNumber())
                .collect(Collectors.toList());


        return IntStream.range(start, end + 1)
                .filter(numberCandidate -> !usedNumbersInDB.contains(numberCandidate))
                .sorted()
                .boxed()
                .collect(Collectors.toList());
    }

    @Override
    public boolean isNumberFree(Integer number, LibraryLocation location) {

        ReserveCollectionNumber _number = (ReserveCollectionNumber) Cayenne.objectForPK(BaseContext
                .getThreadObjectContext(), ReserveCollectionNumber.class, number);

        // number is free if it is not present or free in target location
        return _number == null || _number.isFree(location);
    }

    @Override
    public ReserveCollectionNumber getNumber(Integer number) {
        return Cayenne.objectForPK(BaseContext.getThreadObjectContext(), ReserveCollectionNumber.class, number);
    }

    @Override
    public List<ReserveCollectionNumber> getNumbers() {
        ObjectContext context = BaseContext.getThreadObjectContext();

        SelectQuery query = new SelectQuery(ReserveCollectionNumber.class);

        List<ReserveCollectionNumber> numbers = context.performQuery(query);

        return numbers == null ? Collections.<ReserveCollectionNumber>emptyList() : numbers;
    }

    @Override
    public List<ReserveCollectionNumber> getNumbersLargerThanMax(LibraryLocation location) {

        ObjectContext context = BaseContext.getThreadObjectContext();

        String[] keys = new String[]{"locationId"};
        Object[] values = new Object[]{location.getId()};
        NamedQuery q = new NamedQuery("select_larger_than_max_numbers_by_location", keys, values);

        List<ReserveCollectionNumber> numbers = context.performQuery(q);

        return numbers;
    }

    @Override
    public ReserveCollectionNumber getHighestNumber() {

        ObjectContext context = BaseContext.getThreadObjectContext();
        SelectQuery query = new SelectQuery(ReserveCollectionNumber.class);

        query.addOrdering(ReserveCollectionNumber.NUMBER_PROPERTY, SortOrder.DESCENDING);

        query.setFetchLimit(1);

        List<ReserveCollectionNumber> l = context.performQuery(query);

        return l.size() > 0 ? l.get(0) : null;
    }

    @Override
    public List<ReserveCollectionNumber> getFreeNumbers(LibraryLocation location) {
        ObjectContext context = BaseContext.getThreadObjectContext();

        NamedQuery q = new NamedQuery("select_free_numbers_by_location_ordered_by_number_asc",
                Collections.singletonMap("locationId", location.getId()));

        // find free numbers assigned for current location
        List<ReserveCollectionNumber> numbers = context.performQuery(q);

        return numbers;
    }

    @Override
    public ReserveCollectionNumber create(Integer value) throws CommitException {

        ObjectContext context = BaseContext.getThreadObjectContext();

        // create new number
        ReserveCollectionNumber number = new ReserveCollectionNumber();

        // set value
        number.setNumber(value);

        // commit to backend
        context.registerNewObject(number);
        try {
            // commit to backend
            context.commitChanges();
        } catch (ValidationException e) {
            throw new CommitException("could not create number " + number + " - " + e.getMessage(), e);
        }
        return number;
    }

    @Override
    public void createOrUpdate(ReserveCollectionNumber number) throws CommitException {

        ObjectContext context = BaseContext.getThreadObjectContext();

        // register new object if needed
        if (number.getPersistenceState() == PersistenceState.NEW)
            context.registerNewObject(number);

        try {
            // commit to backend
            context.commitChanges();
        } catch (ValidationException e) {
            throw new CommitException("could not create number " + number + " - " + e.getMessage(), e);
        }
    }
}
