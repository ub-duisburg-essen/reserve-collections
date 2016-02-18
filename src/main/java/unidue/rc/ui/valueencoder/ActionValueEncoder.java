package unidue.rc.ui.valueencoder;

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
