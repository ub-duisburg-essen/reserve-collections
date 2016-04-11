package unidue.rc.dao;


import miless.model.MCRCategory;
import miless.model.MCRCategoryLabel;
import org.apache.cayenne.BaseContext;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.SelectQuery;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class OriginDAOImpl implements OriginDAO {

    @Inject
    private MCRCategoryDAO categoryDAO;

    @Override
    public List<MCRCategory> getOrigins() {

        ObjectContext context = BaseContext.getThreadObjectContext();

        Expression qualifier = ExpressionFactory.noMatchExp(MCRCategory.PARENT_CATEGORY_PROPERTY, null);
        qualifier = qualifier.andExp(ExpressionFactory.matchExp(MCRCategory.CLASSID_PROPERTY, "ORIGIN"));

        SelectQuery query = new SelectQuery(MCRCategory.class);
        query.setQualifier(qualifier);
        query.addPrefetch(MCRCategory.CHILD_CATEGORIES_PROPERTY); // fetch
        // children
        query.addPrefetch(MCRCategory.LABELS_PROPERTY); // fetch children

        @SuppressWarnings("unchecked")
        List<MCRCategory> origins = context.performQuery(query);

        return origins != null ? origins : Collections.<MCRCategory>emptyList();
    }

    @Override
    public MCRCategory getOrigin(String id) {
        ObjectContext context = BaseContext.getThreadObjectContext();

        SelectQuery query = new SelectQuery(MCRCategory.class);

        query.setQualifier(ExpressionFactory.matchExp(MCRCategory.CLASSID_PROPERTY, "ORIGIN")
                .andExp(ExpressionFactory.matchExp(MCRCategory.CATEGID_PROPERTY, id)));

        return (MCRCategory) Cayenne.objectForQuery(context, query);
    }

    @Override
    public String getOriginLabel(Locale locale, Integer originID) {

        String currentLanguage = locale.getLanguage();
        String result = StringUtils.EMPTY;

        MCRCategory origin = categoryDAO.getCategoryById(originID);
        // labels contain text to display inside the client
        List<MCRCategoryLabel> labels = origin.getLabels();
        for (MCRCategoryLabel label : labels) {

            String labelLanguage = new Locale(label.getLang()).getLanguage();

            // filter label by current language
            if (currentLanguage.equals(labelLanguage))
                result = label.getText();

        }
        return result;
    }

}
