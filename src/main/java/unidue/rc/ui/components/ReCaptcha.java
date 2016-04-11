package unidue.rc.ui.components;


import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.corelib.base.AbstractField;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.FormSupport;
import org.apache.tapestry5.services.Request;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p> A <code>ReCaptcha</code> can be used to display a captcha inside a formular of a page or other components. Be
 * sure to provide <code>privateKey</code> and <code>publicKey</code> as parameter. </p> <h3>Example:</h3> {@code <div
 * t:type="recaptcha" publicKey="prop:recaptcha.public.key" privateKey="prop:recaptcha.private.key"
 * valid="captchaValid"></div> }
 *
 * @author Nils Verheyen
 * @see <a href="https://developers.google.com/recaptcha/docs/display">Google developers API description</a>
 * @see <a href="http://redmine.ub.uni-due.de/projects/rc-refactoring/wiki/ReCaptcha">UB Redmine Wiki</a>
 */
public class ReCaptcha extends AbstractField {

    @Inject
    private Logger log;

    @Parameter(defaultPrefix = BindingConstants.LITERAL, allowNull = false, required = true)
    @SuppressWarnings("unused")
    private String privateKey;

    @Parameter(defaultPrefix = BindingConstants.LITERAL, allowNull = false, required = true)
    @Property
    @SuppressWarnings("unused")
    private String publicKey;

    @Parameter
    @SuppressWarnings("unused")
    private Boolean valid;

    @Inject
    private FormSupport formSupport;

    @Inject
    private ComponentResources resources;

    @Inject
    private Request request;

    @Inject
    private HttpServletRequest servletRequest;

    private static final String RECAPTCHA_RESPONSE_FIELD = "g-recaptcha-response";

    @SetupRender
    void addProcessSubmissionAction() {
        if (formSupport == null) {
            throw new RuntimeException(String.format(
                    "Component %s must be enclosed by a Form component.",
                    resources.getCompleteId()));
        }
    }

    @Override
    protected void processSubmission(String controlName) {
        String response = request.getParameter(RECAPTCHA_RESPONSE_FIELD);

        valid = verifyResponse(response, servletRequest.getRemoteAddr());
    }

    private Boolean verifyResponse(String response, String ip) {
        Map<String, String> parameters = new HashMap<>();

        parameters.put("secret", privateKey);
        parameters.put("response", response);
        parameters.put("remoteip", ip);

        return post(parameters);
    }

    private Boolean post(Map<String, String> parameter) {
        HttpHost targetHost = new HttpHost("www.google.com", 443, "https");

        // the request itself
        HttpPost post = new HttpPost("/recaptcha/api/siteverify");

        // add parameter as form encoded entity - do not use post.getParams().setParameter()
        List<NameValuePair> postParameters = parameter.keySet()
                .stream()
                .map(p -> new BasicNameValuePair(p, parameter.get(p)))
                .collect(Collectors.toList());
        try {
            post.setEntity(new UrlEncodedFormEntity(postParameters, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            log.error("could not set form entity - " + e.getMessage());
        }

        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            // execute post request
            HttpResponse response = client.execute(targetHost, post);

            String answer = EntityUtils.toString(response.getEntity());
            JSONObject recaptchaResponse = new JSONObject(answer);

            log.debug("response status: " + response.getStatusLine().getStatusCode());
            log.debug("recaptcha response: " + recaptchaResponse);

            /* first line of response contains true or false. if the answer is false
             * the next line contains a error message.
             * see https://developers.google.com/recaptcha/docs/verify for information
             */
            EntityUtils.consume(response.getEntity());
            if (recaptchaResponse.getBoolean("success")) {
                return true;
            } else {
                log.debug("error code: " + recaptchaResponse.getString("error-codes"));
                return false;
            }

        } catch (IOException e) {
            log.error("could not execute request", e);
            return Boolean.FALSE;
        }
    }
}
