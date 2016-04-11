package unidue.rc.ui.services;


import org.apache.commons.configuration.ConfigurationException;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.InjectService;
import org.slf4j.Logger;
import unidue.rc.dao.DatabaseException;
import unidue.rc.dao.TestCayenneServiceImpl;
import unidue.rc.system.CayenneService;

/**
 * @author Nils Verheyen
 */
public class TestModule {

    public static void bind(ServiceBinder binder) {
        binder.bind(CayenneService.class, TestCayenneServiceImpl.class);
    }


    public static void contributeApplicationDefaults(
            final Logger log,
            final @InjectService("CayenneService") CayenneService cayenneService,
            MappedConfiguration<String, Object> configuration) {
        try {
            cayenneService.init();
        } catch (DatabaseException e) {
            log.error("could not initialize cayenne: ", e);
        } catch (ConfigurationException e) {
            log.error("could not load default config: ", e);
        }

        // The factory default is true but during the early stages of an application
        // overriding to false is a good idea. In addition, this is often overridden
        // on the command line as -Dtapestry.production-mode=false
        configuration.add(SymbolConstants.PRODUCTION_MODE, false);

        // The application version number is incorprated into URLs for some
        // assets. Web browsers will cache assets because of the far future expires
        // header. If existing assets are changed, the version number should also
        // change, to force the browser to download new versions.
        configuration.add(SymbolConstants.APPLICATION_VERSION, "1.0-SNAPSHOT-TEST");
    }
}