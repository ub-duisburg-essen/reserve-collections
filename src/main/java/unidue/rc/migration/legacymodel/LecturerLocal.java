package unidue.rc.migration.legacymodel;


import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;


@Root(name = "lecturer", strict = false)
public class LecturerLocal {

    @Attribute(name="ID", required = false)
    private int id;

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

}
