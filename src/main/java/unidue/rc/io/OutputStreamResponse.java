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

    /**
     * Prepares the response before it is sent to the client. This is the place to set any response headers (e.g.
     * content-disposition).
     *
     * @param response Response that will be sent.
     */
    default void prepareResponse(Response response) { }
}
