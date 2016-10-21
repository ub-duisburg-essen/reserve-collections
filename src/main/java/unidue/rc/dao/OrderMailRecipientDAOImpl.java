package unidue.rc.dao;

import org.apache.cayenne.BaseContext;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.SelectQuery;
import unidue.rc.model.LibraryLocation;
import unidue.rc.model.OrderMailRecipient;

import javax.mail.internet.InternetAddress;
import java.util.Collections;
import java.util.List;

/**
 * Created by nils on 20.10.16.
 */
public class OrderMailRecipientDAOImpl extends BaseDAOImpl implements OrderMailRecipientDAO {

    @Override
    public List<OrderMailRecipient> getRecipients(LibraryLocation location, Class instanceClass) {

        ObjectContext context = BaseContext.getThreadObjectContext();

        SelectQuery query = new SelectQuery(OrderMailRecipient.class);
        query.setQualifier(ExpressionFactory.matchExp(OrderMailRecipient.LOCATION_PROPERTY, location)
                .andExp(ExpressionFactory.matchExp(OrderMailRecipient.INSTANCE_CLASS_PROPERTY, instanceClass.getName())));

        List<OrderMailRecipient> recipients = context.performQuery(query);
        return recipients != null
               ? recipients
               : Collections.EMPTY_LIST;
    }

    @Override
    public OrderMailRecipient addOrderMailRecipient(LibraryLocation location, InternetAddress mail, Class instanceClass) throws CommitException {

        OrderMailRecipient recipient = new OrderMailRecipient();
        recipient.setInstanceClass(instanceClass.getName());
        recipient.setLocation(location);
        recipient.setMail(mail.toString());
        create(recipient);
        return recipient;
    }
}
