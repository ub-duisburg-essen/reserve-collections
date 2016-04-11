package unidue.rc.migration.legacymodel;


import org.simpleframework.xml.*;

@Root(name = "mycoreobject", strict = false)
@NamespaceList(value = { @Namespace(prefix = "mods", reference = "http://www.loc.gov/mods/v3") })
public class ModsContainer {

    @Path("metadata")
    @Element(name = "def.modsContainer")
    private DefModsContainer container;

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ModsContainer [container=" + container + "]";
    }
}
