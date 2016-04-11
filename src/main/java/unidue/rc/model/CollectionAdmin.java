package unidue.rc.model;


import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by nils on 03.08.15.
 */
public enum CollectionAdmin {

    ONLINE("semapp@ub.uni-duisburg-essen.de", DefaultLocation.ONLINE),
    GW_GSW("semapp@ub.uni-duisburg-essen.de", DefaultLocation.GW_GSW),
    MNT("jessica.peto@uni-due.de", DefaultLocation.MNT),
    MEDIZIN("semapp-med@ub.uni-duisburg-essen.de", DefaultLocation.MEDIZIN),
    DUISBURG("infodu-sonderaufgaben@ub.uni-duisburg-essen.de",
            DefaultLocation.LK,
            DefaultLocation.BA,
            DefaultLocation.MC),
    ;

    private final DefaultLocation[] locations;
    private String mail;

    CollectionAdmin(String mail, DefaultLocation... locations) {
        this.locations = locations;
        this.mail = mail;
    }

    public DefaultLocation[] getLocations() {
        return locations;
    }

    public String getMail() {
        return mail;
    }

    public static Set<String> mails(Integer locationID) {
        return Arrays.stream(values())
                .filter(admin -> Arrays.stream(admin.locations).anyMatch(location -> location.getId() == locationID))
                .map(admin -> admin.mail)
                .collect(Collectors.toSet());
    }
}
