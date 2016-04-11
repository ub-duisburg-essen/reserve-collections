package unidue.rc.migration.legacymodel;


import org.simpleframework.xml.*;

@Root(name = "name", strict = false)
@NamespaceList(value = { @Namespace(prefix = "mods", reference = "http://www.loc.gov/mods/v3") })
public class NameMetaDataLocal {

    @Element(name = "displayForm")
    private String teacher;

    @Attribute(name = "valueURI")
    private String valueURI;

    @Path("role")
    @Element(name = "roleTerm")
    private String roleTerm;

    public String getLegalEntityID() {
        if (valueURI != null) {
            String[] values = valueURI.split("#");
            if (values.length > 0) {
                return values[values.length - 1];
            }
        }
        return null;
    }

    public boolean isTeacher() {
        return "tch".equalsIgnoreCase(roleTerm);
    }

    /**
     * @return the teacher
     */
    public String getTeacher() {
        return teacher;
    }

}
