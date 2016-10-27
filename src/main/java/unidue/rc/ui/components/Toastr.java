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

import org.apache.tapestry5.ClientElement;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

/**
 * <p>
 *     A <code>Toastr</code> component can be used to show non blocking notifications for a user. The component is based
 *     on and uses the toastr javascript library.
 * </p>
 * <p>
 *     Example:
 *     {@code
 *     <t:toastr position="top_right" type="info" t:id="myToastr" target="clientElement">Content</t:toastr>
 *     }
 * </p>
 * <div>
 *     Position, type and target are required elements. See {@link Toastr.Type} and {@link Toastr.Position} for more
 *     information. The target contains the element that will be bound to the toastr, to finally show the notification
 *     on an event.
 * </div>
 *
 * @see <a href="https://github.com/CodeSeven/toastr">Toastr on Github</a>
 */
@Import(library = {
        "context:vendor/toastr/toastr.min.js",
        "context:js/toastr.js"
}, stylesheet = {
        "context:vendor/toastr/toastr.min.css"
})
public class Toastr implements ClientElement {

    /**
     * <p>Type of the toastr notification, may be one of the following:</p>
     * <ul>
     *     <li>success</li>
     *     <li>info</li>
     *     <li>warning</li>
     *     <li>error</li>
     * </ul>
     */
    public enum Type {
        SUCCESS, INFO, WARNING, ERROR
    }

    /**
     * <p>Position and sizing of the notification, may be one of the following:</p>
     * <ul>
     *     <li>top_right</li>
     *     <li>bottom_right</li>
     *     <li>bottom_left</li>
     *     <li>top_left</li>
     *     <li>top_full_width</li>
     *     <li>bottom_full_width</li>
     *     <li>top_center</li>
     *     <li>bottom_center</li>
     * </ul>
     */
    public enum Position {
        TOP_RIGHT, BOTTOM_RIGHT, BOTTOM_LEFT, TOP_LEFT, TOP_FULL_WIDTH, BOTTOM_FULL_WIDTH, TOP_CENTER, BOTTOM_CENTER
    }

    @Parameter(allowNull = false, required = true)
    private ClientElement target;

    @Parameter(defaultPrefix = "literal", allowNull = false, required = true, value = "info")
    private Type type;

    @Parameter(defaultPrefix = "literal", allowNull = false, required = true, value = "toast-top-right")
    private Position position;

    @Parameter(defaultPrefix = "literal", allowNull = false, value = "click")
    private String event;

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

    private Element element;

    private String clientId;

    @SetupRender
    void onSetupRender() {

        element = null;
        clientId = null;
    }

    @BeginRender
    void onBeginRender(MarkupWriter writer) {

        element = writer.element("div", "style", "display: none !important;");
    }

    @AfterRender
    void onAfterRender(MarkupWriter writer) {

        writer.end();

        JSONObject options = new JSONObject();
        String toastrPosition = String.format("toast-%s", position.name().toLowerCase().replace('_', '-'));
        options.put("positionClass", toastrPosition);
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
        spec.put("target", target.getClientId());
        spec.put("elementId", this.getClientId());
        spec.put("type", type.name().toLowerCase());
        spec.put("title", title);
        spec.put("event", event);
        spec.put("options", options);

        javaScriptSupport.addInitializerCall("toastr", spec);
    }

    @Override
    public String getClientId() {
        if (clientId == null) {
            if (element == null)
                throw new IllegalStateException(String.format(
                        "Client id for %s is not available as it did not render yet (or was disabled).",
                        componentResources.getCompleteId()));

            clientId = javaScriptSupport.allocateClientId(componentResources);
            element.forceAttributes("id", clientId);
        }

        return clientId;
    }

}
