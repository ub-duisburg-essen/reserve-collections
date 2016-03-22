package unidue.rc.system;

/*
 * #%L
 * Semesterapparate
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 - 2015 Universitaet Duisburg Essen
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import miless.model.User;
import unidue.rc.model.ReserveCollection;
import unidue.rc.model.Resource;

/**
 * Created by nils on 13.07.15.
 */
public interface BaseURLService {

    /**
     * Returns the base url that consists of a protocol, server name and optionally by a port. For example
     * <code>http://localhost:8080</code>.
     *
     * @return see description
     */
    String getBaseURL();

    /**
     * Returns the base url that consists of a protocol, server name, optionally a port and an application path
     * that is used by the web container. For example <code>http://localhost:8080/reserve-collections</code>.
     *
     * @return see description
     */
    String getApplicationURL();

    /**
     * Returns the url with which a {@link ReserveCollection} can be addressed.
     *
     * @param collection url that points to given collection
     * @return see description
     */
    String getViewCollectionURL(ReserveCollection collection);

    /**
     * Returns the url with which a {@link User} can be edited.
     *
     * @param user url that points to edit user page
     * @return see description
     */
    String getEditUserLink(User user);

    /**
     * Returns an URL with which a file of a resource can be downloaded
     *
     * @param resource url that points to download target resource
     * @return see description
     */
    String getDownloadLink(Resource resource);

    /**
     * Returns the url with which a user is able to prolong a collection.
     *
     * @param collection see description
     * @return a url in string form that points to the prolong page
     */
    String getProlongLink(ReserveCollection collection);
}
