package unidue.rc.model;


/**
 * Created by nils on 25.06.15.
 */
public enum DefaultLocation {

    ONLINE("Online", false, 0),
    ESSEN("Essen", true, 10),
    DUISBURG("Duisburg", true, 11),
    GW_GSW(ESSEN, "GW/GSW", true, 1),
    MNT(ESSEN, "MNT", true, 3),
    MEDIZIN(ESSEN, "Medizin", true, 5),
    LK(DUISBURG, "LK", true, 6),
    BA(DUISBURG, "BA", true, 7),
    MC(DUISBURG, "MC", true, 8),
    ;

    private DefaultLocation parent;
    private String name;
    private boolean isPhysical;
    private int id;

    DefaultLocation(String name, boolean isPhysical, int id) {
        this.name = name;
        this.isPhysical = isPhysical;
        this.id = id;
    }

    DefaultLocation(DefaultLocation parent, String name, boolean isPhysical, int id) {
        this.parent = parent;
        this.name = name;
        this.isPhysical = isPhysical;
        this.id = id;
    }

    public DefaultLocation getParent() {
        return parent;
    }

    public String getName() {
        return name;
    }

    public boolean isPhysical() {
        return isPhysical;
    }

    public int getId() {
        return id;
    }
}
