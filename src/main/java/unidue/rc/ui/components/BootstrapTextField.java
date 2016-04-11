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
