package unidue.rc.model;


import org.apache.cayenne.ExtendedEnumeration;

/**
 * Created by nils on 23.06.15.
 */
public enum SettingType implements ExtendedEnumeration {

    SYSTEM(1),
    COLLECTION(2);

    private final Integer value;

    SettingType(Integer value) {
        this.value = value;
    }

    @Override
    public Object getDatabaseValue() {
        return value;
    }

    public Integer getValue() {
        return value;
    }
}
