package unidue.rc.plugins.moodle.services;


import org.apache.shiro.authz.AuthorizationException;
import unidue.rc.plugins.moodle.DecryptedRequestData;
import unidue.rc.plugins.moodle.model.Request;
import unidue.rc.ui.RequestError;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;

/**
 * A <code>MoodleWebService</code> can be used by {@link MoodleRequestHandler} objects to run requests
 * to moodles webservice in particular order defined by the request handler.
 *
 * @see MoodleRequestHandlerImpl
 */
public interface MoodleWebService<T extends Request> {

    /**
     * Deserializes the request data that is kept in {@link DecryptedRequestData#requestData}.
     *
     * @param decryptedRequestData contains the decrypted raw request data of the request.
     * @return the deserialized object this web service is defined for
     * @throws XMLStreamException thrown if deserialization was insuccessful
     */
    T deserialize(DecryptedRequestData decryptedRequestData) throws XMLStreamException;

    /**
     * Returns <code>true</code> if the secret used inside a request is valid.
     *
     * @return <code>true</code> if the used secret is valid
     */
    boolean isSecretValid();

    /**
     * Should be called to initialize all data that is used to execute the request.
     *
     * @throws RequestError thrown if any resource could not be loaded
     */
    void loadResources() throws RequestError;

    /**
     * Checks the permissions used for the request object defined by this instance.
     *
     * @param request contains the request object
     * @throws AuthorizationException thrown if the client is not authorized for the service
     */
    void checkPermission(T request) throws AuthorizationException;

    /**
     * Performs the request to the web service.
     *
     * @return must return a xml response that is returned to the client.
     * @throws RequestError thrown if there is any error regarding the request
     * @throws IOException  thrown if the response could not be serialized
     */
    String execute() throws RequestError, IOException;

}
