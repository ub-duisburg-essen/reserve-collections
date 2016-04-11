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
package unidue.rc.model;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @author Nils Verheyen
 * @since 18.09.13 11:22
 */
public class OpacFacadeLibraryData {

    @JsonProperty(value = "totalItemCount")
    private Integer totalItemCount;

    @JsonProperty(value = "borrowableCount")
    private Integer borrowableCount;

    @JsonProperty(value = "heldItems")
    private Integer heldItems;

    @JsonProperty(value = "requested")
    private Integer requested;

    @JsonProperty(value = "expected")
    private Integer expected;

    @JsonProperty(value = "items")
    private List<OpacFacadeLibraryDataItem> items;

    public Integer getTotalItemCount() {
        return totalItemCount;
    }

    public Integer getBorrowableCount() {
        return borrowableCount;
    }

    public Integer getHeldItems() {
        return heldItems;
    }

    public Integer getRequested() {
        return requested;
    }

    public Integer getExpected() {
        return expected;
    }

    public List<OpacFacadeLibraryDataItem> getItems() {
        return items;
    }
}
