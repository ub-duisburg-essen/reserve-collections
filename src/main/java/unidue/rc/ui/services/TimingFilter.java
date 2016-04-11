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
package unidue.rc.ui.services;


import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.RequestFilter;
import org.apache.tapestry5.services.RequestHandler;
import org.apache.tapestry5.services.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by nils on 23.04.15.
 */
public class TimingFilter implements RequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(TimingFilter.class);

    @Override
    public boolean service(Request request, Response response, RequestHandler handler) throws IOException {
        long startTime = System.currentTimeMillis();

        try {
                    /*
                     * The responsibility of a filter is to invoke the
                     * corresponding method in the handler. When you chain
                     * multiple filters together, each filter received a handler
                     * that is a bridge to the next filter.
                     */
            return handler.service(request, response);
        } finally {
            long elapsed = System.currentTimeMillis() - startTime;

            LOG.debug(String.format("Request time: %d ms", elapsed));
        }
    }
}
