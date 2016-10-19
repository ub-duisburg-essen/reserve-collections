package unidue.rc.ui.selectmodel;

import org.apache.tapestry5.OptionGroupModel;
import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.util.AbstractSelectModel;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by nils on 17.10.16.
 */
public class ClassNameSelectModel extends AbstractSelectModel {

    private List<Class> classes;

    private Messages messages;

    public ClassNameSelectModel(List<Class> classes, Messages messages) {
        this.classes = classes;
        this.messages = messages;
    }

    @Override
    public List<OptionGroupModel> getOptionGroups() {
        return null;
    }

    @Override
    public List<OptionModel> getOptions() {
        return classes.stream()
                .map(this::toOptionModel)
                .collect(Collectors.toList());
    }

    private OptionModel toOptionModel(Class c) {
        return new OptionModel() {
            @Override
            public String getLabel() {
                return messages.get(c.getName());
            }

            @Override
            public boolean isDisabled() {
                return false;
            }

            @Override
            public Map<String, String> getAttributes() {
                return null;
            }

            @Override
            public Object getValue() {
                return c;
            }
        };
    }
}
