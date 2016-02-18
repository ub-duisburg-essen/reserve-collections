package unidue.rc.io;

/*
 * #%L
 * Semesterapparate
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 - 2015 Universitaet Duisburg Essen
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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unidue.rc.model.*;

import java.io.IOException;
import java.util.Date;

/**
 * Created by nils on 06.08.15.
 */
public class CollectionJsonWriter extends StdSerializer<ReserveCollection> implements CollectionVisitor {

    private static final Logger LOG = LoggerFactory.getLogger(CollectionJsonWriter.class);

    private JsonGenerator jsonGenerator;
    private SerializerProvider serializerProvider;

    public CollectionJsonWriter() {
        super(ReserveCollection.class);
    }

    @Override
    public void serialize(ReserveCollection collection, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
        this.jsonGenerator = jsonGenerator;
        this.serializerProvider = serializerProvider;

        collection.accept(this);
    }

    @Override
    public void visit(ReserveCollection collection) {

        try {
            jsonGenerator.writeStartObject();

            // properties
            write(ReserveCollection.ALEPH_SYSTEM_ID_PROPERTY, collection.getAlephSystemId());
            write(ReserveCollection.ALEPH_SYSTEM_ID_PROPERTY, collection.getAlephSystemId());
            write(ReserveCollection.ALEPH_USER_ID_PROPERTY, collection.getAlephUserId());
            write(ReserveCollection.COMMENT_PROPERTY, collection.getComment());
            write(ReserveCollection.CREATED_PROPERTY, collection.getCreated());
            write(ReserveCollection.DISSOLVE_AT_PROPERTY, collection.getDissolveAt());
            write(ReserveCollection.ID_PROPERTY, collection.getId());
            write(ReserveCollection.MODIFIED_PROPERTY, collection.getModified());
            write(ReserveCollection.ORIGIN_ID_PROPERTY, collection.getOriginId());
            write(ReserveCollection.PERMALINK_PROPERTY, collection.getPermalink());
            write(ReserveCollection.READ_KEY_PROPERTY, collection.getReadKey());
            write(ReserveCollection.STATUS_PROPERTY, collection.getStatus());
            write(ReserveCollection.TITLE_PROPERTY, collection.getTitle());
            write(ReserveCollection.VALID_TO_PROPERTY, collection.getValidTo());
            write(ReserveCollection.WRITE_KEY_PROPERTY, collection.getWriteKey());
            write(ReserveCollection.MEDIA_DOWNLOAD_ALLOWED_PROPERTY, collection.isMediaDownloadAllowed());

            // simple relations
            write(ReserveCollection.NUMBER_PROPERTY, collection.getNumber().getNumber());
        } catch (IOException e) {
            LOG.error("could not start write collection " + collection.getId(), e);
        }
    }

    @Override
    public void didVisit(ReserveCollection collection) {

        try {
            jsonGenerator.writeEndObject();
        } catch (IOException e) {
            LOG.error("could not end write collection " + collection.getId(), e);
        }
    }

    @Override
    public void visit(Participation participation) {

        try {
            jsonGenerator.writeStartObject();

            // properties
            write(Participation.END_DATE_PROPERTY, participation.getEndDate());
            write(Participation.ID_PK_COLUMN, participation.getId());
            write(Participation.START_DATE_PROPERTY, participation.getStartDate());
            write(Participation.USER_ID_PROPERTY, participation.getUserId());

            // relations
            jsonGenerator.writeObjectFieldStart(Participation.ROLE_PROPERTY);
            Role role = participation.getRole();
            write(Role.NAME_PROPERTY, role.getName());
            write(Role.ID_PK_COLUMN, role.getId());
            jsonGenerator.writeEndObject();


            jsonGenerator.writeEndObject();
        } catch (IOException e) {
            LOG.error("could not write " + participation.getClass().getSimpleName() + " " + participation.getId(), e);
        }
    }

