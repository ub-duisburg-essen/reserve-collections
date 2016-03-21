package unidue.rc.dao;

/*
 * #%L
 * Semesterapparate
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 Universitaet Duisburg Essen
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

import miless.model.User;
import org.apache.cayenne.BaseContext;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.NamedQuery;
import org.apache.cayenne.query.Query;
import org.apache.cayenne.query.SelectQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unidue.rc.model.*;
import unidue.rc.system.DateConvertUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class ReserveCollectionDAOImpl extends BaseDAOImpl implements ReserveCollectionDAO {

    private static final Logger LOG = LoggerFactory.getLogger(ReserveCollectionDAOImpl.class);

    @Override
    public List<ReserveCollection> getReserveCollections() {

        SelectQuery query = new SelectQuery(ReserveCollection.class);
        query.addPrefetch(ReserveCollection.LIBRARY_LOCATION_PROPERTY);

        return getCollections(query);
    }

    @Override
    public List<ReserveCollection> getCollections(LibraryLocation location) {
        SelectQuery query = new SelectQuery(ReserveCollection.class);
        query.addPrefetch(ReserveCollection.LIBRARY_LOCATION_PROPERTY);
        query.setQualifier(ExpressionFactory.matchExp(ReserveCollection.LIBRARY_LOCATION_PROPERTY, location));

        return getCollections(query);
    }

    @Override
    public List<ReserveCollection> getCollections(User user, ActionDefinition action) {
        String[] keys = {"resource", "name", "userID"};
        String[] values = {action.getResource(), action.getName(), user.getId().toString()};
        NamedQuery query = new NamedQuery(ReserveCollectionsDatamap.SELECT_COLLECTIONS_BY_PERMISSION_QUERYNAME, keys, values);

        return getCollections(query);
    }

    @Override
    public List<ReserveCollection> getExpiringCollections(LocalDate baseDate, int daysUntilExpiration, ReserveCollectionStatus status) {

        SelectQuery query = new SelectQuery(ReserveCollection.class);

        Expression qualifier = ExpressionFactory.matchExp(ReserveCollection.STATUS_PROPERTY, status)
                .andExp(ExpressionFactory.lessOrEqualExp(ReserveCollection.VALID_TO_PROPERTY, DateConvertUtils.asUtilDate(baseDate.plusDays(daysUntilExpiration))))
                .andExp(ExpressionFactory.matchExp(ReserveCollection.DISSOLVE_AT_PROPERTY, null));

        query.setQualifier(qualifier);

        return getCollections(query);
    }

    private List<ReserveCollection> getCollections(Query query) {

        ObjectContext context = BaseContext.getThreadObjectContext();

        List<ReserveCollection> collections = context.performQuery(query);
        return collections != null
                ? collections
                : Collections.EMPTY_LIST;
    }
}
