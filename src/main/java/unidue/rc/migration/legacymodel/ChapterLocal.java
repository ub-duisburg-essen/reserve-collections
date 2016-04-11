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
package unidue.rc.migration.legacymodel;


import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;
import unidue.rc.migration.MigrationException;
import unidue.rc.model.Entry;
import unidue.rc.model.ReserveCollection;

@Root(name = "chapter", strict = false)
public class ChapterLocal implements MigrationVisitable, ResourceValue {

    @Element(name = "title", required = false)
    private String title;

    @Element(name = "book", required = false)
    private BookLocal book;

    @Path("location/pages")
    @Attribute(name = "from", required = false)
    private String pageFrom;

    @Path("location/pages")
    @Attribute(name = "to", required = false)
    private String pageTo;

    @Element(name = "comment", required = false)
    private String comment;

    @Path("text")
    @Element(name = "path", required = false)
    private String path;

    @Path("text")
    @Element(name = "attachmentNo", required = false)
    private String refNo;


    @Path("text")
    @Element(name = "url", required = false)
    private String url;

    @Element(name = "author", required = false)
    private String author;

    @Path("text")
    @Element(name = "itemStatus", required = false)
    private String reviewStatus;

   /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the book
     */
    public BookLocal getBook() {
        return book;
    }

    /**
     * @return the pageFrom
     */
    public String getPageFrom() {
        return pageFrom;
    }

    /**
     * @return the pageTo
     */
    public String getPageTo() {
        return pageTo;
    }

    /**
     * @return the comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return "ChapterLocal [Book=" + book + " title= " + title + " from= " + pageFrom + " to =" + pageTo + "]";
    }

    /**
     * @return the author
     */
    public String getAuthor() {
        return author;
    }

    public String getUrl() {
        return url;
    }

    public String getReviewStatus() {
        return reviewStatus;
    }

    @Override
    public void migrateEntryValue(MigrationVisitor visitor, ReserveCollection collection, Entry entry, EntryLocal entryLocal, String derivateID) throws MigrationException {
        visitor.migrate(this, collection, entry, entryLocal, derivateID);
    }
}
