package unidue.rc.migration.legacymodel;


import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "aleph", strict = false)
public class AlephLocal {

    @Attribute(name = "userID", required = false)
    private String alephuserid;

    @Attribute(name = "systemID", required = false)
    private String alephsystemid;

    /**
     * @return the alephuserid
     */
    public String getAlephuserid() {
        return alephuserid;
    }

    /**
     * @return the alephsystemid
     */
    public String getAlephsystemid() {
        return alephsystemid;
    }

}
