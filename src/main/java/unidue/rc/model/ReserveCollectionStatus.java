package unidue.rc.model;


import org.apache.cayenne.ExtendedEnumeration;

/**
 * @author Nils Verheyen
 * @since 23.10.13 11:58
 */
public enum ReserveCollectionStatus implements ExtendedEnumeration {
    NEW(1),
    ACTIVE(2),
    EXPIRED(3),
    DEACTIVATED(4),
    ARCHIVED(5);

    private final Integer dbValue;

    ReserveCollectionStatus(Integer dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public Object getDatabaseValue() {
        return dbValue;
    }

    public Integer getValue() {
        return dbValue;
    }
}
