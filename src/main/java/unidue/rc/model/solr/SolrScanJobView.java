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
package unidue.rc.model.solr;


import org.apache.solr.client.solrj.beans.Field;

import java.util.Date;

/**
 * @author Nils Verheyen
 * @since 03.02.14 10:29
 */
public class SolrScanJobView {

    public static final String JOB_ID_PROPERTY = "jobID";
    public static final String COLLECTION_ID_PROPERTY = "collectionID";
    public static final String COLLECTION_NUMBER_PROPERTY = "collectionNumber";
    public static final String ENTRY_ID_PROPERTY = "entryID";
    public static final String JOB_MODIFIED_PROPERTY = "jobModified";
    public static final String SCANNABLE_MODIFIED_PROPERTY = "scannableModified";
    public static final String SCANNABLE_TYPE_PROPERTY = "scannableType";
    public static final String SIGNATURE_PROPERTY = "signature";
    public static final String TITLE_PROPERTY = "title";
    public static final String JOB_STATUS_PROPERTY = "jobStatus";
    public static final String JOB_STATUS_LABEL_PROPERTY = "jobStatusLabel";
    public static final String LOCATION_PROPERTY = "locationName";
    public static final String LOCATION_ID_PROPERTY = "locationID";
    public static final String REVISER_PROPERTY = "reviser";
    public static final String REVISER_ID_PROPERTY = "reviserID";
    public static final String SEARCH_FIELD_PROPERTY = "text";
    public static final String PAGE_START_FIELD_PROPERTY = "pageStart";
    public static final String PAGE_END_FIELD_PROPERTY = "pageEnd";

    /**
     * Contains the id of the job.
     */
    @Field(JOB_ID_PROPERTY)
    private int jobID;

    /**
     * Contains the ID of the reserve collection the book jobs book belongs to.
     */
    @Field(COLLECTION_ID_PROPERTY)
    private Integer reserveCollectionID;

    /**
     * Contains the number of the reserve collection the book jobs book belongs to.
     */
    @Field(COLLECTION_NUMBER_PROPERTY)
    private Integer collectionNumber;

    /**
     * Contains the ID of the entry the book jobs book belongs to.
     */
    @Field(ENTRY_ID_PROPERTY)
    private Integer entryID;

    /**
     * Contains the last date of modification of the {@link unidue.rc.model.ScanJob}.
     */
    @Field(JOB_MODIFIED_PROPERTY)
    private Date jobModified;

    /**
     * Contains the last date of modification of the {@link unidue.rc.model.Scannable}.
     */
    @Field(SCANNABLE_MODIFIED_PROPERTY)
    private Date scannableModified;

    /**
     * Contains the concrete type of the {@link unidue.rc.model.Scannable}.
     */
    @Field(SCANNABLE_TYPE_PROPERTY)
    private String scannableType;

    /**
     * Contains the signature of the {@link unidue.rc.model.ScanJob}.
     */
    @Field(SIGNATURE_PROPERTY)
    private String signature;

    /**
     * Contains the title of the {@link unidue.rc.model.ScanJob}.
     */
    @Field(TITLE_PROPERTY)
    private String title;

    /**
     * Contains the job status of the {@link unidue.rc.model.ScanJob}.
     */
    @Field(JOB_STATUS_PROPERTY)
    private int status;

    /**
     * Contains the job status of the {@link unidue.rc.model.ScanJob}.
     */
    @Field(JOB_STATUS_LABEL_PROPERTY)
    private String statusLabel;

    /**
     * Contains the location name of the reserve collection.
     */
    @Field(LOCATION_PROPERTY)
    private String location;

    /**
     * Contains the location id of the reserve collection.
     */
    @Field(LOCATION_ID_PROPERTY)
    private int locationID;

    /**
     * Contains the location name of the reserve collection.
     */
    @Field(REVISER_ID_PROPERTY)
    private int reviserID;

    /**
     * Contains the location name of the reserve collection.
     */
    @Field(REVISER_PROPERTY)
    private String reviser;

    @Field(PAGE_START_FIELD_PROPERTY)
    private String pageStart;

    @Field(PAGE_END_FIELD_PROPERTY)
    private String pageEnd;

    public int getJobID() {
        return jobID;
    }

    public void setJobID(int jobID) {
        this.jobID = jobID;
    }

    public Integer getReserveCollectionID() {
        return reserveCollectionID;
    }

