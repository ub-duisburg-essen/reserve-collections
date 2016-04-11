package unidue.rc.ui.valueencoder;


import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.services.ValueEncoderFactory;
import unidue.rc.dao.RoleDAO;
import unidue.rc.model.Role;

public class RoleValueEncoder implements ValueEncoder<Role>, ValueEncoderFactory<Role> {

    private RoleDAO dao;

    public RoleValueEncoder(RoleDAO dao) {
        this.dao = dao;
    }

    @Override
    public String toClient(Role value) {
        return value.getId().toString();
    }

    @Override
    public Role toValue(String clientValue) {
        return dao.getRoleById(Integer.valueOf(clientValue));
    }

    @Override
    public ValueEncoder<Role> create(Class<Role> type) {
        return this;
    }

}
