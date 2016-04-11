package unidue.rc.dao;


import miless.model.User;
import org.apache.cayenne.BaseContext;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.SelectQuery;
import unidue.rc.model.Mail;
import unidue.rc.model.ReserveCollection;
import unidue.rc.model.Warning;
import unidue.rc.system.DateConvertUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by nils on 17.03.16.
 */
public class WarningDAOImpl extends BaseDAOImpl implements WarningDAO {

    @Override
    public Warning create(Mail mail, ReserveCollection collection, User user, LocalDate calculatedFor) throws CommitException {

        Warning warning = new Warning();
        warning.setMail(mail);
        warning.setReserveCollection(collection);
        warning.setUserID(user.getId());
        warning.setCalculatedFor(DateConvertUtils.asUtilDate(calculatedFor));

        create(warning);
        return warning;
    }

    @Override
    public List<Warning> getWarnings(Integer userID, ReserveCollection collection) {

        SelectQuery query = new SelectQuery(Warning.class);
        Expression qualifier = ExpressionFactory.matchExp(Warning.USER_ID_PROPERTY, userID)
                .andExp(ExpressionFactory.matchExp(Warning.RESERVE_COLLECTION_PROPERTY, collection));
        query.setQualifier(qualifier);

        ObjectContext context = BaseContext.getThreadObjectContext();

        List<Warning> warnings = context.performQuery(query);
        return warnings != null
               ? warnings
               : Collections.EMPTY_LIST;
    }
}
