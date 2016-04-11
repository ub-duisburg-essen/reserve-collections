package unidue.rc.model;


import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by nils on 25.06.15.
 */
public enum ScannableOrderAdmin {

    DEFAULT("semapp-scan@ub.uni-duisburg-essen.de",
            DefaultLocation.GW_GSW,
            DefaultLocation.MNT,
            DefaultLocation.BA,
            DefaultLocation.LK,
            DefaultLocation.MC,
            DefaultLocation.ONLINE),
    MEDIZIN("semapp-med@ub.uni-duisburg-essen.de", DefaultLocation.MEDIZIN);

    private final DefaultLocation[] locations;
    private String mail;

    ScannableOrderAdmin(String mail, DefaultLocation... locations) {
        this.locations = locations;
        this.mail = mail;
    }

    public static Set<String> mails(int locationID) {

        return Arrays.stream(values())
                .filter(admin -> Arrays.stream(admin.locations).anyMatch(location -> location.getId() == locationID))
                .map(admin -> admin.mail)
                .collect(Collectors.toSet());
    }
}
