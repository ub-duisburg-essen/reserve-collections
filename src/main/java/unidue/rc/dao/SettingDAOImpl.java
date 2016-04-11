package unidue.rc.dao;


import org.apache.cayenne.BaseContext;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.query.SelectQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unidue.rc.model.Setting;

import java.util.List;

/**
 * Created by marcus.koesters on 14.04.15.
 */

public class SettingDAOImpl extends BaseDAOImpl implements SettingDAO {

    private static final Logger LOG = LoggerFactory.getLogger(SettingDAOImpl.class);

    @Override
    public List<Setting> getAllSettings() {
        ObjectContext context = BaseContext.getThreadObjectContext();
        SelectQuery query = new SelectQuery(Setting.class);
        return context.performQuery(query);
    }

    @Override
    public Setting getSetting(String key) {
        ObjectContext context = BaseContext.getThreadObjectContext();
        return (Setting) Cayenne.objectForPK(context, Setting.class.getSimpleName(), key);
    }

    @Override
    public boolean getBoolean(String key) {
        Setting setting = getSetting(key);
        return setting != null && Boolean.valueOf(setting.getValue());
    }
}
