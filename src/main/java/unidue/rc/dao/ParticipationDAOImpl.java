/*
 * Copyright 2014 Universitaet Duisburg Essen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.NamedQuery;
import org.apache.cayenne.query.SelectQuery;
import org.apache.cayenne.validation.ValidationException;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unidue.rc.model.DefaultRole;
import unidue.rc.model.Participation;
import unidue.rc.model.ReserveCollection;
import unidue.rc.model.Role;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Nils Verheyen
 * @since 14.08.14 10:39
 */
public class ParticipationDAOImpl extends BaseDAOImpl implements ParticipationDAO {

    private static final Logger LOG = LoggerFactory.getLogger(ParticipationDAOImpl.class);

    @Override
    public Participation getActiveParticipation(User user, ReserveCollection collection) {

       return getActiveParticipation(user.getId(), collection);
    }

    @Override
    public Participation getActiveParticipation(Integer userID, Integer collectionID) {

        ReserveCollection collection = get(ReserveCollection.class, collectionID);

        return getActiveParticipation(userID, collection);
    }

    private Participation getActiveParticipation(Integer userID, ReserveCollection collection) {
        SelectQuery query = new SelectQuery(Participation.class);
        query.setQualifier(ExpressionFactory.matchExp(Participation.USER_ID_PROPERTY, userID)
                .andExp(ExpressionFactory.matchExp(Participation.RESERVE_COLLECTION_PROPERTY, collection))
                .andExp(ExpressionFactory.matchExp(Participation.END_DATE_PROPERTY, null)));
        ObjectContext context = BaseContext.getThreadObjectContext();

        return (Participation) Cayenne.objectForQuery(context, query);
    }

    @Override
    public List<Participation> getActiveParticipations(Role role, ReserveCollection collection) {
        SelectQuery query = new SelectQuery(Participation.class);
        query.setQualifier(ExpressionFactory.matchExp(Participation.RESERVE_COLLECTION_PROPERTY, collection.getId())
                .andExp(ExpressionFactory.matchExp(Participation.ROLE_PROPERTY, role).andExp(ExpressionFactory.matchExp(Participation.END_DATE_PROPERTY,null))));
        ObjectContext context = BaseContext.getThreadObjectContext();

        List<Participation> participations = context.performQuery(query);
        return participations != null
               ? participations
               : Collections.EMPTY_LIST;

    }

    @Override
    public List<Participation> getActiveParticipations(User user) {
        SelectQuery query = new SelectQuery(Participation.class);
        query.setQualifier(ExpressionFactory.matchExp(Participation.USER_ID_PROPERTY, user.getId())
                .andExp(ExpressionFactory.matchExp(Participation.END_DATE_PROPERTY, null)));
        ObjectContext context = BaseContext.getThreadObjectContext();

        List<Participation> participations = context.performQuery(query);
        return participations != null
                ? participations
                : Collections.EMPTY_LIST;
    }

    @Override
    public List<Participation> getActiveParticipations(ReserveCollection collection) {
        SelectQuery query = new SelectQuery(Participation.class);
        query.setQualifier(ExpressionFactory.matchExp(Participation.RESERVE_COLLECTION_PROPERTY, collection)
                .andExp(ExpressionFactory.matchExp(Participation.END_DATE_PROPERTY, null)));
        ObjectContext context = BaseContext.getThreadObjectContext();

        List<Participation> participations = context.performQuery(query);
        return participations != null
                ? participations
                : Collections.EMPTY_LIST;
    }

    @Override
    public List<Participation> getActiveParticipations(List<ReserveCollection> collections, Role role) {
        SelectQuery query = new SelectQuery(Participation.class);

        Expression qualifier = ExpressionFactory.matchExp(Participation.ROLE_PROPERTY, role)
                .andExp(ExpressionFactory.matchExp(Participation.END_DATE_PROPERTY, null))
                .andExp(ExpressionFactory.inExp(Participation.RESERVE_COLLECTION_PROPERTY, collections));

        query.setQualifier(qualifier);

        ObjectContext context = BaseContext.getThreadObjectContext();

        List<Participation> docentParticipations = context.performQuery(query);
        return docentParticipations.stream()
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public void createParticipation(Integer userID, ReserveCollection collection, Role role) throws CommitException {

        Participation participation = new Participation();
        participation.setRole(role);
        participation.setReserveCollection(collection);
        participation.setUserId(userID);
        participation.setStartDate(new Date());
        participation.setAccessKey(RandomStringUtils.random(8, true, true));

        create(participation);
    }
}
