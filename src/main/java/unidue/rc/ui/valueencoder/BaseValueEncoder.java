package unidue.rc.ui.valueencoder;


import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.services.ValueEncoderFactory;
import unidue.rc.dao.BaseDAO;
import unidue.rc.model.IntPrimaryKey;

/**
 * @author Nils Verheyen
 * @since 29.08.13 11:15
 */
public class BaseValueEncoder<T extends IntPrimaryKey> implements ValueEncoder<T>, ValueEncoderFactory<T> {

    private final BaseDAO dao;
    private final Class<T> clazz;

    public BaseValueEncoder(Class<T> clazz, BaseDAO dao) {
        this.clazz = clazz;
        this.dao = dao;
    }

    @Override
    public String toClient(T value) {
        return value.getId().toString();
    }

    @Override
    public T toValue(String clientValue) {
        return dao.get(clazz, Integer.valueOf(clientValue));
    }

    @Override
    public ValueEncoder<T> create(Class<T> type) {
        return this;
    }
}
