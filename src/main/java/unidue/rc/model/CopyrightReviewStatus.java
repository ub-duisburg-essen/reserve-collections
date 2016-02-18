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

import org.apache.commons.lang3.StringUtils;

/**
 * @author Nils Verheyen
 * @since 12.03.14 08:40
 */
public enum  CopyrightReviewStatus implements JobStatus {

    NOT_REVIEWED(1, 0xCC0000),
    REVIEWED_PAYMENT_NEEDED(2, 0xFFA500),
    REVIEWED_FREE(3, 0x7BB661),
    REVIEWED_FEEDBACK_NEEDED(4, 0xB1ADFF),
    REVIEWED_REJECTED_BY_COPYRIGHT(5, 0xff9800);

    private final Integer dbValue;
    private final Integer color;

    CopyrightReviewStatus(int dbValue, int color) {
        this.dbValue = dbValue;
        this.color = color;
    }

    @Override
    public Integer getValue() {
        return dbValue;
    }

    @Override
    public int compareTo(JobStatus other) {
        return dbValue.compareTo(other.getValue());
    }

    @Override
    public Object getDatabaseValue() {
        return dbValue;
    }

    public String getColor() {
        return Integer.toHexString(color);
    }

    public static CopyrightReviewStatus get(int status) {
        for (CopyrightReviewStatus s : values()) {
            if (s.dbValue.equals(status))
                return s;
        }
        return null;
    }

    public static String getName(int status) {
        CopyrightReviewStatus scanJobStatus = get(status);
        return scanJobStatus != null ? scanJobStatus.name() : StringUtils.EMPTY;
    }
}
