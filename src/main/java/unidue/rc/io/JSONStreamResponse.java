package unidue.rc.io;


import org.apache.commons.io.IOUtils;
import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.services.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by nils on 21.10.15.
 */
public class JSONStreamResponse implements StreamResponse {

    private static final Logger LOG = LoggerFactory.getLogger(JSONStreamResponse.class);
    private static final String JSON_ERROR = "{ \"errorCode\": 500, \"message\": \"internal server error occurred\" }";

    private final String json;

    private InputStream stream;

    public JSONStreamResponse(String json) {
        this.json = json;
    }

    @Override
    public String getContentType() {
        return "application/json; charset=utf-8";
    }

    @Override
    public InputStream getStream() throws IOException {
        return stream;
    }

    @Override
    public void prepareResponse(Response response) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            IOUtils.write(json, output);
            stream = new ByteArrayInputStream(output.toByteArray());

            response.setHeader("Content-Length", Integer.toString(output.size()));
        } catch (Exception e) {
            LOG.error("could not write stream", e);

            stream = IOUtils.toInputStream(JSON_ERROR);
        }
    }
}
