package unidue.rc.model.config;


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

