package unidue.rc.ui.pages;


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
 * Created by nils on 31.08.15.
 */
@BreadCrumb(titleKey = "faq.title")
public class FAQ {

    @Inject
    private Logger log;

    @Inject
    @Path("context:faq/faq.html")
    private Asset faqAsset;

    @Property
    private String faq;

    @SetupRender
    void onSetupRender() {
        try (InputStream input = faqAsset.getResource().openStream()) {
            faq = IOUtils.toString(input);
        } catch (IOException e) {
            log.error("could not read faq", e);
        }
    }
}
