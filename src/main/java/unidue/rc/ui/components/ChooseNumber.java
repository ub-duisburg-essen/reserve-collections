package unidue.rc.ui.components;

import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;

import java.util.Collection;

public class ChooseNumber {

    @Parameter(required = true, allowNull = false)
    @Property
    private Collection<Integer> numbers;

    @Parameter(required = true, allowNull = false, defaultPrefix = "literal")
    @Property
    private String event;

    @Property(read = false)
    private Integer number;

    public Integer getNumber() {
        return number;
    }
}
