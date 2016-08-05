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


import miless.model.User;
import org.apache.cayenne.BaseContext;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.DeleteDenyException;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.RelationshipQuery;
import org.apache.cayenne.query.SelectQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unidue.rc.model.Action;
import unidue.rc.model.DefaultRole;
import unidue.rc.model.Membership;
import unidue.rc.model.Role;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link RoleDAO}.
 *
 * @author Nils Verheyen
 */
public class RoleDAOImpl extends BaseDAOImpl implements RoleDAO {

    private static final Logger LOG = LoggerFactory
            .getLogger(RoleDAOImpl.class);

    @Override
    public List<Role> getRoles() {
        ObjectContext objectContext = BaseContext.getThreadObjectContext();

        SelectQuery query = new SelectQuery(Role.class);

        @SuppressWarnings("unchecked")
        List<Role> roles = objectContext.performQuery(query);

        return roles != null ? roles : Collections.<Role>emptyList();
    }

    @Override
    public List<Role> getRoles(User user) {
        if (user == null)
            return Collections.EMPTY_LIST;

        ObjectContext objectContext = BaseContext.getThreadObjectContext();

        SelectQuery query = new SelectQuery(Membership.class);
        query.setQualifier(ExpressionFactory.matchExp(Membership.USER_ID_PROPERTY, user.getId()));

        List<Membership> memberships = objectContext.performQuery(query);
        return memberships.stream()
                .map(m -> m.getRole())
                .sorted((r1, r2) -> r1.getName().compareTo(r2.getName()))
                .collect(Collectors.toList());

    }

    @Override
    public List<Membership> getMembership(User user) {

        SelectQuery query = new SelectQuery(Membership.class);
        query.setQualifier(ExpressionFactory.matchExp(Membership.USER_ID_PROPERTY, user.getId()));
        query.addPrefetch(Membership.ROLE_PROPERTY);

        ObjectContext objectContext = BaseContext.getThreadObjectContext();

        List<Membership> globalRoles = objectContext.performQuery(query);
        return globalRoles != null ? globalRoles : Collections.<Membership>emptyList();
    }

    @Override
    public List<Action> getActions(Role role) {

        ObjectContext context = BaseContext.getThreadObjectContext();
        RelationshipQuery query = new RelationshipQuery(role.getObjectId(),
                Role.ACTIONS_PROPERTY);

        @SuppressWarnings("unchecked")
        List<Action> actions = context.performQuery(query);

        return actions != null ? actions : Collections.<Action>emptyList();
    }

    @Override
    public Role getRoleById(Integer id) {

        return Cayenne.objectForPK(BaseContext.getThreadObjectContext(),
                Role.class, id);
    }

    @Override
    public Role getRole(DefaultRole defaultRole) {
        SelectQuery query = new SelectQuery(Role.class);

        Expression qualifier = ExpressionFactory
                .matchExp(Role.NAME_PROPERTY, defaultRole.getName())
                .andExp(ExpressionFactory.matchExp(Role.IS_DEFAULT_PROPERTY, Boolean.TRUE));

        query.setQualifier(qualifier);

        ObjectContext context = BaseContext.getThreadObjectContext();

        return (Role) Cayenne.objectForQuery(context, query);
    }
}
