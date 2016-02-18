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
