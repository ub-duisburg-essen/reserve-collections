package unidue.rc.ui.valueencoder;


import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.services.ValueEncoderFactory;
import unidue.rc.dao.ActionDAO;
import unidue.rc.model.Action;


public class ActionValueEncoder implements ValueEncoder<Action>, ValueEncoderFactory<Action> {

    private ActionDAO dao;

    public ActionValueEncoder(ActionDAO dao) {
        this.dao = dao;
    }

    @Override
    public ValueEncoder<Action> create(Class<Action> type) {
        return this;
    }

    @Override
    public String toClient(Action value) {
        return value.getId().toString();
    }

    @Override
    public Action toValue(String clientValue) {
        return dao.getActionById(Integer.valueOf(clientValue));
    }

}
