package unidue.rc.ui.components;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
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
import unidue.rc.ui.pages.entry.Download;
import unidue.rc.ui.services.MimeService;

/**
 * Created by nils on 10.06.16.
 */
public class DownloadLink extends AbstractLink {

    public enum Method {
        Inline,
        Attachment
    }

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
        Link downloadLink;
        String hrefString;
        if (mimeServicePage != null && type.equalsIgnoreCase(Method.Inline.name())) {
            downloadLink = linkSource.createPageRenderLinkWithContext(mimeServicePage, resource.getId());
            hrefString = downloadLink.toURI();
        } else {
            downloadLink = linkSource.createPageRenderLinkWithContext(Download.class, resource.getId(), type);
            String baseURI = downloadLink.toURI();
            StringBuilder uri = new StringBuilder(baseURI);
            if (!baseURI.endsWith("/"))
                uri.append("/");
            uri.append(encode(resource.getFileName()));

            hrefString = uri.toString();
        }


        writer.element("a", "href", hrefString);

        writer.attributes(namesAndValues);

        resources.renderInformalParameters(writer);
    }

    private String encode(String value) {
        URLCodec codec = new URLCodec();
        try {
            return codec.encode(value);
        } catch (EncoderException e) {
            log.error("could not encode " + value, e);
            return value;
        }
    }

    @AfterRender
    void onAfterRender(MarkupWriter writer) {
        if (isDisabled()) return;

        writer.end(); // <a>
    }
}
