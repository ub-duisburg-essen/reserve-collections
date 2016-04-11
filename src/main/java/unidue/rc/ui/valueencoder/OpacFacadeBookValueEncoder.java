package unidue.rc.ui.valueencoder;


import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.services.ValueEncoderFactory;
import unidue.rc.model.OpacFacadeBook;
import unidue.rc.system.OpacFacadeService;

/**
 * @author Nils Verheyen
 * @since 20.09.13 15:05
 */
public class OpacFacadeBookValueEncoder implements ValueEncoder<OpacFacadeBook>, ValueEncoderFactory<OpacFacadeBook> {

    private final OpacFacadeService service;

    public OpacFacadeBookValueEncoder(OpacFacadeService service) {
        this.service = service;
    }

    @Override
    public String toClient(OpacFacadeBook value) {
        return value.getDocNumber();
    }

    @Override
    public OpacFacadeBook toValue(String clientValue) {
        return service.getDetails(clientValue);
    }

    @Override
    public ValueEncoder<OpacFacadeBook> create(Class<OpacFacadeBook> type) {
        return this;
    }
}
