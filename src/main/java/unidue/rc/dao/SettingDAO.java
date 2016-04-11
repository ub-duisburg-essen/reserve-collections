package unidue.rc.dao;


import unidue.rc.model.Setting;

import java.util.List;

/**
 * Created by marcus.koesters on 14.04.15.
 */
public interface SettingDAO extends BaseDAO {

    String SERVICE_NAME = "SettingDAO";

    List<Setting> getAllSettings();

    Setting getSetting(String key);

    boolean getBoolean(String key);
}
