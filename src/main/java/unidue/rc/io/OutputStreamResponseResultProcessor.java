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


import org.apache.tapestry5.services.ComponentEventResultProcessor;
import org.apache.tapestry5.services.Response;

import java.io.IOException;
import java.io.OutputStream;

/**
 * <p>ComponentEventResultProcessor that enables EventHandlers to return Callbacks that can stream arbitrary data to the
 * client. The class was inspired by {@link org.apache.tapestry5.internal.services.StreamResponseResultProcessor}</p>
 * See: <a href="http://wiki.apache.org/tapestry/Tapestry5HowToCreateAComponentEventResultProcessor">Tapestry5HowToCreateAComponentEventResultProcessor</a>
 *
 * @author Nils Verheyen
 * @since 03.09.14 15:38
 */
public class OutputStreamResponseResultProcessor implements ComponentEventResultProcessor<OutputStreamResponse> {

    private final Response response;

    public OutputStreamResponseResultProcessor(Response response) {
        this.response = response;
    }

    /**
     * Handles OutputStreamResponse
     *
     * @param streamResponse Callback for streaming arbitrary data to the client using the response {@link
     *                       OutputStream}
     * @see ComponentEventResultProcessor#processResultValue(Object)
     */
    @Override
    public void processResultValue(OutputStreamResponse streamResponse)
            throws IOException {
        streamResponse.prepareResponse(response);
        streamResponse.processRequest(response.getOutputStream(streamResponse.getContentType()));
    }
}
