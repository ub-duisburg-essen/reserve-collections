package unidue.rc.io;


import org.apache.commons.io.IOUtils;
import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.services.Response;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by nils on 15.06.15.
 */
public class XMLStreamResponse implements StreamResponse {

    private static final Logger LOG = LoggerFactory.getLogger(XMLStreamResponse.class);
    private static final String XML_ERROR = "<error code=\"500\" message=\"internal server error occurred\"";

    private InputStream stream;
    
    private Object outputObject;

    public XMLStreamResponse(Object obj) {
        this.outputObject = obj;
    }

    @Override
    public String getContentType() {
        return "text/xml";
    }

    @Override
    public InputStream getStream() throws IOException {
        return stream;
    }

    @Override
    public void prepareResponse(Response response) {
        Serializer serializer = new Persister();
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            serializer.write(outputObject, output);
            stream = new ByteArrayInputStream(output.toByteArray());
            
            response.setHeader("Content-Length", Integer.toString(output.size()));
        } catch (Exception e) {
            LOG.error("could not write stream", e);

            stream = IOUtils.toInputStream(XML_ERROR);
        }
    }
}
