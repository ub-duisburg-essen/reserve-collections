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

/**
 * @author Nils Verheyen
 * @since 16.09.13 15:50
 */
public class OpacFacadeBook {

    private static final String REGEX_NUMBER = "[0-9]+";

    @JsonProperty(value = "title")
    private String title;

    @JsonProperty(value = "editor", required = false)
    private String editor;

    @JsonProperty(value = "edition", required = false)
    private String edition;

    @JsonProperty(value = "volume", required = false)
    private String volume;

    @JsonProperty(value = "docNumber", required = false)
    private String docNumber;

    @JsonProperty(value = "author", required = false)
    private String author;

    @JsonProperty(value = "year", required = false)
    private String year;

    @JsonProperty(value = "isbn", required = false)
    private String isbn;

    @JsonProperty(value = "thumbnailURL", required = false)
    private String thumbnailURL;

    @JsonProperty(value = "publishingLocation", required = false)
    private String publishingLocation;

    @JsonProperty(value = "description", required = false)
    private String description;

    @JsonProperty(value = "lang", required = false)
    private String lang;

    @JsonProperty(value = "publisher", required = false)
    private String publisher;

    @JsonProperty(value = "resourceType", required = false)
    private String resourceType;

    public boolean isYearNumeric() {
        return year != null && year.trim().matches(REGEX_NUMBER);
    }

    public String getTitle() {
        return title;
    }

    public String getEditor() {
        return editor;
    }

    public String getEdition() {
        return edition;
    }

    public String getVolume() {
        return volume;
    }

    public String getDocNumber() {
        return docNumber;
    }

    public String getAuthor() {
        return author;
    }

    public String getYear() {
        return year;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getThumbnailURL() {
        return thumbnailURL;
    }

    public String getPublishingLocation() {
        return publishingLocation;
    }

    public String getDescription() {
        return description;
    }

    public String getLang() {
        return lang;
    }

    public String getPublisher() {
        return publisher;
    }

    public String getResourceType() {
        return resourceType;
    }
}
