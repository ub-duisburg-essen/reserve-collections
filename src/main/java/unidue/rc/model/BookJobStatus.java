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
package unidue.rc.model;


import org.apache.commons.lang3.StringUtils;

/**
 * @author Nils Verheyen
 * @since 29.11.13 07:47
 */
public enum BookJobStatus implements JobStatus {
    NEW(1),
    UNAVAILABLE(2),
    RESERVED(3),
    SYSTEM_DONE(4),
    CANCELED(5);

    private final Integer value;

    BookJobStatus(Integer value) {
        this.value = value;
    }

    @Override
    public Object getDatabaseValue() {
        return value;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public int compareTo(JobStatus other) {
        return value.compareTo(other.getValue());
    }

    public static String getName(int status) {
        for (BookJobStatus s : values()) {
            if (s.value.equals(status))
                return s.name();
        }
        return StringUtils.EMPTY;
    }
}
