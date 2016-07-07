package unidue.rc.ui.components;

import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

/**
 * Created by nils on 27.06.16.
 */

@Import(stylesheet = {
        "context:vendor/bootstrap/css/bootstrap.min.css",
        "context:css/print.css"
})
public class PrintLayout {

    @Parameter
    private String dinCSS;

    @Environmental
    private JavaScriptSupport javaScriptSupport;

    public String getDinCSS() {
        return dinCSS;
    }
}
