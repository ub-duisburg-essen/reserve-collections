package unidue.rc.plugins.moodle.model;

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

/**
 * A <code>Request</code> contains data that each http request from moodle must contain.
 */
public interface Request {

    /**
     * Returns the username for target request.
     *
     * @return the username for the request.
     */
    String getUsername();

    /**
     * Returns the secret defined between this instance and moodle.
     *
     * @return the secret used in this request
     */
    String getSecret();

    /**
     * Returns the auth type that is used by moodle.
     *
     * @return the authentication type used by moodle
     * @see unidue.rc.plugins.moodle.services.MoodleService#getRealm(String)
     */
    String getAuthtype();
}
