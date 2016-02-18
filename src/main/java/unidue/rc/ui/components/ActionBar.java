package unidue.rc.ui.components;

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
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.ApplicationStateManager;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Response;
import org.slf4j.Logger;
import unidue.rc.security.CollectionSecurityService;
import unidue.rc.ui.Referrer;
import unidue.rc.ui.pages.login.Index;

import java.io.IOException;

/**
 * @author Nils Verheyen
 * @since 10.09.13 15:16
 */
@Import(library = {
        "context:js/jquery.fn.sticky.navigation.js",
        "context:js/actionbar.js",
})
public class ActionBar {

    @Inject
    private Logger log;

    @Inject
    private ComponentResources resources;

    @Inject
    private ApplicationStateManager sessionStateManager;

    @Inject
    private Messages messages;

    @InjectPage
    private Index loginPage;

    @Inject
    private CollectionSecurityService securityService;

    @Inject
    private PageRenderLinkSource linkSource;

    @Inject
    private Request request;

    @Inject
    private Response response;

    @SessionState
    private Referrer referrer;

    @OnEvent("DoAuth")
    Object onDoAuth() throws IOException {

        User loggedInUser = securityService.getCurrentUser();
        // logout if current user is logged in
        if (loggedInUser != null) {
            Referrer referrer = this.referrer;

            securityService.logout();

            // reset referrer after logout, because it is reset along with the session
            sessionStateManager.set(Referrer.class, referrer);
            return resources.getPage();
        } else {
            // otherwise send user to login page

            /*
            the page context is generally not available inside components. rather than setting the context inside
            the action bar by every page the tapestry internal constant PAGE_CONTEXT_NAME (t:ac) is used.
             */
            return loginPage;
        }
    }

    public String getAuthActionLabel() {
        User loggedInUser = securityService.getCurrentUser();
        return loggedInUser != null
                ? messages.get("auth.action.label.logout")
                : messages.get("auth.action.label.login");
    }

    public String getUsername() {
        User loggedInUser = securityService.getCurrentUser();
        return loggedInUser != null
                ? loggedInUser.getUsername()
                : messages.get("guest.username");
    }
}