    public void setReserveCollectionID(Integer reserveCollectionID) {
        this.reserveCollectionID = reserveCollectionID;
    }

    public Integer getCollectionNumber() {
        return collectionNumber;
    }

    public void setCollectionNumber(Integer collectionNumber) {
        this.collectionNumber = collectionNumber;
    }

    public Integer getEntryID() {
        return entryID;
    }

    public void setEntryID(Integer entryID) {
        this.entryID = entryID;
    }

    public Date getJobModified() {
        return jobModified;
    }

    public void setJobModified(Date jobModified) {
        this.jobModified = jobModified;
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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getStatusLabel() {
        return statusLabel;
    }

    public void setStatusLabel(String statusLabel) {
        this.statusLabel = statusLabel;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getReviser() {
        return reviser;
    }

    public void setReviser(String reviser) {
        this.reviser = reviser;
    }

    public int getLocationID() {
        return locationID;
    }

    public void setLocationID(int locationID) {
        this.locationID = locationID;
    }

    public int getReviserID() {
        return reviserID;
    }

    public void setReviserID(int reviserID) {
        this.reviserID = reviserID;
    }

    public Date getScannableModified() {
        return scannableModified;
    }

    public void setScannableModified(Date scannableModified) {
        this.scannableModified = scannableModified;
    }

    public String getScannableType() {
        return scannableType;
    }

    public void setScannableType(String scannableType) {
        this.scannableType = scannableType;
    }

    public String getPageStart() {
        return pageStart;
    }

    public void setPageStart(String pageStart) {
        this.pageStart = pageStart;
    }

    public String getPageEnd() {
        return pageEnd;
    }

    public void setPageEnd(String pageEnd) {
        this.pageEnd = pageEnd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SolrScanJobView that = (SolrScanJobView) o;

        if (jobID != that.jobID) return false;
        if (status != that.status) return false;
        if (locationID != that.locationID) return false;
        if (reviserID != that.reviserID) return false;
        if (!reserveCollectionID.equals(that.reserveCollectionID)) return false;
        if (!collectionNumber.equals(that.collectionNumber)) return false;
        if (!entryID.equals(that.entryID)) return false;
        if (jobModified != null
            ? !jobModified.equals(that.jobModified)
            : that.jobModified != null) return false;
        if (scannableModified != null
            ? !scannableModified.equals(that.scannableModified)
            : that.scannableModified != null) return false;
        if (!scannableType.equals(that.scannableType)) return false;
        if (signature != null
            ? !signature.equals(that.signature)
            : that.signature != null) return false;
        if (title != null
            ? !title.equals(that.title)
            : that.title != null) return false;
        if (location != null
            ? !location.equals(that.location)
            : that.location != null) return false;
        if (reviser != null
            ? !reviser.equals(that.reviser)
            : that.reviser != null) return false;
        if (pageStart != null
            ? !pageStart.equals(that.pageStart)
            : that.pageStart != null) return false;
        return pageEnd != null
               ? pageEnd.equals(that.pageEnd)
               : that.pageEnd == null;

    }

    @Override
    public int hashCode() {
        int result = jobID;
        result = 31 * result + reserveCollectionID.hashCode();
        result = 31 * result + collectionNumber.hashCode();
        result = 31 * result + entryID.hashCode();
        result = 31 * result + (jobModified != null
                                ? jobModified.hashCode()
                                : 0);
        result = 31 * result + (scannableModified != null
                                ? scannableModified.hashCode()
                                : 0);
        result = 31 * result + scannableType.hashCode();
        result = 31 * result + (signature != null
                                ? signature.hashCode()
                                : 0);
        result = 31 * result + (title != null
                                ? title.hashCode()
                                : 0);
        result = 31 * result + status;
        result = 31 * result + (location != null
                                ? location.hashCode()
                                : 0);
        result = 31 * result + locationID;
        result = 31 * result + reviserID;
        result = 31 * result + (reviser != null
                                ? reviser.hashCode()
                                : 0);
        result = 31 * result + (pageStart != null
                                ? pageStart.hashCode()
                                : 0);
        result = 31 * result + (pageEnd != null
                                ? pageEnd.hashCode()
                                : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SolrScanJobView{");
        sb.append("jobID=").append(jobID);
        sb.append(", entryID='").append(entryID).append('\'');
        sb.append(", title='").append(title).append('\'');
        sb.append(", status=").append(status);
        sb.append('}');
        return sb.toString();
    }
}
