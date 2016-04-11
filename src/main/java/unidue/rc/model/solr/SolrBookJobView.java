package unidue.rc.model.solr;


import org.apache.solr.client.solrj.beans.Field;

import java.util.Date;

/**
 * @author Nils Verheyen
 * @since 25.11.14 09:58
 */
public class SolrBookJobView {

    public static final String JOB_ID_PROPERTY = "jobID";
    public static final String JOB_ID_NUMERIC_PROPERTY = "jobIDNumeric";
    public static final String COLLECTION_ID_PROPERTY = "collectionID";
    public static final String COLLECTION_NUMBER_PROPERTY = "collectionNumber";
    public static final String COLLECTION_NUMBER_NUMERIC_PROPERTY = "collectionNumberNumeric";
    public static final String MODIFIED_PROPERTY = "modified";
    public static final String SIGNATURE_PROPERTY = "signature";
    public static final String TITLE_PROPERTY = "title";
    public static final String STATUS_PROPERTY = "status";
    public static final String LOCATION_PROPERTY = "locationName";
    public static final String LOCATION_ID_PROPERTY = "locationID";
    public static final String SEARCH_FIELD_PROPERTY = "text";

    @Field(JOB_ID_PROPERTY)
    private String jobID;

    @Field(JOB_ID_NUMERIC_PROPERTY)
    private int jobIDNumeric;

    @Field(COLLECTION_ID_PROPERTY)
    private int collectionID;

    @Field(COLLECTION_NUMBER_PROPERTY)
    private String collectionNumber;

    @Field(COLLECTION_NUMBER_NUMERIC_PROPERTY)
    private int collectionNumberNumeric;

    @Field(MODIFIED_PROPERTY)
    private Date modified;

    @Field(SIGNATURE_PROPERTY)
    private String signature;

    @Field(TITLE_PROPERTY)
    private String title;

    @Field(LOCATION_PROPERTY)
    private String location;

    @Field(LOCATION_ID_PROPERTY)
    private int locationID;

    @Field(STATUS_PROPERTY)
    private int status;

    public String getJobID() {
        return jobID;
    }

    public void setJobID(String jobID) {
        this.jobID = jobID;
    }

    public int getJobIDNumeric() {
        return jobIDNumeric;
    }

    public void setJobIDNumeric(int jobIDNumeric) {
        this.jobIDNumeric = jobIDNumeric;
    }

    public int getCollectionID() {
        return collectionID;
    }

    public void setCollectionID(int collectionID) {
        this.collectionID = collectionID;
    }

    public String getCollectionNumber() {
        return collectionNumber;
    }

    public void setCollectionNumber(String collectionNumber) {
        this.collectionNumber = collectionNumber;
    }

    public int getCollectionNumberNumeric() {
        return collectionNumberNumeric;
    }

    public void setCollectionNumberNumeric(int collectionNumberNumeric) {
        this.collectionNumberNumeric = collectionNumberNumeric;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getLocationID() {
        return locationID;
    }

    public void setLocationID(int locationID) {
        this.locationID = locationID;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SolrBookJobView)) return false;

        SolrBookJobView that = (SolrBookJobView) o;

        if (collectionID != that.collectionID) return false;
        if (locationID != that.locationID) return false;
        if (status != that.status) return false;
        if (!collectionNumber.equals(that.collectionNumber)) return false;
        if (!jobID.equals(that.jobID)) return false;
        if (!location.equals(that.location)) return false;
        if (!modified.equals(that.modified)) return false;
        if (signature != null
            ? !signature.equals(that.signature)
            : that.signature != null) return false;
        if (!title.equals(that.title)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = jobID.hashCode();
        result = 31 * result + collectionID;
        result = 31 * result + collectionNumber.hashCode();
        result = 31 * result + modified.hashCode();
        result = 31 * result + (signature != null
                                ? signature.hashCode()
                                : 0);
        result = 31 * result + title.hashCode();
        result = 31 * result + location.hashCode();
        result = 31 * result + locationID;
        result = 31 * result + status;
        return result;
    }
}
