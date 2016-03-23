package unidue.rc.dao;

/*
 * #%L
 * Semesterapparate
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 - 2015 Universitaet Duisburg Essen
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