    @Override
    public void visit(LibraryLocation location) {

        try {
            jsonGenerator.writeObjectFieldStart(ReserveCollection.LIBRARY_LOCATION_PROPERTY);

            // properties
            write(LibraryLocation.ID_PROPERTY, location.getId());
            write(LibraryLocation.NAME_PROPERTY, location.getName());
            write(LibraryLocation.PHYSICAL_PROPERTY, location.isPhysical());

            jsonGenerator.writeEndObject();
        } catch (IOException e) {
            LOG.error("could not write " + location.getClass().getSimpleName() + " " + location.getId(), e);
        }
    }

    @Override
    public void visit(Entry entry) {

        try {
            jsonGenerator.writeStartObject();

            // properties
            write(Entry.CREATED_PROPERTY, entry.getCreated());
            write(Entry.ID_PK_COLUMN, entry.getId());
            write(Entry.MODIFIED_PROPERTY, entry.getModified());
            write(Entry.POSITION_PROPERTY, entry.getPosition());

        } catch (IOException e) {
            LOG.error("could not write " + entry.getClass().getSimpleName() + " " + entry.getId(), e);
        }
    }

    @Override
    public void didVisit(Entry entry) {
        try {

            jsonGenerator.writeEndObject();
        } catch (IOException e) {
            LOG.error("could not write " + entry.getClass().getSimpleName() + " " + entry.getId(), e);
        }
    }

    @Override
    public void visit(Html html) {

        try {
            jsonGenerator.writeObjectFieldStart(Html.class.getSimpleName().toLowerCase());

            // properties
            write(Html.ID_PK_COLUMN, html.getId());
            write(Html.TEXT_PROPERTY, html.getText());

            jsonGenerator.writeEndObject();
        } catch (IOException e) {
            LOG.error("could not write " + html.getClass().getSimpleName() + " " + html.getId(), e);
        }
    }

    @Override
    public void visit(Headline headline) {
        try {
            jsonGenerator.writeObjectFieldStart(Headline.class.getSimpleName().toLowerCase());

            // properties
            write(Headline.ID_PK_COLUMN, headline.getId());
            write(Headline.TEXT_PROPERTY, headline.getText());

            jsonGenerator.writeEndObject();
        } catch (IOException e) {
            LOG.error("could not write " + headline.getClass().getSimpleName() + " " + headline.getId(), e);
        }
    }

    @Override
    public void visit(WebLink webLink) {
        try {
            jsonGenerator.writeObjectFieldStart(WebLink.class.getSimpleName().toLowerCase());

            // properties
            write(WebLink.ID_PK_COLUMN, webLink.getId());
            write(WebLink.NAME_PROPERTY, webLink.getName());
            write(WebLink.URL_PROPERTY, webLink.getUrl());

            jsonGenerator.writeEndObject();
        } catch (IOException e) {
            LOG.error("could not write " + webLink.getClass().getSimpleName() + " " + webLink.getId(), e);
        }
    }

    @Override
    public void visit(Reference reference) {
        try {
            jsonGenerator.writeObjectFieldStart(Reference.class.getSimpleName().toLowerCase());

            // properties
            write(Reference.AUTHORS_PROPERTY, reference.getAuthors());
            write(Reference.COMMENT_PROPERTY, reference.getComment());
            write(Reference.EDITION_PROPERTY, reference.getEdition());
            write(Reference.ID_PK_COLUMN, reference.getId());
            write(Reference.ISBN_PROPERTY, reference.getIsbn());
            write(Reference.PLACE_OF_PUBLICATION_PROPERTY, reference.getPlaceOfPublication());
            write(Reference.PUBLISHER_PROPERTY, reference.getPublisher());
            write(Reference.SIGNATURE_PROPERTY, reference.getSignature());
            write(Reference.THUMBNAIL_URL_PROPERTY, reference.getThumbnailURL());
            write(Reference.TITLE_PROPERTY, reference.getTitle());
            write(Reference.VOLUME_PROPERTY, reference.getVolume());
            write(Reference.YEAR_OF_PUBLICATION_PROPERTY, reference.getYearOfPublication());

        } catch (IOException e) {
            LOG.error("could not write start of " + reference.getClass().getSimpleName() + " " + reference.getId(), e);
        }
    }

    @Override
    public void didVisit(Reference reference) {
        try {
            jsonGenerator.writeEndObject();
        } catch (IOException e) {
            LOG.error("could not write end of " + reference.getClass().getSimpleName() + " " + reference.getId(), e);
        }
    }

