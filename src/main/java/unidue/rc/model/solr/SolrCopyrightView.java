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
 * @since 25.11.14 12:24
 */
public class SolrCopyrightView {

    public static final String COLLECTION_ID_PROPERTY = "collectionID";
    public static final String COLLECTION_NUMBER_PROPERTY = "collectionNumber";
    public static final String COLLECTION_NUMBER_NUMERIC_PROPERTY = "collectionNumberNumeric";
    public static final String RESOURCE_ID_PROPERTY = "resourceID";
    public static final String ENTRY_ID_PROPERTY = "entryID";
    public static final String ENTRY_ID_NUMERIC_PROPERTY = "entryIDNumeric";
    public static final String MODIFIED_PROPERTY = "modified";
    public static final String FILE_NAME_PROPERTY = "fileName";
    public static final String MIME_TYPE_PROPERTY = "mimeType";
    public static final String COLLECTION_TITLE_PROPERTY = "collectionTitle";
    public static final String REVIEW_STATUS_PROPERTY = "reviewStatus";
    public static final String SEARCH_FIELD_PROPERTY = "text";

    @Field(COLLECTION_ID_PROPERTY)
    private int collectionID;

    @Field(COLLECTION_NUMBER_PROPERTY)
    private String collectionNumber;

    @Field(COLLECTION_NUMBER_NUMERIC_PROPERTY)
    private int collectionNumberNumeric;

    @Field(RESOURCE_ID_PROPERTY)
    private int resourceID;

    @Field(ENTRY_ID_PROPERTY)
    private String entryID;

    @Field(ENTRY_ID_NUMERIC_PROPERTY)
    private int entryIDNumeric;

    @Field(MODIFIED_PROPERTY)
    private Date modified;

    @Field(FILE_NAME_PROPERTY)
    private String fileName;

    @Field(MIME_TYPE_PROPERTY)
    private String mimeType;

    @Field(COLLECTION_TITLE_PROPERTY)
    private String collectionTitle;

    @Field(REVIEW_STATUS_PROPERTY)
    private int reviewStatus;

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

    public int getResourceID() {
        return resourceID;
    }

    public void setResourceID(int resourceID) {
        this.resourceID = resourceID;
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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getCollectionTitle() {
        return collectionTitle;
    }

    public void setCollectionTitle(String collectionTitle) {
        this.collectionTitle = collectionTitle;
    }

    public int getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(int reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SolrCopyrightView)) return false;

        SolrCopyrightView that = (SolrCopyrightView) o;

        if (collectionID != that.collectionID) return false;
        if (resourceID != that.resourceID) return false;
        if (reviewStatus != that.reviewStatus) return false;
        if (!collectionNumber.equals(that.collectionNumber)) return false;
        if (!collectionTitle.equals(that.collectionTitle)) return false;
        if (!entryID.equals(that.entryID)) return false;
        if (!fileName.equals(that.fileName)) return false;
        if (!mimeType.equals(that.mimeType)) return false;
        if (!modified.equals(that.modified)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = collectionID;
        result = 31 * result + collectionNumber.hashCode();
        result = 31 * result + resourceID;
        result = 31 * result + entryID.hashCode();
        result = 31 * result + modified.hashCode();
        result = 31 * result + fileName.hashCode();
        result = 31 * result + mimeType.hashCode();
        result = 31 * result + collectionTitle.hashCode();
        result = 31 * result + reviewStatus;
        return result;
    }
}
