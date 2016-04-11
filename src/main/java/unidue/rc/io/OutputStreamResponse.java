package unidue.rc.io;


import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.services.Response;
import unidue.rc.ui.services.AppModule;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Tapestry is able to use different types as return types on page activation, action links and so forth. The
 * <code>OutputStreamResponse</code> can be used as additional return type, if it is contributed through the application
 * module.
 *
 * @author Nils Verheyen
 * @see AppModule#contributeComponentEventResultProcessor(MappedConfiguration, Response)
 * @see <a href="http://tapestry.apache.org/page-navigation.html">Tapestry Page Navigation</a>
 * @since 03.09.14 15:51
 */
public interface OutputStreamResponse {

    /**
     * Returns the content type to be reported to the client.
     *
     * @return mime type of the output
     */
    String getContentType();

    /**
     * Implements a callback to directly write to the output stream. The stream will be closed after this method
     * returns. The provided stream is wrapped in a {@link java.io.BufferedOutputStream} for efficiency.
     *
     * @param out output to write to
     * @throws IOException thrown on any error during write
     */
    void processRequest(OutputStream out) throws IOException;
}
