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
