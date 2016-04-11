package unidue.rc.ui.selectmodel;


import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.OptionGroupModel;
import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.util.AbstractSelectModel;

import java.text.DateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExpirySelectModel extends AbstractSelectModel {

    private final Map<Calendar, String> options;
    private final Messages messages;

    public ExpirySelectModel(Map<Calendar, String> options, Messages messages) {
        this.options = options;
        this.messages = messages;
    }

    @Override
    public List<OptionGroupModel> getOptionGroups() {
        return null;
    }

    @Override
    public List<OptionModel> getOptions() {

        return options.entrySet()
                .stream()
                .map(entry -> create(entry.getValue(), entry.getKey()))
                .collect(Collectors.toList());
    }

    private OptionModel create(String label, Calendar value) {
        return new OptionModel() {

            @Override
            public String getLabel() {
                return messages.contains(label)
                       ? messages.format(label, value)
                       : DateFormat.getDateInstance().format(value.getTime());
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
                return value;
            }
        };
    }

}
