package unidue.rc.ui.components;


import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;
import unidue.rc.model.ReserveCollection;

/**
 * <p> A <code>CreateEntryLinkList</code> can be used as a component to render a bootstrap navbar list with links to
 * create new entry for a new reserve collection. </p>
 * <pre>
 *     {@code <t:createentrylinklist collection="..."/>}
 * </pre>
 *
 * @author Nils Verheyen
 * @since 11.09.13 09:35
 */
public class CreateEntryLinkList {

    @Parameter(required = true, allowNull = false, name = "collection")
    @Property
    private ReserveCollection collection;
}
