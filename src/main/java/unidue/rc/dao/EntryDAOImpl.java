package unidue.rc.dao;


import org.apache.cayenne.BaseContext;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.PersistenceState;
import org.apache.cayenne.Persistent;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.SelectQuery;
import org.apache.cayenne.query.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unidue.rc.model.Entry;
import unidue.rc.model.EntryValue;
import unidue.rc.model.ReserveCollection;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


public class EntryDAOImpl extends BaseDAOImpl implements EntryDAO {
    private static final Logger LOG = LoggerFactory.getLogger(EntryDAOImpl.class);

    @Override
    public final Entry createEntry(ReserveCollection collection) throws CommitException {

        // create new entry to associated value with collection
        Entry entry = new Entry();
        entry.setReserveCollection(collection);

        // save entry
        create(entry);

        return entry;
    }

    @Override
    public final void createEntry(EntryValue value, ReserveCollection collection) throws CommitException {
        Entry entry = createEntry(collection);

        value.setEntry(entry);

        super.create(value);
    }

    @Override
    public List<Entry> getEntries(ReserveCollection rc) {
        SelectQuery query = new SelectQuery(Entry.class);

        query.setQualifier(ExpressionFactory.matchExp(Entry.RESERVE_COLLECTION_PROPERTY, rc));
        query.addOrdering(Entry.POSITION_PROPERTY, SortOrder.ASCENDING);

        ObjectContext objectContext = BaseContext.getThreadObjectContext();

        @SuppressWarnings("unchecked")
        List<Entry> result = objectContext.performQuery(query);

        return result != null ? result : Collections.<Entry>emptyList();

    }

    @Override
    public void move(Entry target, Entry base) throws CommitException {
        List<Entry> subEntries = base.getReserveCollection().getEntries()
                .stream()
                .filter(entry -> entry.getPosition() > base.getPosition())
                .collect(Collectors.toList());
        for (Entry e : subEntries) {
            e.moveDown();
            update(e);
        }
        target.setPosition(base.getPosition() + 1);
        update(target);
    }

    @Override
    public void create(Object o) throws CommitException {

        if (o instanceof Entry) {
            Entry entry = (Entry) o;
            ReserveCollection rc = entry.getReserveCollection();
            entry.setPosition(rc.getMaxEntryIndex() + 1);
            entry.setCreated(new Date());
            entry.setModified(new Date());
        }
        super.create(o);
    }

    @Override
    public void update(Object o) throws CommitException {

        if (o instanceof Entry) {
            Entry entry = (Entry) o;

            Persistent dataObject = entry.getValue();
            if (dataObject != null && dataObject.getPersistenceState() == PersistenceState.MODIFIED)
                entry.setModified(new Date());

        }
        super.update(o);
    }
}