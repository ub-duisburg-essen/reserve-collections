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

import com.googlecode.tapestry5cayenne.components.Select;
import org.apache.tapestry5.OptionGroupModel;
import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.corelib.components.Palette;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.util.AbstractSelectModel;
import unidue.rc.model.Action;
import unidue.rc.model.ActionDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * An <code>ActionSelectModel</code> can be used as a {@link SelectModel} to
 * display {@link Action}s inside a {@link Select} or {@link Palette} component.
 *
 * @author Nils Verheyen
 */
public class ActionSelectModel extends AbstractSelectModel {

    private List<Action> actions;

    private Messages messages;

    public ActionSelectModel(List<Action> actions, Messages messages) {
        this.actions = actions;
        this.messages = messages;
    }

    @Override
    public List<OptionGroupModel> getOptionGroups() {
        return null;
    }
    
    @Override
    public List<OptionModel> getOptions() {
        return actions
                .stream()
                .map(this::create)
                .sorted((o1, o2) -> o1.getLabel().compareTo(o2.getLabel()))
                .collect(Collectors.toList());
    }

    private OptionModel create(final Action action) {

        return new OptionModel() {
            /**
             * Returns the label from the {@link Messages} catalogue if one is given,
             * otherwise the name of the {@link ActionDefinition} value itself.
             */
            @Override
            public String getLabel() {

                String resource = action.getResource();
                String name = action.getName();

                String labelKey = resource + "." + name;

                String label = messages.contains(labelKey) ? messages.get(labelKey) : labelKey;
                return label;
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
                return action;
            }
        };
    }

    public void removeAction(Action action) {
        this.actions.remove(action);
    }
}
