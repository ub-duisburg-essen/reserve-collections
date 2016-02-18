package unidue.rc.model;

/*
 * #%L
 * Semesterapparate
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 Universitaet Duisburg Essen
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

import org.apache.cayenne.ExtendedEnumeration;

/**
 * Cayenne is not very usable with abstraction, therefore this interface represents a status of a job. Multiple jobs can
 * be used together with this status. A status is normally just a enumeration, therefore the methods declared in this
 * interface match them.
 *
 * @author Nils Verheyen
 * @see BookJobStatus
 * @see ScanJobStatus
 * @see unidue.rc.model.BookJob
 * @see unidue.rc.model.ScanJob
 * @since 12.12.13 12:45
 */
public interface JobStatus extends ExtendedEnumeration {

    /**
     * Returns the name of this status.
     *
     * @return see description
     */
    String name();

    /**
     * Returns the int value of this status
     *
     * @return see description
     */
    Integer getValue();

    /**
     * Compares this status to another. The result is similar to the result of a enum.
     *
     * @param other other status to compare to
     * @return see description
     */
    int compareTo(JobStatus other);
}
