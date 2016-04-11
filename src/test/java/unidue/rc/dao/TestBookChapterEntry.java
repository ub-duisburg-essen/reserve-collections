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
package unidue.rc.dao;


import org.apache.cayenne.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import unidue.rc.DbTestUtils;
import unidue.rc.model.BookChapter;
import unidue.rc.model.JournalArticle;
import unidue.rc.model.ReserveCollection;

/**
 * Created with IntelliJ IDEA. User: mkoesters Date: 20.02.14 Time: 11:08 To change this template use File | Settings |
 * File Templates.
 */
public class TestBookChapterEntry extends Assert {
    private static final Logger LOG = LoggerFactory.getLogger(TestBookChapterEntry.class);


    private EntryDAO entryDAO;

    private DbTestUtils dbTestUtils;
    private ReserveCollection rc;

    @BeforeClass
    public void setup() throws Exception {

        try {

            dbTestUtils = new DbTestUtils();
            dbTestUtils.setupdb();
            entryDAO = new EntryDAOImpl();
            rc = dbTestUtils.createMockReserveCollection("TestA");

        } catch (DatabaseException | ValidationException e) {
            LOG.error("could not setup " + this.getClass().getSimpleName() + " tests: " + e.getMessage());
            throw e;
        }
    }

    @AfterClass
    public void shutdown() {

        dbTestUtils.shutdown();
    }


    @Test
    public void testCreateInvalidBookTitle() {
        BookChapter chapter = new BookChapter();
        chapter.setBookTitle(null);
        chapter.setChapterTitle("ChapterTitle");
        chapter.setPageStart("100");
        chapter.setPageEnd("110");
        try {
            entryDAO.createEntry(chapter, rc);
        } catch (CommitException e) {
            assertTrue(e instanceof CommitException);
        }
    }

    @Test
    public void testCreateInvalidChapterTitle() {
        BookChapter chapter = new BookChapter();
        chapter.setBookTitle("BookTitle");
        chapter.setChapterTitle(null);
        chapter.setPageStart("100");
        chapter.setPageEnd("110");
        try {
            entryDAO.createEntry(chapter, rc);
        } catch (CommitException e) {
            assertTrue(e instanceof CommitException);
        }
    }

    @Test
    public void testCreateInvalidPageStart() {
        BookChapter chapter = new BookChapter();
        chapter.setBookTitle("BookTitle");
        chapter.setChapterTitle("ChapterTitle");
        chapter.setPageStart(null);
        chapter.setPageEnd("110");
        try {
            entryDAO.createEntry(chapter, rc);
        } catch (CommitException e) {
            assertTrue(e instanceof CommitException);
        }
    }

    @Test
    public void testCreateInvalidPageEnd() {
        BookChapter chapter = new BookChapter();
        chapter.setBookTitle("BookTitle");
        chapter.setChapterTitle("ChapterTitle");
        chapter.setPageStart("100");
        chapter.setPageEnd(null);
        try {
            entryDAO.createEntry(chapter, rc);
        } catch (CommitException e) {
            assertTrue(e instanceof CommitException);
        }
    }

    @Test
    public void testCreateInvalidJournalTitle() {
        JournalArticle article = new JournalArticle();
        try {
            entryDAO.createEntry(article, rc);
        } catch (CommitException e) {
            assertTrue(e instanceof CommitException);
        }
    }

    @Test
    public void testValidBookChapter() {
        BookChapter chapter = null;
        try {
            chapter = dbTestUtils.createMockBookChapter(rc);

        } catch (CommitException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        chapter = entryDAO.get(BookChapter.class, chapter.getId());
        assertTrue(chapter != null);
    }


}
