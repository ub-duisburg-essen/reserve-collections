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
import org.apache.cayenne.DataRow;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.NamedQuery;
import org.apache.cayenne.query.SelectQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Default implementation of {@link UserDAO}.
 *
 * @author Nils Verheyen
 */
public class UserDAOImpl extends BaseDAOImpl implements UserDAO {

    private static final Logger LOG = LoggerFactory.getLogger(UserDAOImpl.class);

    public User getUserById(Integer userId) {

        ObjectContext context = BaseContext.getThreadObjectContext();

        return Cayenne.objectForPK(context, User.class, userId);
    }

    public List<User> getUsers() {
        ObjectContext context = BaseContext.getThreadObjectContext();

        SelectQuery query = new SelectQuery(User.class);

        @SuppressWarnings("unchecked")
        List<User> users = context.performQuery(query);

        return users != null ? users : Collections.<User>emptyList();
    }

    public List<User> getUsers(Collection<Integer> userIDs) {
        ObjectContext context = BaseContext.getThreadObjectContext();

        SelectQuery query = new SelectQuery(User.class);
        query.setQualifier(ExpressionFactory.inExp(User.USERID_PROPERTY, userIDs));

        @SuppressWarnings("unchecked")
        List<User> users = context.performQuery(query);

        return users != null ? users : Collections.<User>emptyList();
    }

    @Override
    public void create(Object o) throws CommitException {

        if (o instanceof User) {

            ObjectContext objectContext = BaseContext.getThreadObjectContext();
            User user = (User) o;
            // get current max id for users
            NamedQuery selectMaxUseridQuery = new NamedQuery("select_max_userid");
            DataRow row = (DataRow) Cayenne.objectForQuery(objectContext, selectMaxUseridQuery);
            Integer maxUserid = (Integer) row.get("userid");
            if (maxUserid == null) {
                maxUserid = 1;
            }
            user.setUserid(maxUserid + 1);
            super.create(user);
        }
    }

    @Override
    public User getUser(String username) {

        ObjectContext context = BaseContext.getThreadObjectContext();

        SelectQuery query = new SelectQuery(User.class);

        Expression expression = ExpressionFactory.matchExp(User.USERNAME_PROPERTY, normalizeUsername(username));

        query.setQualifier(expression);

        User user = (User) Cayenne.objectForQuery(context, query);
        return user;
    }

    @Override
    public User getUser(String username, String realmID) {
        ObjectContext context = BaseContext.getThreadObjectContext();

        SelectQuery query = new SelectQuery(User.class);

        Expression expression = ExpressionFactory.matchExp(User.USERNAME_PROPERTY, normalizeUsername(username))
                .andExp(ExpressionFactory.matchExp(User.REALM_PROPERTY, realmID));

        query.setQualifier(expression);

        User user = (User) Cayenne.objectForQuery(context, query);
        return user;
    }

    @Override
    public List<User> getUsersByLegalEntityId(Integer legalEntityID) {
        ObjectContext context = BaseContext.getThreadObjectContext();

        SelectQuery query = new SelectQuery(User.class);

        Expression expression = ExpressionFactory.matchExp(User.LEID_PROPERTY, legalEntityID);

        query.setQualifier(expression);

        List<User> users = context.performQuery(query);
        return users == null
                ? Collections.EMPTY_LIST
                : users;
    }

    @Override
    public List<User> search(String searchWord) {
        SelectQuery query = new SelectQuery(User.class);

        searchWord = "%" + searchWord + "%";
        Expression expression = ExpressionFactory.likeIgnoreCaseExp(User.USERNAME_PROPERTY, searchWord)
                .orExp(ExpressionFactory.likeIgnoreCaseExp(User.REALNAME_PROPERTY, searchWord))
                .orExp(ExpressionFactory.likeIgnoreCaseExp(User.EMAIL_PROPERTY, searchWord));

        query.setQualifier(expression);
        ObjectContext context = BaseContext.getThreadObjectContext();
        List<User> users = context.performQuery(query);

        return users != null ? users : Collections.<User>emptyList();
    }

    @Override
    public String normalizeUsername(String username) {
        return username == null ? null : username.trim().toLowerCase();
    }

    @Override
    public boolean exists(String username) {
        SelectQuery query = new SelectQuery(User.class);

        query.setQualifier(ExpressionFactory.matchExp(User.USERNAME_PROPERTY, normalizeUsername(username)));

        ObjectContext context = BaseContext.getThreadObjectContext();
        List<User> users = context.performQuery(query);

        return !users.isEmpty();
    }
}
