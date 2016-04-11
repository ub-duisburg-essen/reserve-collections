package unidue.rc.migration.legacymodel;


import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "dateTime", strict = false)
public class DateTimeLocal {

    @Attribute(name = "type")
    private String type;

    @Attribute(name = "format")
    private String format;

    @Attribute(name = "value")
    private String value;

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @return the format
     */
    public String getFormat() {
        return format;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "DateTimeLocal [type= " + type + " format= " + format + " value= " + value + "]";
    }

}
