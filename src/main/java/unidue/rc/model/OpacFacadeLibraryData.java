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
