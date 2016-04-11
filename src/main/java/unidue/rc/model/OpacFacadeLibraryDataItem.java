package unidue.rc.model;


import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Nils Verheyen
 * @since 18.09.13 11:23
 */
public class OpacFacadeLibraryDataItem {

    @JsonProperty(value = "signature")
    private String signature;

    @JsonProperty(value = "barcode")
    private String barcode;

    @JsonProperty(value = "loanDueDate")
    private String loanDueDate;

    @JsonProperty(value = "itemStatus")
    private String itemStatus;

    @JsonProperty(value = "location")
    private String location;

    @JsonProperty(value = "collection")
    private String collection;

    @JsonProperty(value = "isLoaned")
    private Boolean isLoaned;

    @JsonProperty(value = "isProvided")
    private Boolean isProvided;

    @JsonProperty(value = "isExpected")
    private Boolean isExpected;

    @JsonProperty(value = "isRequested")
    private Boolean isRequested;

    @JsonProperty(value = "isHoldable")
    private Boolean isHoldable;

    public String getSignature() {
        return signature;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getLoanDueDate() {
        return loanDueDate;
    }

    public String getItemStatus() {
        return itemStatus;
    }

    public String getLocation() {
        return location;
    }

    public String getCollection() {
        return collection;
    }

    public Boolean getLoaned() {
        return isLoaned;
    }

    public Boolean getExpected() {
        return isExpected;
    }

    public Boolean getRequested() {
        return isRequested;
    }

    public Boolean getIsProvided() {
        return isProvided;
    }

    public Boolean getIsHoldable() {
        return isHoldable;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public void setLoanDueDate(String loanDueDate) {
        this.loanDueDate = loanDueDate;
    }

    public void setItemStatus(String itemStatus) {
        this.itemStatus = itemStatus;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public void setIsLoaned(Boolean isLoaned) {
        this.isLoaned = isLoaned;
    }

    public void setIsProvided(Boolean isProvided) {
        this.isProvided = isProvided;
    }

    public void setIsExpected(Boolean isExpected) {
        this.isExpected = isExpected;
    }

    public void setIsRequested(Boolean isRequested) {
        this.isRequested = isRequested;
    }

    public void setIsHoldable(Boolean isHoldable) {
        this.isHoldable = isHoldable;
    }
}
