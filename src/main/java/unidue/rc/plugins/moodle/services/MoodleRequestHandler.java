package unidue.rc.plugins.moodle.services;


import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.services.RequestGlobals;

/**
 * Created by nils on 23.09.15.
 */
public interface MoodleRequestHandler {

    StreamResponse handle(RequestGlobals globals, MoodleWebService service);
}
