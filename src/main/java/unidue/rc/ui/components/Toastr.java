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
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.InjectContainer;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

/**
 * Created by nils on 20.05.16.
 */
@Import(library = {
        "context:vendor/toastr/toast.min.js",
        "context:js/toastr.js"
}, stylesheet = {
        "context:vendor/toastr/toast.min.css"
})
public class Toastr {

    public enum Type {
        SUCCESS, INFO, WARNING, ERROR
    }
    public enum Position {
        TOP_RIGHT, BOTTOM_RIGHT, BOTTOM_LEFT, TOP_LEFT, TOP_FULL_WIDTH, BOTTOM_FULL_WIDTH, TOP_CENTER, BOTTOM_CENTER
    }

    @Parameter(defaultPrefix = "literal", allowNull = false, value = "info")
    private Type type;

    @Parameter(defaultPrefix = "literal", allowNull = false, value = "top_right")
    private Position position;

    @Parameter
    private String title;

    @Inject
    private JavaScriptSupport javaScriptSupport;

    @InjectContainer
    private ClientElement clientElement;

    public void show() {

        JSONObject spec = new JSONObject();
        spec.put("elementID", clientElement.getClientId());
        spec.put("type", type.name().toLowerCase());
        spec.put("position", position.name().toLowerCase());
        spec.put("title", title);
        javaScriptSupport.addInitializerCall("toastr", spec);
    }
}
