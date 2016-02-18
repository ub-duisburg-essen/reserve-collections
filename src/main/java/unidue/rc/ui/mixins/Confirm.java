package unidue.rc.ui.mixins;

/*
 * #%L
 * Semesterapparate
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 - 2015 Universitaet Duisburg Essen
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

import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.ClientElement;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.AfterRender;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.InjectContainer;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

/**
 * A mixin used to attach a JavaScript confirmation box to the onclick
 * event of any component that implements ClientElement.
 * Created by nils on 09.06.15.
 */
@Import(library = { "context:js/mixins/confirm.js" })
public class Confirm {
    /**
     * Confirmation message to display.
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String message;

    /**
     * Dialog box title.
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String title;

    /**
     * Validation Button text.
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String validationMsg;

    /**
     * Cancel Button text.
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String cancelMsg;

    /**
     * Confirmation box height.
     */
    @Parameter(value = "240", defaultPrefix = BindingConstants.LITERAL)
    private int height;

    /**
     * Confirmation box width.
     */
    @Parameter(value = "320", defaultPrefix = BindingConstants.LITERAL)
    private int width;
    /**
     * If this parameter is set to <i>true</i>, the user can't interact with the application while the
     * confirmation box is displayed.
     */
    @Parameter(value = "true", defaultPrefix = BindingConstants.LITERAL)
    private boolean isModal;

    /**
     * If this parameter is set to <i>true</i>, the user can dynamically resize the confirmation box.
     */
    @Parameter(value = "false", defaultPrefix = BindingConstants.LITERAL)
    private boolean isResizable;

    /**
     * If this parameter is set to <i>true</i>, the user can drag the confirmation box.
     */
    @Parameter(value = "true", defaultPrefix = BindingConstants.LITERAL)
    private boolean isDraggable;

    /**
     * If this parameter is set to <i>true</i>, default javascript alert is used. Otherwise, jquery dialog box
     * is used, and can be customized with several parameters.
     * <p>
     * Usable parameters in both configurations :
     * message
     * <p>
     * Usable parameters when useDefaultConfirm = false:
     * title, validationMsg, cancelMsg, isModal, isResizable, isDraggable
     * <p>
     * validationMsg, cancelMsg, isModal, isResizable, height
     */
    @Parameter(value = "false", defaultPrefix = BindingConstants.LITERAL)
    private boolean useDefaultConfirm;


    /**
     * since 3.4.3
     * The Confirm parameters you want to override.
     * <p>
     * This will be used only if seDefaultConfirm = false (defaultValue):
     */
    @Parameter
    private JSONObject params;

    //Injected services.

    @Inject
    private JavaScriptSupport javaScriptSupport;

    @InjectContainer
    private ClientElement element;

    @Inject
    private ComponentResources resources;

    @Inject
    private Messages messageProvider;

    @AfterRender
    public void afterRender() {
        JSONObject config = new JSONObject();

        String clientId = element.getClientId();

        config.put("id", clientId);

        config.put("title", title != null ? title : messageProvider.get("default-confirm-title"));
        config.put("message", message != null ? message : messageProvider.get("default-confirm-message"));
        config.put("validationMsg", validationMsg != null ? validationMsg : messageProvider.get("default-valid-label"));
        config.put("cancelMsg", cancelMsg != null ? cancelMsg : messageProvider.get("default-cancel-label"));

        config.put("useDefaultConfirm", useDefaultConfirm);
        config.put("isModal", isModal);
        config.put("isResizable", isResizable);
        config.put("isDraggable", isDraggable);
        config.put("height", height);
        config.put("width", width);


        /*
         * We will merge the default JSON Object with the params parameter
         */
        if (resources.isBound("params")) {
            merge(config, params);
        }

        javaScriptSupport.addInitializerCall("confirm", config);
    }

    static JSONObject merge(JSONObject obj1, JSONObject obj2) {
        if (obj1 == null)
            return null;

        if (obj2 == null)
            return obj1;

        for (String key : obj2.keys()) {
            obj1.put(key, obj2.get(key));
        }

        return null;
    }
}
