package unidue.rc.dao;


import org.apache.cayenne.BaseContext;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.SelectQuery;
import unidue.rc.model.Mail;

import java.util.Collections;
import java.util.List;

/**
 * Created by nils on 07.07.15.
 */
public class MailDAOImpl extends BaseDAOImpl implements MailDAO {
    @Override
    public List<Mail> getUnsendMails() {
        ObjectContext context = BaseContext.getThreadObjectContext();

        SelectQuery query = new SelectQuery(Mail.class);
        query.setQualifier(ExpressionFactory.matchExp(Mail.SEND_PROPERTY, Boolean.FALSE)
                .orExp(ExpressionFactory.matchExp(Mail.SEND_PROPERTY, null)));

        List<Mail> unsendMails = context.performQuery(query);
        return unsendMails != null ? unsendMails : Collections.EMPTY_LIST;
    }
}
