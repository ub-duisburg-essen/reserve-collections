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
