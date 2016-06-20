package unidue.rc.ui.components;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.AfterRender;
import org.apache.tapestry5.annotations.BeginRender;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.corelib.base.AbstractLink;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.slf4j.Logger;
import unidue.rc.model.Resource;
import unidue.rc.system.BaseURLService;
import unidue.rc.ui.pages.entry.Download;
import unidue.rc.ui.services.MimeService;

import java.net.URISyntaxException;

/**
 * Created by nils on 10.06.16.
 */
public class DownloadLink extends AbstractLink {

    /**
     * <p>One of:</p>
     * <ul>
     * <li><code>inline</code></li>
     * <li><code>attachment</code></li>
     * </ul>
     * <p>Inline downloads will be handled by the browser, with attachment the browser is forced to save the file.</p>
     */
    @Parameter(required = true, allowNull = false, defaultPrefix = "literal")
    private String type;

    @Parameter(required = true, allowNull = false)
    private Resource resource;

    @Inject
    private Logger log;

    @Inject
    private PageRenderLinkSource linkSource;

    @Inject
    private BaseURLService urlService;

    @Inject
    private ComponentResources resources;

    @Inject
    private MimeService mimeService;

    @BeginRender
    void onBeginRender(MarkupWriter writer) {
        if (isDisabled()) return;

        writeDownloadLink(writer);
    }

    /**
     * Writes an &lt;a&gt; element with the provided link as the href attribute. A call to
     * {@link org.apache.tapestry5.MarkupWriter#end()} is <em>not</em> provided. Automatically appends an anchor if
     * the component's anchor parameter is non-null. Informal parameters are rendered as well.
     *
     * @param writer         to write markup to
     * @param namesAndValues additional attributes to write
     */
    private void writeDownloadLink(MarkupWriter writer, Object... namesAndValues) {

        // Download page requires resource id and method therefore the link has to created at
        // least with this information
        Class<?> mimeServicePage = mimeService.getPage(resource);
        String hrefString;
        if (mimeServicePage != null && type.equalsIgnoreCase(BaseURLService.DownloadMethod.Inline.name())) {
            hrefString = buildMimePageHref(mimeServicePage);
        } else {
            try {
                hrefString = buildHrefFromURLService();
            } catch (URISyntaxException e) {
                Link downloadLink = linkSource.createPageRenderLinkWithContext(Download.class, resource.getId(),
                        BaseURLService.DownloadMethod.Attachment);
                hrefString = downloadLink.toURI();
            }
        }

        writer.element("a", "href", hrefString);

        writer.attributes(namesAndValues);

        resources.renderInformalParameters(writer);
    }

    private String buildHrefFromURLService() throws URISyntaxException {
        BaseURLService.DownloadMethod downloadMethod;
        switch (type) {
            case "inline":
                downloadMethod = BaseURLService.DownloadMethod.Inline;
                break;
            case "attachment":
            default:
                downloadMethod = BaseURLService.DownloadMethod.Attachment;
                break;
        }
        return urlService.getDownloadLink(resource, downloadMethod);
    }

    private String buildMimePageHref(Class<?> mimeServicePage) {
        Link downloadLink = linkSource.createPageRenderLinkWithContext(mimeServicePage, resource.getId());
        String href = downloadLink.toURI();
        return href;
    }

    @AfterRender
    void onAfterRender(MarkupWriter writer) {
        if (isDisabled()) return;

        writer.end(); // <a>
    }
}
