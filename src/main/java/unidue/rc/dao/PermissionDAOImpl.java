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

import miless.model.User;
import org.apache.cayenne.BaseContext;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.NamedQuery;
import org.apache.cayenne.query.SelectQuery;
import unidue.rc.model.ActionDefinition;
import unidue.rc.model.Permission;
import unidue.rc.model.Role;

import java.util.Collections;
import java.util.List;

/**
 * Created by nils on 11.05.15.
 */
public class PermissionDAOImpl extends BaseDAOImpl implements PermissionDAO {

    @Override
    public List<Permission> getPermissions(User user) {

        SelectQuery query = new SelectQuery(Permission.class);
        query.setQualifier(ExpressionFactory.matchExp(Permission.USER_ID_PROPERTY, user.getId()));

        ObjectContext context = BaseContext.getThreadObjectContext();

        List<Permission> permissions = context.performQuery(query);
        return permissions != null
                ? permissions
                : Collections.EMPTY_LIST;
    }

    @Override
    public List<Permission> getPermissions(Integer instanceID) {

        ObjectContext context = BaseContext.getThreadObjectContext();

        SelectQuery query = new SelectQuery(Permission.class);
        Expression qualifier = ExpressionFactory.matchExp(Permission.INSTANCE_ID_PROPERTY, instanceID);
        query.setQualifier(qualifier);

        List<Permission> permissions = context.performQuery(query);

        return permissions != null ? permissions : Collections.EMPTY_LIST;
    }

    @Override
    public void deletePermissionsByInstance(Integer instanceID) {

        NamedQuery query = new NamedQuery("delete_permissions_by_instanceid", Collections.singletonMap("instanceID", instanceID));

        ObjectContext context = BaseContext.getThreadObjectContext();

        context.performGenericQuery(query);
    }
}
