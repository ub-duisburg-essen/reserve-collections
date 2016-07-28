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
package unidue.rc.migration;


import unidue.rc.model.CopyrightReviewStatus;

import java.util.Arrays;
import java.util.Optional;

/**
 * Created by nils on 01.07.15.
 */
public enum CopyrightReviewMapping {

    NotReviewed("not.reviewed", CopyrightReviewStatus.NOT_REVIEWED),
    ReviewedPaymentNeeded("reviewed.payment.needed", CopyrightReviewStatus.REVIEWED_PAYMENT_NEEDED),
    ReviewedFeedbackNeeded("reviewed.feedback", CopyrightReviewStatus.REVIEWED_FEEDBACK_NEEDED),
    ReviewedFree("reviewed.free", CopyrightReviewStatus.REVIEWED_FREE),
    ReviewedBooktex("reviewed.booktex", CopyrightReviewStatus.REVIEWED_BOOKTEX)
    ;

    private String legacyStatus;
    private CopyrightReviewStatus status;

    CopyrightReviewMapping(String legacyStatus, CopyrightReviewStatus status) {
        this.legacyStatus = legacyStatus;
        this.status = status;
    }

    public static CopyrightReviewStatus get(String legacyValue) {
        return get(legacyValue, null);
    }

    public static CopyrightReviewStatus get(String legacyValue, CopyrightReviewStatus defaultValue) {
        Optional<CopyrightReviewMapping> m = Arrays.stream(values())
                .filter(mapping -> mapping.legacyStatus.equals(legacyValue))
                .findFirst();
        return m.isPresent() ? m.get().status : defaultValue;
    }

    public String getLegacyStatus() {
        return legacyStatus;
    }

    public CopyrightReviewStatus getStatus() {
        return status;
    }
}
