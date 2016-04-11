package unidue.rc.migration.legacymodel;


import org.simpleframework.xml.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Root(name = "def.modsContainer", strict = false)
@NamespaceList(value = { @Namespace(prefix = "mods", reference = "http://www.loc.gov/mods/v3") })
public class DefModsContainer {

    @Path("modsContainer/mods/titleInfo")
    @Element(required = false)
    private String nonSort;

    @Path("modsContainer/mods/titleInfo")
    @Element
    private String title;

    @Path("modsContainer/mods/titleInfo")
    @Element(required = false)
    private String subTitle;

    @Path("modsContainer/mods")
    @ElementList(inline = true, required = true)
    private ArrayList<NameMetaDataLocal> name;

    @Path("modsContainer/mods")
    @ElementList(inline = true, required = true)
    private ArrayList<ClassificationMetaDataLocal> classifications;

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public String getNonSort() {
        return nonSort;
    }

    /**
     * (non-Javadoc)
     * 
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "DefModsContainer [title=" + title + "]";
    }

    public List<Integer> getTeacherIDs() {
        return this.name.stream()
                .filter(name -> name.isTeacher())
                .map(name -> name.getLegalEntityID())
                .map(id -> Integer.valueOf(id))
                .collect(Collectors.toList());
    }

    public String getOriginID() {
        if (classifications != null && classifications.size() > 0) {
            for (ClassificationMetaDataLocal classification : classifications) {
                if (classification.isOriginClassification())
                    return classification.getValueURIID();
            }
        }
        return null;
    }
}
