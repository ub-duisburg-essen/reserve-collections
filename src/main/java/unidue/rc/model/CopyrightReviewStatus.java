package unidue.rc.model;


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
