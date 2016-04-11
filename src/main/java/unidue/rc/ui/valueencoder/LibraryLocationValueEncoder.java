package unidue.rc.ui.valueencoder;


import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.services.ValueEncoderFactory;
import unidue.rc.dao.LibraryLocationDAO;
import unidue.rc.model.LibraryLocation;

public class LibraryLocationValueEncoder implements ValueEncoder<LibraryLocation>, ValueEncoderFactory<LibraryLocation> {

    private LibraryLocationDAO dao;

    public LibraryLocationValueEncoder(LibraryLocationDAO dao) {
        this.dao = dao;
    }

    @Override
    public String toClient(LibraryLocation value) {
        return value.getId().toString();
    }

    @Override
    public LibraryLocation toValue(String clientValue) {
        return dao.getLocationById(Integer.valueOf(clientValue));
    }

    @Override
    public ValueEncoder<LibraryLocation> create(Class<LibraryLocation> type) {
        return this;
    }
}
