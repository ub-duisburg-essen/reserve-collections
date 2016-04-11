package unidue.rc.system;


import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * Created by nils on 25.06.15.
 */
public class SystemMessageServiceImpl implements SystemMessageService {

    private final PropertiesConfiguration properties;

    public SystemMessageServiceImpl() throws ConfigurationException {
        this.properties = new PropertiesConfiguration(SystemMessageServiceImpl.class.getResource("/system.messages_de.properties"));
    }

    @Override
    public String get(String key) {
        return properties.getString(key);
    }
}
