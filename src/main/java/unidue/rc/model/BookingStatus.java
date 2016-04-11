package unidue.rc.model;


import org.apache.cayenne.ExtendedEnumeration;

/**
 * Created by nils on 18.09.15.
 */
public enum BookingStatus implements ExtendedEnumeration {

    AWAITS_BOOKING(1),
    IS_BOOKED(2);

    private final int value;

    BookingStatus(int value) {
        this.value = value;
    }

    @Override
    public Object getDatabaseValue() {
        return value;
    }
}
