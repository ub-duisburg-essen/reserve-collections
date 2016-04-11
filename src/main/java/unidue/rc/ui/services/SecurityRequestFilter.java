package unidue.rc.ui.services;


import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.tapestry5.internal.services.RenderQueueException;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.internal.OperationException;
import org.apache.tapestry5.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by nils on 23.04.15.
 */
public class SecurityRequestFilter implements RequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityRequestFilter.class);

    private final Messages messages;

    /**
     * Receive all the services needed as constructor arguments. When we bind this service, T5 IoC will provide all the
     * services.
     *
     * @param messages system messages catalogue
     */
    public SecurityRequestFilter(Messages messages) {
        this.messages = messages;
    }

    @Override
    public boolean service(Request request, Response response, RequestHandler handler) throws IOException {
        try {
            return handler.service(request, response);
        } catch (UnauthenticatedException e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, messages.get("error401.page.title"));
            return false;
        } catch (AuthorizationException | AuthenticationException e) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, messages.get("error403.page.title"));
            return false;
        }
    }
}
