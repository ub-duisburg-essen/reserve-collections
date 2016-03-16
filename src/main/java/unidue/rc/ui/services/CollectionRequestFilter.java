/*
 * Copyright 2014 Universitaet Duisburg Essen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package unidue.rc.ui.services;

/*
 * #%L
 * Semesterapparate
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 Universitaet Duisburg Essen
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

import miless.model.User;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.internal.EmptyEventContext;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unidue.rc.security.CollectionSecurityService;
import unidue.rc.security.RequiresActionPermission;
import unidue.rc.ui.SecurityContextPage;
import unidue.rc.system.SystemConfigurationService;
import unidue.rc.ui.ProtectedPage;
import unidue.rc.ui.Referrer;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * @author Nils Verheyen
 * @since 14.08.14 14:14
 */
public class CollectionRequestFilter implements ComponentRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(CollectionRequestFilter.class);

    private static final String REQUEST_LOG_FORMAT = "user: %10s |- requests: %30s |- params: %s";
    private static final String GUEST_USERNAME = "guest";
    private static final String PARAM_LOG_DIVIDER = ", ";

    private static final String COMPONENT_PARAM_PREFIX = "t:";
    private static final String ERROR_PAGE_NAME_PREFIX = "Error";
    private static final String LOGIN_PAGE_NAME_PREFIX = "login";
    private static final String USE_SECURITY_SETTINGS_KEY = "use.security.checks";

    enum AuthCheckResult {
        AUTHORISED, DENIED, RELOAD_XHR, AUTHENTICATE
    }

    private final PageRenderLinkSource pageRenderLinkSource;
    private final ComponentSource componentSource;
    private final Request request;
    private final Response response;
    private ApplicationStateManager sessionStateManager;
    private final Messages messages;
    private final CollectionSecurityService securityService;
    private final SystemConfigurationService config;

    public CollectionRequestFilter(PageRenderLinkSource pageRenderLinkSource, ComponentSource componentSource,
                                   Request request, Response response, ApplicationStateManager sessionStateManager,
                                   Messages messages, CollectionSecurityService securityService,
                                   SystemConfigurationService config) {
        this.pageRenderLinkSource = pageRenderLinkSource;
        this.componentSource = componentSource;
        this.request = request;
        this.response = response;
        this.sessionStateManager = sessionStateManager;
        this.messages = messages;
        this.securityService = securityService;
        this.config = config;
    }

    @Override
    public void handleComponentEvent(ComponentEventRequestParameters parameters, ComponentRequestHandler handler) throws IOException {
        handler.handleComponentEvent(parameters);
    }

    @Override
    public void handlePageRender(PageRenderRequestParameters parameters, ComponentRequestHandler handler) throws IOException {
        String logicalPageName = parameters.getLogicalPageName();
        EventContext activationContext = parameters.getActivationContext();

        logRequest(parameters);

        // set referrer that the user is redirected to its previous addressed page after login
        setReferrer(logicalPageName, activationContext);

        // check if security is disabled
        if (!config.getBoolean(USE_SECURITY_SETTINGS_KEY)) {
            handler.handlePageRender(parameters);
            return;
        }

        // execute standard request cycle if security is enabled
        AuthCheckResult result = checkAuthorityToPage(logicalPageName, activationContext);

        switch (result) {
            case AUTHORISED:
                handler.handlePageRender(parameters);
                break;
            case DENIED:
                // The method will have set the response to redirect to the PageDenied page.
                response.sendError(HttpServletResponse.SC_FORBIDDEN, messages.get("error.msg.page.access.denied"));
                break;
            case AUTHENTICATE:
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, messages.get("error.msg.unauthorized"));
                break;
            default:
                throw new IllegalStateException(result.toString());
        }
    }

    private void logRequest(PageRenderRequestParameters parameters) {
        User currentUser = securityService.getCurrentUser();
        String username = currentUser != null
                ? currentUser.getUsername()
                : GUEST_USERNAME;

        String logicalPageName = parameters.getLogicalPageName();
        EventContext activationContext = parameters.getActivationContext();
        String[] values = activationContext.toStrings();

        LOG.info(String.format(REQUEST_LOG_FORMAT, username, logicalPageName, StringUtils.join(values, PARAM_LOG_DIVIDER)));
    }

    private void setReferrer(String logicalPageName, EventContext activationContext) {

        if (!StringUtils.startsWithIgnoreCase(logicalPageName, ERROR_PAGE_NAME_PREFIX)
                && !StringUtils.startsWithIgnoreCase(logicalPageName, LOGIN_PAGE_NAME_PREFIX)) {

            Referrer referrer = new Referrer();
            Link linkToRequestedPage = createLinkToRequestedPage(logicalPageName, activationContext);
            LOG.debug("setting referrer: " + linkToRequestedPage.toURI());
            referrer.setPageLink(linkToRequestedPage);
            sessionStateManager.set(Referrer.class, referrer);
        }
    }

    private AuthCheckResult checkAuthorityToPage(String requestedPageName, EventContext activationContext) {
        Component page = componentSource.getPage(requestedPageName);
        ProtectedPage protectedPage = page.getClass().getAnnotation(ProtectedPage.class);
        boolean isPageProtected = protectedPage != null;

        // If page is public (ie. not protected), then everyone is authorised to it so allow access
        if (!isPageProtected)
            return AuthCheckResult.AUTHORISED;

        // If request is AJAX with no session, return an AJAX response that forces reload of the page
        if (request.isXHR() && request.getSession(false) == null) {
            return AuthCheckResult.RELOAD_XHR;
        }

        // If user has not been authenticated, disallow.
        if (protectedPage.isAuthenticationNeeded() &&  securityService.getCurrentUser() == null) {
            return AuthCheckResult.AUTHENTICATE;
        }

        // If user is authorised to the page, then all is well.
        if (isAuthorised(page, activationContext)) {
            return AuthCheckResult.AUTHORISED;
        }

        return AuthCheckResult.DENIED;
    }

    private boolean isAuthorised(Component page, EventContext activationContext) {
        Method[] methods = page.getClass().getDeclaredMethods();
        String objectID = activationContext != null && activationContext.getCount() == 1
                ? activationContext.toStrings()[0]
                : null;
        final boolean[] result = {true};
        Arrays.stream(methods)
                .filter(method -> method.getAnnotation(RequiresActionPermission.class) != null)
                .forEach(method -> {
                    try {
                        RequiresActionPermission permission = method.getAnnotation(RequiresActionPermission.class);
                        Arrays.stream(permission.value())
                                .forEach(definition -> {
                                    LOG.debug("checking permission for " + method.getDeclaringClass().getName() + "." + method.getName() + " with context id " + objectID);
                                    securityService.checkPermission(definition, objectID);
                                });
                    } catch (AuthorizationException e) {
                        // user ist not authorized
                        result[0] = false;
                    } catch (RuntimeException e) {
                        // activation context is no integer
                        LOG.error("could not check permissions on " + method.getName() + " of " + method.getDeclaringClass().getName() + " with context id " + objectID);
                    }
                });
        if (page instanceof SecurityContextPage) {
            try {
                SecurityContextPage contextPage = (SecurityContextPage) page;
                contextPage.checkPermission(securityService, activationContext);
            }   catch (AuthorizationException e) {
                // user ist not authorized
                result[0] = false;
            }
        }
        return result[0];
    }

    private Link createLinkToRequestedPage(String requestedPageName, EventContext eventContext) {
        // Create a link to the page you wanted.

        Link linkToRequestedPage;

        if (eventContext instanceof EmptyEventContext) {
            linkToRequestedPage = pageRenderLinkSource.createPageRenderLink(requestedPageName);
        } else {
            Object[] args = new String[eventContext.getCount()];
            for (int i = 0; i < eventContext.getCount(); i++) {
                args[i] = eventContext.get(String.class, i);
            }
            linkToRequestedPage = pageRenderLinkSource.createPageRenderLinkWithContext(requestedPageName, args);
        }

        // Add any activation request parameters (AKA query parameters).

        List<String> parameterNames = request.getParameterNames();

        for (String parameterName : parameterNames) {
            linkToRequestedPage.removeParameter(parameterName);
            if (!parameterName.startsWith(COMPONENT_PARAM_PREFIX)) {
                linkToRequestedPage.addParameter(parameterName, request.getParameter(parameterName));
            }
        }

        return linkToRequestedPage;
    }
}
