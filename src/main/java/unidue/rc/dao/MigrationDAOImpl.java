package unidue.rc.dao;


import org.apache.cayenne.BaseContext;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.SelectQuery;
import unidue.rc.model.Migration;

import java.util.Collections;
import java.util.List;

/**
 * Created by nils on 06.07.15.
 */
public class MigrationDAOImpl extends BaseDAOImpl implements MigrationDAO {

    @Override
    public Migration getMigrationByDocID(String documentID) {
        ObjectContext context = BaseContext.getThreadObjectContext();

        SelectQuery query = new SelectQuery(Migration.class);
        query.setQualifier(ExpressionFactory.matchExp(Migration.DOCUMENT_ID_PROPERTY, documentID));

        return (Migration) Cayenne.objectForQuery(context, query);
    }
}
