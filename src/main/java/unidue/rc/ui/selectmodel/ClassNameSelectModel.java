/**
 * Copyright (C) 2014 - 2016 Universitaet Duisburg-Essen (semapp|uni-due.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
