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
package unidue.rc.ui.components;


import miless.model.User;
import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.Asset;
import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.Block;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Path;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.apache.tapestry5.services.javascript.StylesheetLink;
import org.apache.tapestry5.services.javascript.StylesheetOptions;
import org.slf4j.Logger;
import se.unbound.tapestry.breadcrumbs.BreadCrumbInfo;
import unidue.rc.model.ActionDefinition;
import unidue.rc.model.DefaultRole;
import unidue.rc.security.CollectionSecurityService;
import unidue.rc.system.SystemConfigurationService;

/**
 * Layout component for pages of application reserve-collections.
 */
@Import(stylesheet = {
        "context:vendor/bootstrap/css/bootstrap.min.css",
        "context:vendor/jquery-ui/css/ude-reserve-collections/jquery-ui-1.10.2.custom.min.css",
        "context:vendor/toastr/toastr.min.css",
        "context:css/main.css"
}, library = {
        "context:js/main.js",
        "context:js/apply.responsive.menu.js",
        "context:js/apply.help.tooltip.js",
        "context:js/highlight.form.control.groups.js"
}, stack = {
        "GlobalScriptStack"
})
public class Layout {

    @Inject
    private Logger log;

    /**
     * The page title, for the <title> element and the <h1>element.
     */
    @SuppressWarnings("unused")
    @Property
    @Parameter(required = true, defaultPrefix = BindingConstants.LITERAL)
    private String title;

    @SuppressWarnings("unused")
    @Property
    @Parameter(required = false, defaultPrefix = BindingConstants.LITERAL)
    private String subtitle;

    @Property
    @Parameter(required = false, defaultPrefix = BindingConstants.BLOCK)
    private Block actionBarBlock;

    @Property
    private BreadCrumbInfo breadCrumb;

    @Inject
    private ComponentResources resources;

    @Inject
    private Messages messages;

    @Inject
    private SystemConfigurationService config;

    @Inject
    private CollectionSecurityService securityService;

    @Property
    private boolean showDialog;

    @Inject
    @Path("context:css/print.css")
    private Asset printStylesheet;

    @Environmental
    private JavaScriptSupport javaScriptSupport;

    @SetupRender
    void onSetupRender() {
        javaScriptSupport.importStylesheet(new StylesheetLink(printStylesheet, new StylesheetOptions("print")));
    }

    public boolean isLoggedIn() {
        return securityService.getCurrentUser() != null;
    }

    public Integer getUserID() {
        User currentUser = securityService.getCurrentUser();
        return currentUser != null ? currentUser.getUserid() : 0;
    }

    public String getClassForLink(String link) {

        return StringUtils.startsWithIgnoreCase(link, resources.getPageName()) ? "current-page-item" : null;
    }

    public boolean isEditSettingsPermitted() {
        return securityService.isPermitted(ActionDefinition.EDIT_SETTINGS)
                || securityService.isPermitted(ActionDefinition.EDIT_COLLECTION_SETTINGS);
    }

    public Integer getDocentRoleId() {
        return securityService.getRole(DefaultRole.DOCENT).getId();
    }

    public String getLinkForNode(String id) {
        return config.getString("nav.link." + id);
    }

    public String getLinkTitleForNode(String id) {
        return messages.get("nav.item." + id);
    }
}
