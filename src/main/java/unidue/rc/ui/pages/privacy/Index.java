/**
 * Copyright (C) 2014 - 2016 Universitaet Duisburg-Essen (semapp|uni-due.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package unidue.rc.ui.pages.privacy;


import miless.model.User;
import org.apache.commons.io.IOUtils;
import org.apache.tapestry5.Asset;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Cookies;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;
import org.slf4j.Logger;
import se.unbound.tapestry.breadcrumbs.BreadCrumb;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.UserDAO;
import unidue.rc.security.CollectionSecurityService;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by marcus.koesters on 29.06.15.
 */

@BreadCrumb(titleKey = "privacy.title")
public class Index {

    public static final String TRACKING_COOKIE = "trackReserveCollections";
    public static final String TRACKING_PERMITTED = "doTrack";
    public static final String TRACKING_REJECTED = "doNotTrack";

    @Inject
    private Logger log;

    @Inject
    private Cookies cookies;

    @Inject
    private Request request;

    @Inject
    private Messages messages;

    @Inject
    private AjaxResponseRenderer ajaxResponseRenderer;

    @InjectComponent
    private Zone privacyObjectionZone;

    @Inject
    @Path("context:privacy/privacy.html")
    private Asset privacyAsset;

    @Property
    private String privacy;

    @Property
    private boolean isTrackingPermitted;

    @Inject
    private CollectionSecurityService securityService;

    @Inject
    private UserDAO userDAO;

    @SetupRender
    void init() {
        String cookieValue = cookies.readCookieValue(TRACKING_COOKIE);
        isTrackingPermitted = cookieValue != null && cookieValue.equals(TRACKING_PERMITTED);

        try (InputStream input = privacyAsset.getResource().openStream()) {
            privacy = IOUtils.toString(input);
        } catch (IOException e) {
            log.error("could not read privacy asset", e);
        }
    }

    @OnEvent(value = EventConstants.SUCCESS, component = "privacyObjectionForm")
    public Object onSetConsent() {

        String cookieValue = isTrackingPermitted
                             ? TRACKING_PERMITTED
                             : TRACKING_REJECTED;
        cookies.writeCookieValue(TRACKING_COOKIE, cookieValue);
        updateCurrentUser(isTrackingPermitted);
        return request.isXHR()
               ? privacyObjectionZone.getBody()
               : null;
    }

    private void updateCurrentUser(boolean isTrackingAllowed) {
        User user = securityService.getCurrentUser();
        if (user != null) {
            user.setIsTrackingAllowed(isTrackingAllowed);
            try {
                userDAO.update(user);
            } catch (CommitException e) {
                log.error("could not update user " + user, e);
            }
        }
    }

}
