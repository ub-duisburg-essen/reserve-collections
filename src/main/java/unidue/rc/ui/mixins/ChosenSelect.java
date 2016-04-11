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
package unidue.rc.ui.mixins;


import org.apache.tapestry5.Field;
import org.apache.tapestry5.annotations.AfterRender;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.InjectContainer;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

/**
 * The chosen mixin provides adds jquery chosen autocomplete functionality to select lists. Add attribute
 * <code>t:mixins="ChosenSelect"</code> to the select list to provide autocomplete.
 *
 * @author Nils Verheyen
 * @see <a href="http://harvesthq.github.io/chosen/">Chosen</a>
 * @since 08.10.13 15:29
 */
@Import(library = {
        "context:js/messages.js",
        "context:js/chosen.jquery.js",
        "context:js/apply.jquery.chosen.js"
}, stylesheet = {
        "context:vendor/jquery-chosen/chosen/chosen.css"
})
public class ChosenSelect {

    @Environmental
    private JavaScriptSupport javaScriptSupport;

    /**
     * The field component to which this mixin is attached.
     */
    @InjectContainer
    private Field field;

    @AfterRender
    void afterRender() {
        javaScriptSupport.addScript("applyChosen('%s');", field.getClientId());
    }
}
