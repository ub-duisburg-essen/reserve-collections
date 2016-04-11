package unidue.rc.io;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import unidue.rc.model.*;

import java.io.IOException;
import java.util.Date;

/**
 * Created by nils on 07.08.15.
 */
public class CollectionJsonReader extends StdDeserializer<ReserveCollection> {

    protected CollectionJsonReader() {
        super(ReserveCollection.class);
    }

    @Override
    public ReserveCollection deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        // Sanity check: verify that we got "Json Object":
        checkStartObject(jp);

        ReserveCollection collection = new ReserveCollection();
        while (jp.nextToken() != JsonToken.END_OBJECT && jp.getCurrentToken() != null) {

            final String field = jp.getCurrentName();

            // ALEPH_SYSTEM_ID_PROPERTY
            // ALEPH_USER_ID_PROPERTY
            // COMMENT_PROPERTY
            // CREATED_PROPERTY
            // DO_WARNING_PROPERTY
            // ID_PROPERTY
            // MODIFIED_PROPERTY
            // ORIGIN_ID_PROPERTY
            // PERMALINK_PROPERTY
            // PIWIK_ID_PROPERTY
            // READ_KEY_PROPERTY
            // STATUS_PROPERTY
            // TITLE_PROPERTY
            // VALID_TO_PROPERTY
            // WRITE_KEY_PROPERTY
            switch (field) {

                // properties

                // string
                case ReserveCollection.ALEPH_SYSTEM_ID_PROPERTY:
                case ReserveCollection.ALEPH_USER_ID_PROPERTY:
                case ReserveCollection.COMMENT_PROPERTY:
                case ReserveCollection.PERMALINK_PROPERTY:
                case ReserveCollection.READ_KEY_PROPERTY:
                case ReserveCollection.TITLE_PROPERTY:
                case ReserveCollection.WRITE_KEY_PROPERTY:
                    collection.writeProperty(field, jp.getValueAsString());
                    break;
                // date
                case ReserveCollection.CREATED_PROPERTY:
                case ReserveCollection.MODIFIED_PROPERTY:
                case ReserveCollection.VALID_TO_PROPERTY:
                case ReserveCollection.DISSOLVE_AT_PROPERTY:
                    collection.writeProperty(field, new Date(jp.getValueAsInt()));
                    break;
                // boolean
                case ReserveCollection.MEDIA_DOWNLOAD_ALLOWED_PROPERTY:
                    collection.setMediaDownloadAllowed(jp.getValueAsBoolean());
                    break;
                // int
                case ReserveCollection.ID_PROPERTY:
                case ReserveCollection.ORIGIN_ID_PROPERTY:
                    collection.setId(jp.getValueAsInt());
                    break;
                // other
                case ReserveCollection.STATUS_PROPERTY:
                    collection.setStatus(ReserveCollectionStatus.valueOf(jp.getValueAsString()));
                    break;

                // simple relations
                case ReserveCollection.NUMBER_PROPERTY:
                    ReserveCollectionNumber number = new ReserveCollectionNumber();
                    number.setNumber(jp.getValueAsInt());
                    collection.setNumber(number);
                    break;

                // relations
                case ReserveCollection.ENTRIES_PROPERTY:
                    deserializeEntries(jp, collection);
                    break;
                case ReserveCollection.PARTICIPATIONS_PROPERTY:
                    deserializeParticipations(jp, collection);
                    break;
                case ReserveCollection.LIBRARY_LOCATION_PROPERTY:
                    deserializeLocation(jp, collection);
                    break;
            }
        }
        jp.close();
        return collection;
    }

    private void deserializeEntries(JsonParser jp, ReserveCollection collection) throws IOException {
        // Sanity check: verify that we got "Json Object":
        if (jp.getCurrentToken() != JsonToken.START_ARRAY
            && jp.nextToken() != JsonToken.START_OBJECT) {
            throw new IOException("Expected data to start with an Array");
        }

        Entry entry = new Entry();
        while (jp.nextToken() != JsonToken.END_ARRAY && jp.getCurrentToken() != null) {

            if (jp.getCurrentToken() == JsonToken.START_OBJECT)
                entry = new Entry();
            if (jp.getCurrentToken() == JsonToken.END_OBJECT)
                collection.addToEntries(entry);

            final String field = jp.getCurrentName();
            switch (field) {

                // properties
                case Entry.CREATED_PROPERTY:
                case Entry.MODIFIED_PROPERTY:
                    entry.writeProperty(field, new Date(jp.getValueAsInt()));
                    break;
                case Entry.ID_PK_COLUMN:
                case Entry.POSITION_PROPERTY:
                    entry.writeProperty(field, jp.getValueAsInt());
                    break;

                // relations
                case Entry.BOOK_PROPERTY:
                    deserializeBook(jp, entry);
                    break;
                case Entry.BOOK_CHAPTER_PROPERTY:
                    deserializeBookChapter(jp, entry);
                    break;
                case Entry.FILE_PROPERTY:
                    deserializeFile(jp, entry);
                    break;
                case Entry.HEADLINE_PROPERTY:
                    deserializeHeadline(jp, entry);
                    break;
                case Entry.HTML_PROPERTY:
                    deserializeHtml(jp, entry);
                    break;
                case Entry.JOURNAL_ARTICLE_PROPERTY:
                    deserializeJournalArticle(jp, entry);
                    break;
                case Entry.REFERENCE_PROPERTY:
                    deserializeReference(jp, entry);
                    break;
                case Entry.WEB_LINK_PROPERTY:
                    deserializeWebLink(jp, entry);
                    break;
            }
        }
    }

    private void deserializeBook(JsonParser jp, Entry entry) throws IOException {
        // Sanity check: verify that we got "Json Object":
        checkStartObject(jp);

        Book book = new Book();
        while (jp.nextToken() != JsonToken.END_OBJECT && jp.getCurrentToken() != null) {
            if (jp.getCurrentToken() == JsonToken.END_OBJECT)
                entry.setBook(book);

            final String field = jp.getCurrentName();
            switch (field) {
                case Book.AUTHORS_PROPERTY:
                case Book.COMMENT_PROPERTY:
                case Book.EDITION_PROPERTY:
                case Book.ISBN_PROPERTY:
                case Book.PLACE_OF_PUBLICATION_PROPERTY:
                case Book.PUBLISHER_PROPERTY:
                case Book.RESOURCE_TYPE_PROPERTY:
                case Book.SIGNATURE_PROPERTY:
                case Book.THUMBNAIL_URL_PROPERTY:
                case Book.TITLE_PROPERTY:
                case Book.VOLUME_PROPERTY:
                    book.writeProperty(field, jp.getValueAsString());
                    break;
                case Book.YEAR_OF_PUBLICATION_PROPERTY:
                    book.writeProperty(field, jp.getValueAsInt());
                    break;

                case Book.RESOURCE_PROPERTY:
                    deserializeResource(jp, book);
            }
        }
    }

    private void deserializeResource(JsonParser jp, ResourceContainer resourceContainer) throws IOException {
        // Sanity check: verify that we got "Json Object":
        checkStartObject(jp);

        Resource r = new Resource();
        while (jp.nextToken() != JsonToken.END_OBJECT && jp.getCurrentToken() != null) {
            if (jp.getCurrentToken() == JsonToken.END_OBJECT)
                resourceContainer.setResource(r);

            final String field = jp.getCurrentName();
            switch (field) {
                case Resource.COPYRIGHT_REVIEW_STATUS_PROPERTY:
                    r.setCopyrightReviewStatus(CopyrightReviewStatus.valueOf(jp.getValueAsString()));
                    break;
                case Resource.FILE_PATH_PROPERTY:
                case Resource.FULL_TEXT_URL_PROPERTY:
                case Resource.MIME_TYPE_PROPERTY:
                    r.writeProperty(field, jp.getValueAsString());
                    break;
            }
        }
    }

    private void deserializeBookChapter(JsonParser jp, Entry entry) {

    }

    private void deserializeFile(JsonParser jp, Entry entry) {

    }

    private void deserializeHeadline(JsonParser jp, Entry entry) {

    }

    private void deserializeHtml(JsonParser jp, Entry entry) {

    }

    private void deserializeJournalArticle(JsonParser jp, Entry eentry) {

    }

    private void deserializeReference(JsonParser jp, Entry entry) {

    }

    private void deserializeWebLink(JsonParser jp, Entry entry) {

    }

    private void deserializeParticipations(JsonParser jp, ReserveCollection collection) {

    }

    private void deserializeLocation(JsonParser jp, ReserveCollection collection) {

    }

    private void checkStartObject(JsonParser jp) throws IOException {
        // Sanity check: verify that we got "Json Object":
        if (jp.getCurrentToken() != JsonToken.START_OBJECT) {
            throw new IOException("Expected data to start with an object");
        }
    }
}
