package unidue.rc.ui.valueencoder;


import miless.model.MCRCategory;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.services.ValueEncoderFactory;
import unidue.rc.dao.MCRCategoryDAO;

public class MCRCategoryValueEncoder implements ValueEncoder<MCRCategory>, ValueEncoderFactory<MCRCategory> {

    private MCRCategoryDAO dao;

    public MCRCategoryValueEncoder(MCRCategoryDAO dao) {
        this.dao = dao;
    }

    @Override
    public ValueEncoder<MCRCategory> create(Class<MCRCategory> type) {
        return this;
    }

    @Override
    public String toClient(MCRCategory value) {
        return value.getInternalId().toString();
    }

    @Override
    public MCRCategory toValue(String internalId) {
        return dao.getCategoryById(Integer.valueOf(internalId));
    }

}
