package unidue.rc.migration.legacymodel;


import org.simpleframework.xml.*;

@Root(name = "mycoreobject", strict = false)
@NamespaceList(value = { @Namespace(prefix = "mods", reference = "http://www.loc.gov/mods/v3") })
public class ReserveCollectionMetaDataLocal {

    @Path("metadata")
    @Element(name = "def.modsContainer")
    private DefModsContainer container;

    /**
     * @return the container
     */
    public DefModsContainer getContainer() {
        return container;
    }

    /**
     * @param container
     *            the container to set
     */
    public void setContainer(DefModsContainer container) {
        this.container = container;
    }

}
