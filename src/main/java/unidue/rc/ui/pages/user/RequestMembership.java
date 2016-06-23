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
package unidue.rc.ui.pages.user;


import miless.model.User;
import org.apache.commons.mail.EmailException;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import se.unbound.tapestry.breadcrumbs.BreadCrumb;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.RoleDAO;
import unidue.rc.model.Mail;
import unidue.rc.model.Role;
import unidue.rc.security.CollectionSecurityService;
import unidue.rc.system.MailService;
import unidue.rc.system.SystemConfigurationService;
import unidue.rc.ui.ProtectedPage;
import unidue.rc.ui.Referrer;

import java.io.IOException;

/**
 * Created by nils on 04.08.15.
 */
@BreadCrumb(titleKey = "request.membership.page.title")
@ProtectedPage
public class RequestMembership {

    @Inject
    private Logger log;

    @Inject
    private SystemConfigurationService config;

    @Inject
    private CollectionSecurityService securityService;

    @Inject
    private MailService mailService;

    @Inject
    @Service(RoleDAO.SERVICE_NAME)
    private RoleDAO roleDAO;

    @Inject
    private PageRenderLinkSource linkSource;

    @Inject
    private Messages messages;

    @Property
    private Role requestedRole;

    @SessionState
    private Referrer referrer;

    @Property
    @Persist(PersistenceConstants.FLASH)
    private String requestSuccessMsg;

    @Property
    @Persist(PersistenceConstants.FLASH)
    private String requestFailedMsg;

    @RequiresAuthentication
    void onActivate(Integer roleID) {
        requestedRole = roleDAO.get(Role.class, roleID);
    }

    @OnEvent(EventConstants.PASSIVATE)
    Integer onPassivate() {
        return requestedRole.getId();
    }

    public String getRequestMessage() {
        return messages.format("info.msg.request.membership", requestedRole.getName());
    }

    @OnEvent(component = "requestLink")
    void onRequestMembership() {

        User currentUser = securityService.getCurrentUser();
        VelocityContext context = new VelocityContext();
        context.put("user", currentUser);
        context.put("role", requestedRole);
        context.put("userOrigin", mailService.buildOrigin(currentUser));
        context.put("editUserLink", linkSource.createPageRenderLinkWithContext(Index.class, currentUser.getId()).toAbsoluteURI());
        try {
            Mail mail = mailService.builder("/vt/mail.request.membership.vm")
                    .from(config.getString("mail.from"))
                    .subject(messages.format("request.membership.mail.subject", currentUser.getUsername(), requestedRole.getName()))
                    .context(context)
                    .addRecipients(config.getString("system.mail"))
                    .create();
            mailService.sendMail(mail);
            requestSuccessMsg = messages.get("info.msg.request.membership.mail.send");
        } catch (CommitException e) {
            log.error("could not save mail", e);
            requestFailedMsg = messages.get("info.msg.request.membership.failed");
        } catch (EmailException e) {
            log.error("could not send mail", e);
            requestFailedMsg = messages.get("info.msg.request.membership.failed");
        } catch (IOException e) {
            log.error("could not create mail", e);
            requestFailedMsg = messages.get("info.msg.request.membership.failed");
        }

    }
}
