package unidue.rc.model;

/*
 * #%L
 * Semesterapparate
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 - 2015 Universitaet Duisburg Essen
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

import org.apache.commons.lang3.builder.ToStringBuilder;
import unidue.rc.model.auto._Mail;

import java.util.List;
import java.util.stream.Collectors;

public class Mail extends _Mail implements IntPrimaryKey {

    @Override
    public Integer getId() {
        return CayenneUtils.getID(this, ID_PK_COLUMN);
    }

    public List<String> getNodes(MailNodeType nodeType) {
        return getNodes().stream()
                .filter(node -> node.getType().equals(nodeType))
                .map(node -> node.getValue())
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append(ID_PK_COLUMN, getId())
                .append(FROM_PROPERTY, getFrom())
                .append(SUBJECT_PROPERTY, getSubject())
                .append(NUM_TRIES_PROPERTY, getNumTries())
                .toString();
    }
}