    @Override
    public void visit(File file) {
        try {
            jsonGenerator.writeObjectFieldStart(File.class.getSimpleName().toLowerCase());

            // properties
            write(File.DESCRIPTION_PROPERTY, file.getDescription());
            write(File.ID_PK_COLUMN, file.getId());

        } catch (IOException e) {
            LOG.error("could not write start of " + file.getClass().getSimpleName() + " " + file.getId(), e);
        }
    }

    @Override
    public void didVisit(File file) {
        try {
            jsonGenerator.writeEndObject();
        } catch (IOException e) {
            LOG.error("could not write end of " + file.getClass().getSimpleName() + " " + file.getId(), e);
        }
    }

    @Override
    public void visit(JournalArticle article) {
        try {
            jsonGenerator.writeObjectFieldStart(JournalArticle.class.getSimpleName().toLowerCase());

            // properties
            write(JournalArticle.ARTICLE_TITLE_PROPERTY, article.getArticleTitle());
            write(JournalArticle.AUTHORS_PROPERTY, article.getAuthors());
            write(JournalArticle.COMMENT_PROPERTY, article.getComment());
            write(JournalArticle.ID_PK_COLUMN, article.getId());
            write(JournalArticle.ISSN_PROPERTY, article.getIssn());
            write(JournalArticle.ISSUE_PROPERTY, article.getIssue());
            write(JournalArticle.JOURNAL_TITLE_PROPERTY, article.getJournalTitle());
            write(JournalArticle.PAGE_END_PROPERTY, article.getPageEnd());
            write(JournalArticle.PAGE_START_PROPERTY, article.getPageStart());
            write(JournalArticle.PLACE_OF_PUBLICATION_PROPERTY, article.getPlaceOfPublication());
            write(JournalArticle.PUBLISHER_PROPERTY, article.getPublisher());
            write(JournalArticle.REFERENCE_NUMBER_PROPERTY, article.getReferenceNumber());
            write(JournalArticle.SIGNATURE_PROPERTY, article.getSignature());
            write(JournalArticle.VOLUME_PROPERTY, article.getVolume());

        } catch (IOException e) {
            LOG.error("could not write start of " + article.getClass().getSimpleName() + " " + article.getId(), e);
        }
    }

    @Override
    public void didVisit(JournalArticle article) {
        try {
            jsonGenerator.writeEndObject();
        } catch (IOException e) {
            LOG.error("could not write end of " + article.getClass().getSimpleName() + " " + article.getId(), e);
        }
    }

    @Override
    public void visit(BookChapter chapter) {
        try {
            jsonGenerator.writeObjectFieldStart(BookChapter.class.getSimpleName().toLowerCase());

            // properties
            write(BookChapter.BOOK_AUTHORS_PROPERTY, chapter.getBookAuthors());
            write(BookChapter.BOOK_TITLE_PROPERTY, chapter.getBookTitle());
            write(BookChapter.CHAPTER_AUTHORS_PROPERTY, chapter.getChapterAuthors());
            write(BookChapter.CHAPTER_TITLE_PROPERTY, chapter.getChapterTitle());
            write(BookChapter.COMMENT_PROPERTY, chapter.getComment());
            write(BookChapter.EDITION_PROPERTY, chapter.getEdition());
            write(BookChapter.EDITOR_PROPERTY, chapter.getEditor());
            write(BookChapter.ID_PK_COLUMN, chapter.getId());
            write(BookChapter.ISBN_PROPERTY, chapter.getIsbn());
            write(BookChapter.PAGE_END_PROPERTY, chapter.getPageEnd());
            write(BookChapter.PAGE_START_PROPERTY, chapter.getPageStart());
            write(BookChapter.PLACE_OF_PUBLICATION_PROPERTY, chapter.getPlaceOfPublication());
            write(BookChapter.PUBLISHER_PROPERTY, chapter.getPublisher());
            write(BookChapter.REFERENCE_NUMBER_PROPERTY, chapter.getReferenceNumber());
            write(BookChapter.SIGNATURE_PROPERTY, chapter.getSignature());
            write(BookChapter.YEAR_OF_PUBLICATION_PROPERTY, chapter.getYearOfPublication());

        } catch (IOException e) {
            LOG.error("could not write start of " + chapter.getClass().getSimpleName() + " " + chapter.getId(), e);
        }
    }

