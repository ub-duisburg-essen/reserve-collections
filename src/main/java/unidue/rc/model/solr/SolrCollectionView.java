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
import java.util.List;
import java.util.stream.Collectors;

/**
 * A <code>SolrCollectionView</code> represents en entity in solr, that contains the simple viewable data of a {@link
 * unidue.rc.model.ReserveCollection}.
 *
 * @author Nils Verheyen
 * @see unidue.rc.search.SolrService.Core#ReserveCollection
 * @since 19.11.14 11:03
 */
public class SolrCollectionView {

    public static final String COLLECTION_ID_PROPERTY = "collectionID";
    public static final String COLLECTION_COMMENT_PROPERTY="collectionComment";
    public static final String COLLECTION_NUMBER_PROPERTY = "collectionNumber";
    public static final String COLLECTION_NUMBER_NUMERIC_PROPERTY = "collectionNumberNumeric";
    public static final String STATUS_PROPERTY = "status";
    public static final String LOCATION_PROPERTY = "location";
    public static final String LOCATION_ID_PROPERTY = "locationID";
    public static final String TITLE_PROPERTY = "title";
    public static final String AUTHORS_PROPERTY = "authors";
    public static final String VALID_TO_PROPERTY = "validTo";
    public static final String DISSOLVE_AT_PROPERTY = "dissolveAt";
    public static final String SEARCH_FIELD_PROPERTY = "text";

    @Field(COLLECTION_ID_PROPERTY)
    String collectionID;

    @Field(COLLECTION_COMMENT_PROPERTY)
    String collectionComment;

    @Field(COLLECTION_NUMBER_PROPERTY)
    String collectionNumber;

    @Field(COLLECTION_NUMBER_NUMERIC_PROPERTY)
    int collectionNumberNumeric;

    @Field(STATUS_PROPERTY)
    String status;

    @Field(LOCATION_PROPERTY)
    String location;

    @Field(LOCATION_ID_PROPERTY)
    int locationID;

    @Field(TITLE_PROPERTY)
    String title;

    @Field(AUTHORS_PROPERTY)
    List<String> authors;

    @Field(VALID_TO_PROPERTY)
    Date validTo;

    @Field(DISSOLVE_AT_PROPERTY)
    Date dissolveAt;

    public String getCollectionID() {
        return collectionID;
    }

    public void setCollectionID(String collectionID) {
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getAuthors() {
        return authors != null
               ? authors.stream()
                       .distinct()
                       .collect(Collectors.toList())
               : null;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public Date getValidTo() {
        return validTo;
    }

    public void setValidTo(Date validTo) {
        this.validTo = validTo;
    }

    public Date getDissolveAt() {
        return dissolveAt;
    }

    public void setDissolveAt(Date dissolveAt) {
        this.dissolveAt = dissolveAt;
    }

    public String getCollectionComment() { return collectionComment; }

    public void setCollectionComment(String collectionComment) { this.collectionComment = collectionComment; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SolrCollectionView)) return false;

        SolrCollectionView that = (SolrCollectionView) o;

        if (locationID != that.locationID) return false;
        if (authors != null
            ? !authors.equals(that.authors)
            : that.authors != null) return false;
        if (!collectionID.equals(that.collectionID)) return false;
        if (!collectionNumber.equals(that.collectionNumber)) return false;
        if (!location.equals(that.location)) return false;
        if (!status.equals(that.status)) return false;
        if (!title.equals(that.title)) return false;
        if (!validTo.equals(that.validTo)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = collectionID.hashCode();
        result = 31 * result + collectionNumber.hashCode();
        result = 31 * result + status.hashCode();
        result = 31 * result + location.hashCode();
        result = 31 * result + locationID;
        result = 31 * result + title.hashCode();
        result = 31 * result + (authors != null
                                ? authors.hashCode()
                                : 0);
        result = 31 * result + validTo.hashCode();

        return result;
    }
}
