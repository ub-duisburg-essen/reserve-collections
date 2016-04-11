package unidue.rc.dao;


import org.apache.cayenne.BaseContext;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.SelectQuery;
import unidue.rc.model.Action;
import unidue.rc.model.ActionDefinition;
import unidue.rc.model.Role;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ActionDAOImpl implements ActionDAO {

    @Override
    public List<Action> getActions() {
        ObjectContext context = BaseContext.getThreadObjectContext();

        SelectQuery query = new SelectQuery(Action.class);

        List<Action> actions = context.performQuery(query);

        return actions != null ? actions : Collections.<Action>emptyList();
    }

    @Override
    public Action getActionById(Integer id) {
        return Cayenne.objectForPK(BaseContext.getThreadObjectContext(), Action.class, id);
    }

    @Override
    public List<Action> getUnrelatedActions(Role role) {
        ObjectContext context = BaseContext.getThreadObjectContext();
        SelectQuery query = new SelectQuery(Action.class);
        //NamedQuery query = new NamedQuery("select_unrelated_actions_of_role", Collections.singletonMap("roleID", role.getId()));
        List<Action> actions = context.performQuery(query);
        List<Action>  unrelatedActions = actions.stream().filter(action->!action.getRoles().contains(role)).collect(Collectors.toList());
        return unrelatedActions != null ? unrelatedActions : Collections.<Action>emptyList();
    }

    @Override
    public Action getAction(ActionDefinition action) {
        ObjectContext context = BaseContext.getThreadObjectContext();

        SelectQuery query = new SelectQuery(Action.class);
        query.setQualifier(ExpressionFactory

                .matchExp(Action.NAME_PROPERTY, action.getName())
                .andExp(ExpressionFactory
                .matchExp(Action.RESOURCE_PROPERTY, action.getResource())));

        return (Action) Cayenne.objectForQuery(context, query);
    }

}
