package unidue.rc.ui.components;

import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;

/**
 * A <code>CopyrightUploadWarning</code> displays a bootstrap alert info in one line with a given column layout.
 *
 * @see #offset
 * @see #layoutClass
 * @see #trail
 */
public class CopyrightUploadWarning {

    /**
     * The offset contains the css class that is used to offset the alert.
     */
    @Parameter(required = false, defaultPrefix = "literal")
    @Property
    private String offset;

    /**
     * Contains the column class with which the alert is shown.
     */
    @Parameter(required = false, allowNull = false, value = "col-md-9", defaultPrefix = "literal")
    @Property
    private String layoutClass;

    /**
     * The offset contains the css class that is used to limit the alert.
     */
    @Parameter(required = false, defaultPrefix = "literal")
    @Property
    private String trail;
}
