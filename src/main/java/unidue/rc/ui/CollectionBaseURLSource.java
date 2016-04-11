package unidue.rc.ui;


import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.BaseURLSource;
import org.apache.tapestry5.services.Request;
import unidue.rc.system.SystemConfigurationService;

/**
 * Created by nils on 09.07.15.
 */
public class CollectionBaseURLSource implements BaseURLSource {

    private final Request request;

    private final SystemConfigurationService config;

    private String hostname;
    private int hostPort;
    private int secureHostPort;

    public CollectionBaseURLSource(Request request,
                                   @Inject @Symbol(SymbolConstants.HOSTNAME) String hostname,
                                   @Symbol(SymbolConstants.HOSTPORT) int hostPort,
                                   @Symbol(SymbolConstants.HOSTPORT_SECURE) int secureHostPort,
                                   @Inject SystemConfigurationService config) {
        this.request = request;
        this.hostname = hostname;
        this.hostPort = hostPort;
        this.secureHostPort = secureHostPort;
        this.config = config;
    }

    public String getBaseURL(boolean secure) {
        return String.format("%s://%s%s",
                secure ? "https" : "http",
                hostname(),
                portExtension(secure));
    }

    private String portExtension(boolean secure) {
        int port = secure ? secureHostPort : hostPort;

        // The default for the ports is 0, which means to use Request.serverPort. That's mostly
        // for development.
        if (port <= 0) {
            port = request == null
                    ? config.getInt("server.port")
                    : request.getServerPort();
        }

        int expectedPort = secure ? 443 : 80;

        if (port == expectedPort) {
            return "";
        }

        return ":" + port;
    }

    private String hostname() {

        if (StringUtils.isEmpty(hostname)) {
            return request == null
                    ? config.getString("server.name")
                    : request.getServerName();
        }

        // This is common in some PaaS deployments, such as Heroku, where the port is passed in as
        // and environment variable.

        if (this.hostname.startsWith("$")) {
            return System.getenv(hostname.substring(1));
        }

        return hostname;
    }
}
