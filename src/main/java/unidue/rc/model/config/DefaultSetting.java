package unidue.rc.model.config;


import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by marcus.koesters on 09.04.15.
 */

@Root
public class DefaultSetting {

    @Element(name="key")
    private String key;

    @Element(name="value", required=false)
    private String value;

    @Element(name="label")
    private String label;

    @Element(name="format", required = false)
    private String format;

    @Element(name="type")
    private Integer type;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }
}
