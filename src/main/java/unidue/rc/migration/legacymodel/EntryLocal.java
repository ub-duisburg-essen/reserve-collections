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
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import unidue.rc.migration.MigrationException;
import unidue.rc.model.Entry;
import unidue.rc.model.ReserveCollection;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Root(name = "entry", strict = false)
public class EntryLocal implements MigrationVisitable {

    @Attribute(name = "ID")
    private String entryID;

    @Attribute(required = false)
    private boolean deleted;

    @ElementList(inline = true)
    private List<DateTimeLocal> dates;

    @Element(name = "html", required = false)
    private String html;

    @Element(name = "book", required = false)
    private BookLocal book;

    @Element(name = "article", required = false)
    private ArticleLocal article;

    @Element(name = "chapter", required = false)
    private ChapterLocal chapter;

    @Element(name = "file", required = false)
    private FileLocal file;

    @Element(name = "headline", required = false)
    private String headline;

    @Element(name = "webLink", required = false)
    private WeblinkLocal weblink;

    @Element(name = "freeText", required = false)
    private FreeTextLocal freetext;

    @Element(name = "milessLink", required = false)
    private DocumentLinkLocal documentLink;

    public String getEntryID() {
        return entryID;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public String getHtml() {
        return html;
    }

    public BookLocal getBook() {
        return book;
    }

    public ArticleLocal getArticle() {
        return article;
    }

    public ChapterLocal getChapter() {
        return chapter;
    }

    public FileLocal getFile() {
        return file;
    }

    public String getHeadline() {
        return headline;
    }

    public WeblinkLocal getWeblink() {
        return weblink;
    }

    public FreeTextLocal getFreetext() {
        return freetext;
    }

    public DocumentLinkLocal getDocumentLink() {
        return documentLink;
    }

    /**
     * @return the dates
     */
    public List<DateTimeLocal> getDates() {
        return dates;
    }

    @Override
    public String toString() {
        return "EntryLocal [id=" + entryID + "]";
    }

    public Date getCreated() {
        for (DateTimeLocal dtl : dates) {
            if ("created".equals(dtl.getType()))
                return toDate(dtl);
        }
        return null;
    }

    public Date getModified() {
        for (DateTimeLocal dtl : dates) {
            if ("modified".equals(dtl.getType()))
                return toDate(dtl);
        }
        return null;
    }

    private static Date toDate(DateTimeLocal dtl) {
        DateFormat df = new SimpleDateFormat(dtl.getFormat());
        try {
            return df.parse(dtl.getValue());
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void migrateEntryValue(MigrationVisitor visitor, ReserveCollection collection, Entry entry, EntryLocal entryLocal, String derivateID) throws MigrationException {

        ArticleLocal article = getArticle();
        if (article != null)
            article.migrateEntryValue(visitor, collection, entry, entryLocal, derivateID);

        BookLocal book = getBook();
        if (book != null)
            book.migrateEntryValue(visitor, collection, entry, entryLocal, derivateID);

        ChapterLocal chapter = getChapter();
        if (chapter != null)
            chapter.migrateEntryValue(visitor, collection, entry, entryLocal, derivateID);

        FileLocal file = getFile();
        if (file != null)
            file.migrateEntryValue(visitor, collection, entry, entryLocal, derivateID);

        String headline = getHeadline();
        if (headline != null)
            visitor.migrateHeadline(headline, collection, entry, entryLocal, derivateID);

        String html = getHtml();
        if (html != null)
            visitor.migrateHtml(html, collection, entry, entryLocal, derivateID);

        FreeTextLocal text = getFreetext();
        if (text != null)
            visitor.migrateText(text, collection, entry, entryLocal, derivateID);

        WeblinkLocal weblink = getWeblink();
        if (weblink != null)
            visitor.migrate(weblink, collection, entry, entryLocal, derivateID);

        DocumentLinkLocal documentLink = getDocumentLink();
        if (documentLink != null)
            visitor.migrate(documentLink, collection, entry, entryLocal, derivateID);

    }
}
