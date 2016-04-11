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
