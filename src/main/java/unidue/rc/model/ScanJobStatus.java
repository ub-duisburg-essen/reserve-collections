package unidue.rc.model;


import org.apache.commons.lang3.StringUtils;

/**
 * @author Nils Verheyen
 * @since 29.11.13 07:27
 */
public enum ScanJobStatus implements JobStatus {

    NEW(1, 0x55D400),
    IN_PROCESS(2, 0xff6600),
    RESERVATION(3, 0x7137c8),
    REQUEST_AT_THE_RESELLER(4, 0xB32F34),
    DONE(6, 0x2c89a0);

    private final Integer value;
    private final Integer color;

    ScanJobStatus(Integer value, Integer color) {
        this.value = value;
        this.color = color;
    }

    @Override
    public Object getDatabaseValue() {
        return value;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    public String getColor() {
        return Integer.toHexString(color);
    }

    @Override
    public int compareTo(JobStatus other) {
        return value.compareTo(other.getValue());
    }

    public static ScanJobStatus get(int status) {
        for (ScanJobStatus s : values()) {
            if (s.value.equals(status))
                return s;
        }
        return null;
    }

    public static String getName(int status) {
        ScanJobStatus scanJobStatus = get(status);
        return scanJobStatus != null ? scanJobStatus.name() : StringUtils.EMPTY;
    }
}
