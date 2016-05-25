/**
 * Copyright (C) 2014 - 2016 Universitaet Duisburg-Essen (semapp|uni-due.de)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package unidue.rc.ui.components;

import org.apache.tapestry5.ClientElement;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.Context;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

/**
 * Created by nils on 20.05.16.
 */
@Import(library = {
        "context:vendor/toastr/toastr.min.js",
        "context:js/toastr.js"
}, stylesheet = {
        "context:vendor/toastr/toastr.min.css"
})
public class Toastr {

    public enum Type {
        SUCCESS, INFO, WARNING, ERROR
    }

    public enum Position {
        TOP_RIGHT, BOTTOM_RIGHT, BOTTOM_LEFT, TOP_LEFT, TOP_FULL_WIDTH, BOTTOM_FULL_WIDTH, TOP_CENTER, BOTTOM_CENTER
    }

    @Parameter(allowNull = false, required = true)
    private ClientElement observe;

    @Parameter(defaultPrefix = "literal", allowNull = false, required = true, value = "info")
    private Type type;

    @Parameter(defaultPrefix = "literal", allowNull = false, required = true, value = "top_right")
    private Position position;

    @Parameter
    private String title;

    @Parameter(value = "swing", allowNull = false, defaultPrefix = "literal")
    private String showEasing;

    @Parameter(value = "linear", allowNull = false, defaultPrefix = "literal")
    private String hideEasing;

    @Parameter(value = "fadeIn", allowNull = false, defaultPrefix = "literal")
    private String showMethod;

    @Parameter(value = "fadeOut", allowNull = false, defaultPrefix = "literal")
    private String hideMethod;

    @Parameter(value = "300")
    private int showDuration;

    @Parameter(value = "1000")
    private int hideDuration;

    @Parameter(value = "5000")
    private int timeOut;

    @Parameter(value = "1000")
    private int extendedTimeOut;

    @Parameter
    private boolean closeButton;

    @Parameter
    private boolean debug;

    @Parameter
    private boolean progressBar;

    @Parameter
    private boolean preventDuplicates;

    @Parameter
    private boolean newestOnTop;

    @Inject
    private ComponentResources componentResources;

    @Environmental
    private JavaScriptSupport javaScriptSupport;


    @BeginRender
    void onBeginRender(MarkupWriter writer) {

        writer.element("div", "style", "display: none !important;");
    }

    @AfterRender
    void onAfterRender(MarkupWriter writer) {

        writer.end();

        JSONObject options = new JSONObject();
        options.put("positionClass", position.name().toLowerCase());
        options.put("showEasing", showEasing);
        options.put("hideEasing", hideEasing);
        options.put("showMethod", showMethod);
        options.put("hideMethod", hideMethod);
        options.put("showDuration", showDuration);
        options.put("hideDuration", hideDuration);
        options.put("timeOut", timeOut);
        options.put("extendedTimeOut", extendedTimeOut);
        options.put("closeButton", closeButton);
        options.put("debug", debug);
        options.put("progressBar", progressBar);
        options.put("preventDuplicates", preventDuplicates);
        options.put("newestOnTop", newestOnTop);

        JSONObject spec = new JSONObject();
        spec.put("observe", observe.getClientId());
        spec.put("elementID", componentResources.getId());
        spec.put("type", type.name().toLowerCase());
        spec.put("title", title);
        spec.put("options", options);

        javaScriptSupport.addInitializerCall("toastr", spec);
    }
}
