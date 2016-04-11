package unidue.rc.ui.pages.copyright;


import org.apache.commons.io.IOUtils;
import org.apache.tapestry5.Asset;
import org.apache.tapestry5.annotations.Path;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;
import se.unbound.tapestry.breadcrumbs.BreadCrumb;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by marcus.koesters on 12.08.15.
 */
@BreadCrumb(titleKey = "copyright.title")
public class Index {

    @Inject
    private Logger log;

    @Inject
    @Path("context:copyright/copyright.html")
    private Asset copyrightAsset;

    @Inject
    @Path("context:copyright/Merkblatt_52a_2016-02.pdf")
    @Property(write = false)
    private Asset leafletAsset;

    @Property(write = false)
    private String copyright;

    @SetupRender
    void onSetupRender() {
        try (InputStream input = copyrightAsset.getResource().openStream()) {
            copyright = IOUtils.toString(input);
        } catch (IOException e) {
            log.error("could not read copyright", e);
        }
    }
}
