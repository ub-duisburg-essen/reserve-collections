package unidue.rc.ui.selectmodel;

/*
 * #%L
 * Semesterapparate
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 Universitaet Duisburg Essen
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
