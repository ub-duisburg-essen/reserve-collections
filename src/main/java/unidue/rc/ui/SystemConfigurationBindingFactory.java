package unidue.rc.ui;


import org.apache.tapestry5.Binding;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.ioc.internal.util.TapestryException;
import org.apache.tapestry5.services.BindingFactory;
import unidue.rc.system.SystemConfigurationService;

/**
 * <p>
 * A <code>SystemConfigurationBindingFactory</code> is able to create new {@link SystemConfigurationBinding} objects so
 * that additional system configuration can be loaded of the class path. By default all configuration data in tapestry
 * is set inside the local application properties ( {@code <appname>.properties}). These properties are ment for labels
 * inside the ui. System configuration should not be added to these configuration files. Therefore this configuration
 * data is set for example inside the {@code sysconfig.xml}.
 * </p>
 * <a href="http://wiki.apache.org/tapestry/Tapestry5HowToAddMessageFormatBindingPrefix">Tapestry5HowToAddMessageFormatBindingPrefix</a>
 *
 * @author Nils Verheyen
 */
public class SystemConfigurationBindingFactory implements BindingFactory {

    private SystemConfigurationService configService;

    public SystemConfigurationBindingFactory(SystemConfigurationService configService) throws TapestryException {
        this.configService = configService;
    }

    @Override
    public Binding newBinding(String description, ComponentResources container, ComponentResources component,
                              String expression, Location location) {
        return new SystemConfigurationBinding(configService, expression);
    }

}
