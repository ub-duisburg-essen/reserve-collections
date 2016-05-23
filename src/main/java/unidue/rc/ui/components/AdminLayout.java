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

import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.Asset;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Path;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.apache.tapestry5.services.javascript.StylesheetLink;
import org.apache.tapestry5.services.javascript.StylesheetOptions;

/**
 * Created by nils on 06.04.16.
 */

@Import(stylesheet = {
        "context:vendor/bootstrap/css/bootstrap.min.css",
        "context:vendor/jquery-ui/css/ude-reserve-collections/jquery-ui-1.10.2.custom.min.css",
        "context:vendor/toastr/toastr.min.css",
        "context:css/main.css",
        "context:css/admin.css"
}, library = {
        "context:js/main.js",
        "context:js/apply.responsive.menu.js",
        "context:js/apply.help.tooltip.js",
        "context:js/highlight.form.control.groups.js"
}, stack = {
        "GlobalScriptStack"
})
public class AdminLayout {

    @Inject
    @Path("context:css/print.css")
    private Asset printStylesheet;

    @Environmental
    private JavaScriptSupport javaScriptSupport;

    @Inject
    private ComponentResources resources;

    @SetupRender
    void onSetupRender() {
        javaScriptSupport.importStylesheet(new StylesheetLink(printStylesheet, new StylesheetOptions("print")));
    }

    public String getClassForLink(String link) {

        return StringUtils.startsWithIgnoreCase(link, resources.getPageName()) ? "current-page-item" : null;
    }
}
