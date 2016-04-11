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
    public static final String COLLECTION_NUMBER_NUMERIC_PROPERTY = "collectionNumberNumeric";
    public static final String ENTRY_ID_PROPERTY = "entryID";
    public static final String ENTRY_ID_NUMERIC_PROPERTY = "entryIDNumeric";
    public static final String MODIFIED_PROPERTY = "modified";
    public static final String SCANNABLE_MODIFIED_PROPERTY = "scannableModified";
    public static final String JOURNAL_SIGNATURE_PROPERTY = "journalSignature";
    public static final String JOURNAL_TITLE_PROPERTY = "journalTitle";
    public static final String BOOK_SIGNATURE_PROPERTY = "bookSignature";
    public static final String BOOK_TITLE_PROPERTY = "bookTitle";
    public static final String JOB_STATUS_PROPERTY = "jobStatus";
    public static final String LOCATION_PROPERTY = "locationName";
    public static final String LOCATION_ID_PROPERTY = "locationID";
    public static final String REVISER_PROPERTY = "reviser";
    public static final String REVISER_ID_PROPERTY = "reviserID";
    public static final String SEARCH_FIELD_PROPERTY = "text";
    public static final String CHAPTER_PAGE_START_FIELD_PROPERTY = "chapterPageStart";
    public static final String CHAPTER_PAGE_END_FIELD_PROPERTY = "chapterPageEnd";
    public static final String ARTICLE_PAGE_START_FIELD_PROPERTY = "articlePageStart";
    public static final String ARTICLE_PAGE_END_FIELD_PROPERTY = "articlePageEnd";

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
    private String collectionNumber;

    @Field(COLLECTION_NUMBER_NUMERIC_PROPERTY)
    private int collectionNumberNumeric;

    /**
     * Contains the ID of the entry the book jobs book belongs to.
     */
    @Field(ENTRY_ID_PROPERTY)
    private String entryID;

    @Field(ENTRY_ID_NUMERIC_PROPERTY)
    private int entryIDNumeric;

    /**
     * Contains the last date of modification of the {@link unidue.rc.model.Book}.
     */
    @Field(MODIFIED_PROPERTY)
    private Date modified;

    /**
     * Contains the last date of modification of the {@link unidue.rc.model.Book}.
     */
    @Field(SCANNABLE_MODIFIED_PROPERTY)
    private Date scannableModified;

    /**
     * Contains the signature of the {@link unidue.rc.model.ScanJob}.
     */
    @Field(JOURNAL_SIGNATURE_PROPERTY)
    private String journalSignature;

    /**
     * Contains the title of the {@link unidue.rc.model.ScanJob}.
     */
    @Field(JOURNAL_TITLE_PROPERTY)
    private String journalTitle;

    /**
     * Contains the signature of the {@link unidue.rc.model.ScanJob}.
     */
    @Field(BOOK_SIGNATURE_PROPERTY)
    private String bookSignature;

    /**
     * Contains the title of the {@link unidue.rc.model.ScanJob}.
     */
    @Field(BOOK_TITLE_PROPERTY)
    private String bookTitle;

    /**
     * Contains the job status of the {@link unidue.rc.model.BookJob}.
     */
    @Field(JOB_STATUS_PROPERTY)
    private int status;

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

    @Field(CHAPTER_PAGE_START_FIELD_PROPERTY)
    private String chapterPageStart;

    @Field(CHAPTER_PAGE_END_FIELD_PROPERTY)
    private String chapterPageEnd;

    @Field(ARTICLE_PAGE_START_FIELD_PROPERTY)
    private String articlePageStart;

    @Field(ARTICLE_PAGE_END_FIELD_PROPERTY)
    private String articlePageEnd;

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

    public String getEntryID() {
        return entryID;
    }

    public void setEntryID(String entryID) {
        this.entryID = entryID;
    }

    public int getEntryIDNumeric() {
        return entryIDNumeric;
    }

    public void setEntryIDNumeric(int entryIDNumeric) {
        this.entryIDNumeric = entryIDNumeric;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public String getJournalSignature() {
        return journalSignature;
    }

    public void setJournalSignature(String signature) {
        this.journalSignature = signature;
    }

    public String getJournalTitle() {
        return journalTitle;
    }

    public void setJournalTitle(String title) {
        this.journalTitle = title;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
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

    public String getBookSignature() {
        return bookSignature;
    }

    public void setBookSignature(String bookSignature) {
        this.bookSignature = bookSignature;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public Date getScannableModified() {
        return scannableModified;
    }

    public void setScannableModified(Date scannableModified) {
        this.scannableModified = scannableModified;
    }

    public String getTitle() {
        return bookTitle != null ? bookTitle : journalTitle;
    }

    public String getSignature() {
        return bookSignature != null ? bookSignature : journalSignature;
    }

    public String getChapterPageStart() {
        return chapterPageStart;
    }

    public void setChapterPageStart(String chapterPageStart) {
        this.chapterPageStart = chapterPageStart;
    }

    public String getChapterPageEnd() {
        return chapterPageEnd;
    }

    public void setChapterPageEnd(String chapterPageEnd) {
        this.chapterPageEnd = chapterPageEnd;
    }

    public String getArticlePageStart() {
        return articlePageStart;
    }

    public void setArticlePageStart(String articlePageStart) {
        this.articlePageStart = articlePageStart;
    }

    public String getArticlePageEnd() {
        return articlePageEnd;
    }

    public void setArticlePageEnd(String articlePageEnd) {
        this.articlePageEnd = articlePageEnd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SolrScanJobView)) return false;

        SolrScanJobView that = (SolrScanJobView) o;

        if (jobID != that.jobID) return false;
        if (locationID != that.locationID) return false;
        if (reviserID != that.reviserID) return false;
        if (status != that.status) return false;
        if (bookSignature != null
            ? !bookSignature.equals(that.bookSignature)
            : that.bookSignature != null) return false;
        if (bookTitle != null
            ? !bookTitle.equals(that.bookTitle)
            : that.bookTitle != null) return false;
        if (!collectionNumber.equals(that.collectionNumber)) return false;
        if (!entryID.equals(that.entryID)) return false;
        if (journalSignature != null
            ? !journalSignature.equals(that.journalSignature)
            : that.journalSignature != null) return false;
        if (journalTitle != null
            ? !journalTitle.equals(that.journalTitle)
            : that.journalTitle != null) return false;
        if (location != null
            ? !location.equals(that.location)
            : that.location != null) return false;
        if (modified != null
            ? !modified.equals(that.modified)
            : that.modified != null) return false;
        if (!reserveCollectionID.equals(that.reserveCollectionID)) return false;
        if (reviser != null
            ? !reviser.equals(that.reviser)
            : that.reviser != null) return false;
        if (scannableModified != null
            ? !scannableModified.equals(that.scannableModified)
            : that.scannableModified != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = jobID;
        result = 31 * result + reserveCollectionID.hashCode();
        result = 31 * result + collectionNumber.hashCode();
        result = 31 * result + entryID.hashCode();
        result = 31 * result + (modified != null
                                ? modified.hashCode()
                                : 0);
        result = 31 * result + (scannableModified != null
                                ? scannableModified.hashCode()
                                : 0);
        result = 31 * result + (journalSignature != null
                                ? journalSignature.hashCode()
                                : 0);
        result = 31 * result + (journalTitle != null
                                ? journalTitle.hashCode()
                                : 0);
        result = 31 * result + (bookSignature != null
                                ? bookSignature.hashCode()
                                : 0);
        result = 31 * result + (bookTitle != null
                                ? bookTitle.hashCode()
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
        return result;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("SolrScanJobView{");
        sb.append("jobID=").append(jobID);
        sb.append(", entryID='").append(entryID).append('\'');
        sb.append(", journalTitle='").append(journalTitle).append('\'');
        sb.append(", bookTitle='").append(bookTitle).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
