package unidue.rc.ui.components;

import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.FormFragment;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import unidue.rc.dao.ReserveCollectionNumberDAO;
import unidue.rc.model.ReserveCollection;

public class NumberIntervalChooser {

    @Property
    @Parameter
    private ReserveCollection collection;

    @Property
    @Validate("required")
    @Parameter(value = "0")
    private Integer from;

    @Property
    @Validate("required")
    @Parameter(value = "100")
    private Integer to;

    @Property
    private Integer number;

    @Property(write = false)
    @Persist(PersistenceConstants.FLASH)
    private String errorMessage;

    @Component(id = "chooseNumberFragment")
    private FormFragment numberFragment;

    @Inject
    private ReserveCollectionNumberDAO numberDAO;

    @Inject
    private Messages messages;

    public void validateNumberForm(Form form) {

        if (from > to)
            form.recordError(messages.get("error.msg.from.larger.than.to"));
    }
}
