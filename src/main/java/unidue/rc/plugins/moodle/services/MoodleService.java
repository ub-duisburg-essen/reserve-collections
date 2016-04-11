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
package unidue.rc.plugins.moodle.services;


import unidue.rc.io.XMLStreamResponse;
import unidue.rc.plugins.moodle.DecryptedRequestData;
import unidue.rc.plugins.moodle.model.ResourceRequest;
import unidue.rc.ui.RequestError;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

/**
 * Created by nils on 15.06.15.
 */
public interface MoodleService {

    enum RealmMapping {

        CAS("cas", "ude"),
        LDAP("ldap", "ude"),
        SHIBBOLETH("shibboleth", "ude"),
        WILDCARD("*", "*");

        private final String moodleName;
        private final String collectionName;

        RealmMapping(String moodleName, String collectionName) {
            this.moodleName = moodleName;
            this.collectionName = collectionName;
        }
    }

    /**
     * Requests which contain encrypted information have the same form. The parameter <code>requestData</code> contains
     * encrypted information and the <code>key</code> contains a symmetric key with which the request data is encrypted.
     * These two parameters are decrypted, decoded and returned in a new <code>DecryptedRequestData</code> object if
     * decryption was successful, <code>null</code> otherwise.
     *
     * @param request the http servlet request
     * @return the decrypted request data if decryption was successful
     * @throws RequestError thrown if the request contains invalid data
     */
    DecryptedRequestData decryptRequestData(HttpServletRequest request) throws RequestError;

    /**
     * Sends a XML back with the following form to target response.
     * <pre>&lt;moodle data="..." skey="..."/&gt;</pre>
     * The data is encrypted with the symmetric key and encoded with base64. The symmetric key is encrypted with moodles
     * public key.
     *
     * @param data   data string that is encrypted with target symmetric key
     * @param symkey symmetric to to encrypt the data
     * @return {@link XMLStreamResponse} that can be written to the client
     * @throws IOException thrown if the data could not be serialized
     */
    XMLStreamResponse createResponseObject(String data, byte[] symkey) throws IOException;

    /**
     * Caches target request of a {@link unidue.rc.model.Resource} for later download request.
     *
     * @param requestData contains the request data that should be cached
     * @return a session id which must be used to download the requested file.
     * @see #cacheResourceRequest(ResourceRequest)
     */
    String cacheResourceRequest(ResourceRequest requestData);

    /**
     * Retrieves the {@link ResourceRequest} for target session id, or <code>null</code> if the request is timed out.
     *
     * @param sessionID contains the session id that target request should be returned for
     * @return the {@link ResourceRequest} or <code>null</code>
     */
    ResourceRequest getResourceRequest(String sessionID);

    /**
     * Returns the realm name used in this application that is an equivalent to moodles realm or <code>null</code>
     * if none could be found.
     *
     * @param moodleName contains the realm name used by moodle
     * @return the realm used inside this application
     */
    default String getRealm(String moodleName) {
        Optional<RealmMapping> optional = Arrays
                .stream(RealmMapping.values())
                .filter(mapping -> mapping.moodleName.equals(moodleName))
                .findAny();
        return optional.isPresent()
                ? optional.get().collectionName
                : null;
    }
}
