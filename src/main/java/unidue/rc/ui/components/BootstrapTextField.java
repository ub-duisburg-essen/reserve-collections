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


import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.corelib.components.TextField;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nils on 30.07.15.
 */
public class BootstrapTextField extends TextField {

    @Override
    protected void writeFieldTag(MarkupWriter writer, String value) {
        List<Object> atts = new ArrayList<>();

        atts.add("name");
        atts.add(getControlName());

        atts.add("id");
        atts.add(getClientId());

        atts.add("value");
        atts.add(value);

        atts.add("size");
        atts.add(getWidth());

        if (isDisabled()) {

            atts.add("readonly");
            atts.add("readonly");
        }

        writer.element("input", atts.toArray());
    }

    final void afterRender(MarkupWriter writer) {
        writer.end(); // input
    }
}