    @Override
    public void didVisit(BookChapter chapter) {
        try {
            jsonGenerator.writeEndObject();
        } catch (IOException e) {
            LOG.error("could not write end of " + chapter.getClass().getSimpleName() + " " + chapter.getId(), e);
        }
    }

    @Override
    public void visit(Book book) {
        try {
            jsonGenerator.writeObjectFieldStart(Book.class.getSimpleName().toLowerCase());

            // properties
            write(Book.AUTHORS_PROPERTY, book.getAuthors());
            write(Book.COMMENT_PROPERTY, book.getComment());
            write(Book.EDITION_PROPERTY, book.getEdition());
            write(Book.ID_PK_COLUMN, book.getId());
            write(Book.ISBN_PROPERTY, book.getIsbn());
            write(Book.PLACE_OF_PUBLICATION_PROPERTY, book.getPlaceOfPublication());
            write(Book.PUBLISHER_PROPERTY, book.getPublisher());
            write(Book.RESOURCE_TYPE_PROPERTY, book.getResourceType());
            write(Book.SIGNATURE_PROPERTY, book.getSignature());
            write(Book.THUMBNAIL_URL_PROPERTY, book.getThumbnailURL());
            write(Book.TITLE_PROPERTY, book.getTitle());
            write(Book.VOLUME_PROPERTY, book.getVolume());
            write(Book.YEAR_OF_PUBLICATION_PROPERTY, book.getYearOfPublication());

        } catch (IOException e) {
            LOG.error("could not write start of " + book.getClass().getSimpleName() + " " + book.getId(), e);
        }
    }

    @Override
    public void didVisit(Book book) {
        try {
            jsonGenerator.writeEndObject();
        } catch (IOException e) {
            LOG.error("could not write end of " + book.getClass().getSimpleName() + " " + book.getId(), e);
        }
    }

    @Override
    public void visit(Resource resource) {
        try {
            jsonGenerator.writeObjectFieldStart(Resource.class.getSimpleName().toLowerCase());

            // properties
            write(Resource.COPYRIGHT_REVIEW_STATUS_PROPERTY, resource.getCopyrightReviewStatus());
            write(Resource.FILE_PATH_PROPERTY, resource.getFilePath());
            write(Resource.FULL_TEXT_URL_PROPERTY, resource.getFullTextURL());
            write(Resource.ID_PK_COLUMN, resource.getId());
            write(Resource.MIME_TYPE_PROPERTY, resource.getMimeType());

            jsonGenerator.writeEndObject();

        } catch (IOException e) {
            LOG.error("could not write start of " + resource.getClass().getSimpleName() + " " + resource.getId(), e);
        }
    }

    @Override
    public void startList(String fieldName) {
        try {
            jsonGenerator.writeArrayFieldStart(fieldName);
        } catch (IOException e) {
            LOG.error("could not start array");
        }
    }

    @Override
    public void endList() {
        try {
            jsonGenerator.writeEndArray();
        } catch (IOException e) {
            LOG.error("could not end array");
        }

    }

    private void write(String fieldName, String value) throws IOException {
        if (value != null) {
            jsonGenerator.writeStringField(fieldName, value);
        }
    }
    
    private void write(String fieldName, Integer value) throws IOException {
        if (value != null) {
            jsonGenerator.writeNumberField(fieldName, value);
        }
    }
    
    private void write(String fieldName, Boolean value) throws IOException {
        if (value != null) {
            jsonGenerator.writeBooleanField(fieldName, value);
        }
    }

    private void write(String fieldName, Date value) throws IOException {
        if (value != null) {
            jsonGenerator.writeNumberField(fieldName, value.getTime());
        }
    }

    private void write(String fieldName, Enum value) throws IOException {
        if (value != null) {
            jsonGenerator.writeStringField(fieldName, value.name());
        }
    }
}
