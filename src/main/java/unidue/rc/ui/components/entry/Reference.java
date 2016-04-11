package unidue.rc.ui.components.entry;


import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;

/**
 * Created by nils on 05.08.15.
 */
public class Reference {

    @Parameter(required = true, allowNull = false)
    @Property
    private unidue.rc.model.Reference reference;
}
