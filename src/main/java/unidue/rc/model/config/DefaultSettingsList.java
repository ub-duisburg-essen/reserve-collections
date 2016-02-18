package unidue.rc.model.config;

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

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by marcus.koesters on 15.04.15.
 */

@Root
public class DefaultSettingsList {

@ElementList(inline=true)
private List<DefaultSetting> defaultSettings;

    public List<DefaultSetting> getDefaultSettings() {
        return defaultSettings;
    }

    public void setDefaultSettings(List<DefaultSetting> defaultSettings) {
        this.defaultSettings = defaultSettings;
    }

    public Map<String,DefaultSetting> getDefMap() {
        Map<String,DefaultSetting> map = new HashMap<String,DefaultSetting> ();
           for(DefaultSetting setting :defaultSettings) {
                map.put(setting.getKey(),setting);
        }
        return map;
    }


}

