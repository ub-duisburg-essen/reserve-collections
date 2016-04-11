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


import unidue.rc.model.Resource;

import java.util.List;

/**
 * A <code>MimeService</code> can be used to retrieve pages and components according to mime types detected through
 * apache tikas implementation.
 *
 * @author Nils Verheyen
 * @see org.apache.tika.mime.MediaType
 * @see org.apache.tika.Tika
 * @since 26.08.14 11:49
 */
public interface MimeService {

    /**
     * Returns the class of a page that is able to display content according to target mime type. Be sure to give commit
     * the {@link unidue.rc.model.Resource} id to the page otherwise no content can be displayed.
     *
     * @param resource resource displayed be returned page
     * @return a class of a page or null if no page is able to display content.
     */
    Class<?> getPage(Resource resource);

    /**
     * Returns a list with all protocols that are supported through streaming configured in sysconfig.xml through
     * the key <code>stream.format.protocol.[xyz]</code>.
     *
     * @param mimeType should contain the full mime type of a {@link unidue.rc.model.Resource}
     * @return all available streaming protocols for target mime
     */
    List<String> getStreamingProtocols(String mimeType);

    /**
     * Returns the port with which a protocol returned by {@link #getStreamingProtocols(String)} can be addressed.
     *
     * @param protocol protocol that is used
     * @return port for the protocol or <code>null</code> if none is found
     */
    Integer getStreamingPort(String protocol);
}
