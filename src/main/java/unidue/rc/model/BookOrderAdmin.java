package unidue.rc.model;


import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by nils on 25.06.15.
 */
public enum BookOrderAdmin {

    GW_GSW("semapp@ub.uni-duisburg-essen.de", DefaultLocation.GW_GSW),
    MNT("jessica.peto@uni-due.de", DefaultLocation.MNT),
    MEDIZIN("semapp-med@ub.uni-duisburg-essen.de", DefaultLocation.MEDIZIN),
    LK("infodu-sonderaufgaben@ub.uni-duisburg-essen.de", DefaultLocation.LK),
    BA("fachbib.ba@ub.uni-duisburg-essen.de", DefaultLocation.BA),
    MC("fachbib.mc@ub.uni-duisburg-essen.de", DefaultLocation.MC),
    ;

    private final DefaultLocation[] locations;
    private String mail;

    BookOrderAdmin(String mail, DefaultLocation... locations) {
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
