package unidue.rc.ui.services;


import unidue.rc.model.Resource;

import java.util.List;

/**
 * A <code>MimeService</code> can be used to retrieve pages and components according to mime types detected through
 * apache tikas implementation.
 *
 * @author Nils Verheyen
 * @see org.apache.tika.mime.MediaType
 * @see org.apache.tika.Tika
 * @since 26.08.14 11:49
 */
public interface MimeService {

    /**
     * Returns the class of a page that is able to display content according to target mime type. Be sure to give commit
     * the {@link unidue.rc.model.Resource} id to the page otherwise no content can be displayed.
     *
     * @param resource resource displayed be returned page
     * @return a class of a page or null if no page is able to display content.
     */
    Class<?> getPage(Resource resource);

    /**
     * Returns a list with all protocols that are supported through streaming configured in sysconfig.xml through
     * the key <code>stream.format.protocol.[xyz]</code>.
     *
     * @param mimeType should contain the full mime type of a {@link unidue.rc.model.Resource}
     * @return all available streaming protocols for target mime
     */
    List<String> getStreamingProtocols(String mimeType);

    /**
     * Returns the port with which a protocol returned by {@link #getStreamingProtocols(String)} can be addressed.
     *
     * @param protocol protocol that is used
     * @return port for the protocol or <code>null</code> if none is found
     */
    Integer getStreamingPort(String protocol);
}
