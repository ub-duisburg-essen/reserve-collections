package unidue.rc.model;

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

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @author Nils Verheyen
 * @since 17.09.13 10:16
 */
public class OpacFacadeFind {

    @JsonProperty("sessionId")
    private String sessionId;

    @JsonProperty("count")
    private Integer count;

    @JsonProperty("setNumber")
    private Integer setNumber;

    @JsonProperty("records")
    private List<OpacFacadeBook> books;

    public String getSessionId() {
        return sessionId;
    }

    public Integer getCount() {
        return count;
    }

    public Integer getSetNumber() {
        return setNumber;
    }

    public List<OpacFacadeBook> getBooks() {
        return books;
    }

    public boolean isEmpty() {
        return books == null || books.isEmpty();
    }

    public boolean containsBooks() {
        return books != null && books.size() > 0;
    }
}
