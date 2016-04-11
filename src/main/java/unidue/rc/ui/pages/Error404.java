package unidue.rc.ui.pages;


import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.Request;

/**
 * This page serves as a http error page with status 404 - not found.
 *
 * @author Nils Verheyen
 * @since 27.08.13 09:54
 */
public class Error404 {

    @Property
    @Inject
    private Request request;

    @Property
    @Inject
    @Symbol(SymbolConstants.PRODUCTION_MODE)
    private boolean productionMode;
}
