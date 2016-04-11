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
    ReviewedFree("reviewed.free", CopyrightReviewStatus.REVIEWED_FREE)
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
