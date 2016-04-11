package unidue.rc.ui.valueencoder;


import org.apache.tapestry5.ValueEncoder;
import unidue.rc.dao.ReserveCollectionNumberDAO;
import unidue.rc.model.ReserveCollectionNumber;

/**
 * @author Nils Verheyen
 * @since 29.10.13 09:26
 */
public class CollectionNumberValueEncoder implements ValueEncoder<ReserveCollectionNumber> {

    private final ReserveCollectionNumberDAO dao;

    public CollectionNumberValueEncoder(ReserveCollectionNumberDAO dao) {
        this.dao = dao;
    }

    @Override
    public String toClient(ReserveCollectionNumber value) {
        return value.getNumber().toString();
    }

    @Override
    public ReserveCollectionNumber toValue(String clientValue) {
        return dao.getNumber(Integer.valueOf(clientValue));
    }
}
