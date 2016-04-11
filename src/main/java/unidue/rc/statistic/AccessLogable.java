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
package unidue.rc.statistic;


import unidue.rc.model.AccessLog;
import unidue.rc.model.accesslog.Access;

/**
 * An <code>AccessLogable</code> can be used to match {@link Access} objects from web server logs and use the
 * as aggregated {@link AccessLog} data.
 */
public interface AccessLogable {

    String VIEW_ACTION = "view";
    String DOWNLOAD_ACTION = "download";

    /**
     * Returns <code>true</code> if target {@link Access} matches specific rules to aggregate statistic data,
     * <code>false</code> otherwise.
     *
     * @param access access object that may matches this loggable
     * @return <code>true</code> is given access matches
     * @see #createAccessLog(Access)
     */
    boolean matches(Access access);

    /**
     * Creates a new {@link AccessLog} object that can be used as aggregated statistic data. Target access must be
     * matches by this matches method.
     *
     * @param access access object that may matches this loggable
     * @return <code>true</code> is given access matches
     * @see #matches(Access)
     */
    AccessLog createAccessLog(Access access);
}
