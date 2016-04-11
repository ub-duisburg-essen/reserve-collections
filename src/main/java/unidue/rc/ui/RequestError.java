package unidue.rc.ui;


import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * Created by nils on 15.06.15.
 */
@Root(name = "error")
public class RequestError extends Exception {

    @Attribute(name = "code")
    private int sc;

    @Attribute(name = "message")
    private String message;

    public RequestError(int sc, String message) {
        super(message);
        this.sc = sc;
        this.message = message;
    }
}
