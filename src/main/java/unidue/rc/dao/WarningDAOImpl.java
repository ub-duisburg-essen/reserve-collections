package unidue.rc.dao;

/*
 * #%L
 * Semesterapparate
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 - 2016 Universitaet Duisburg Essen
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
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.SelectQuery;
import unidue.rc.model.ReserveCollection;
import unidue.rc.model.Warning;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by nils on 17.03.16.
 */
public class WarningDAOImpl extends BaseDAOImpl implements WarningDAO {

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
