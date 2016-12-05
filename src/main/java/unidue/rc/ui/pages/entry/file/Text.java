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
package unidue.rc.ui.pages.entry.file;


import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.HttpError;
import org.slf4j.Logger;
import se.unbound.tapestry.breadcrumbs.BreadCrumb;
import se.unbound.tapestry.breadcrumbs.BreadCrumbList;
import unidue.rc.dao.ResourceDAO;
import unidue.rc.model.Resource;
import unidue.rc.security.CollectionSecurityService;
import unidue.rc.system.SystemConfigurationService;
import unidue.rc.ui.ProtectedPage;
import unidue.rc.ui.ResourcePageUtil;
import unidue.rc.ui.SecurityContextPage;
import unidue.rc.ui.pages.Error404;
import unidue.rc.workflow.ResourceService;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Nils Verheyen
 * @since 13.10.14 14:50
 */
@Import(library = {
        "context:vendor/highlight-js/highlight.min.js",
        "context:js/entry/file/text.js",
}, stylesheet = {
        "context:vendor/highlight-js/default.min.css"
})
@BreadCrumb(titleKey = "text")
@ProtectedPage
public class Text implements SecurityContextPage {

    @Inject
    private Logger log;

    @Inject
    private Messages messages;

    @Inject
    private SystemConfigurationService config;

    @Inject
    private ResourceDAO resourceDAO;

    @Inject
    private ResourceService resourceService;

    @Property
    private Resource text;

    @SessionState
    private BreadCrumbList breadCrumbList;

    /** Contains all values defined inside sysconfig.xml by syntax.highlight. */
    @Persist(PersistenceConstants.FLASH)
    private Set<String> brushes;

    @SetupRender
    public void beginRender() {
        breadCrumbList.getLastCrumb().setTitle(text.getFileName());
    }

    @OnEvent(EventConstants.ACTIVATE)
    Object onActivate(Integer resourceID) {
        text = resourceDAO.get(Resource.class, resourceID);
        if (text == null || !text.isFileAvailable())
            return new HttpError(HttpServletResponse.SC_NOT_FOUND, messages.get("not.found"));

        List<String> extensionList = config.getStringArray("syntax.highlight.extensions");
        brushes = new HashSet<>(extensionList);
        return null;
    }

    @OnEvent(EventConstants.PASSIVATE)
    Object onPassivate() {
        return text.getId();
    }

    public String getRawText() {
        File textFile = resourceService.download(text);
        StringBuffer buffer = new StringBuffer();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(textFile));
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
                buffer.append(System.lineSeparator());
            }
            return buffer.toString();
        } catch (IOException e) {
            log.error("could not read text of file " + textFile.getAbsolutePath());
        }
        return StringUtils.EMPTY;
    }

    public String getBrush() {
        String extension = text.getExtension();
        if (extension != null && brushes.contains(extension))
            return extension;
        return "nohighlight";
    }

    @Override
    public void checkPermission(CollectionSecurityService securityService, EventContext activationContext) throws AuthorizationException {
        ResourcePageUtil.checkPermission(securityService, activationContext, resourceDAO);
    }
}
