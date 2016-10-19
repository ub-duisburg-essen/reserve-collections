package unidue.rc.ui.valueencoder;

import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.services.ValueEncoderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by nils on 19.10.16.
 */
public class ClassNameValueEncoder implements ValueEncoder<Class>, ValueEncoderFactory<Class> {

    private static final Logger LOG = LoggerFactory.getLogger(ClassNameValueEncoder.class);

    @Override
    public String toClient(Class value) {
        return value.getName();
    }

    @Override
    public Class toValue(String clientValue) {
        try {
            return Class.forName(clientValue);
        } catch (ClassNotFoundException e) {
            LOG.error("could not load class " + clientValue, e);
            return null;
        }
    }

    @Override
    public ValueEncoder<Class> create(Class<Class> type) {
        return this;
    }
}
