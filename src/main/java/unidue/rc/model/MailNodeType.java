package unidue.rc.model;


import org.apache.cayenne.ExtendedEnumeration;

/**
 * Created by nils on 24.06.15.
 */
public enum MailNodeType implements ExtendedEnumeration {
    RECIPIENT(1),
    REPLY_TO(2),
    CC(3),
    BCC(4);

    private final Integer value;

    MailNodeType(Integer value) {
        this.value = value;
    }

    @Override
    public Object getDatabaseValue() {
        return value;
    }
}
