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
import unidue.rc.model.JournalArticle;
import unidue.rc.model.ReserveCollection;

/**
 * Created with IntelliJ IDEA. User: mkoesters Date: 20.02.14 Time: 11:08 To change this template use File | Settings |
 * File Templates.
 */
public class TestJournalArticleEntry extends Assert {
    private static final Logger LOG = LoggerFactory.getLogger(TestJournalArticleEntry.class);


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
    public void testCreateInvalidTitle() {
        JournalArticle article = new JournalArticle();
        article.setJournalTitle(null);
        article.setArticleTitle("TestArticleTitle");
        article.setPageStart("100");
        article.setPageEnd("110");
        article.setPlaceOfPublication("TestPlace");
        try {
            entryDAO.createEntry(article, rc);
        } catch (CommitException e) {
            assertTrue(e instanceof CommitException);
        }
    }

    @Test
    public void testCreateInvalidArticleTitle() {
        JournalArticle article = new JournalArticle();
        article.setJournalTitle("TestTitle");
        article.setArticleTitle(null);
        article.setPageStart("100");
        article.setPageEnd("110");
        article.setPlaceOfPublication("TestPlace");
        try {
            entryDAO.createEntry(article, rc);
        } catch (CommitException e) {
            assertTrue(e instanceof CommitException);
        }
    }

    @Test
    public void testCreateInvalidPageStart() {
        JournalArticle article = new JournalArticle();
        article.setJournalTitle("TestTitle");
        article.setArticleTitle("ArticleTitle");
        article.setPageStart(null);
        article.setPageEnd("110");
        article.setPlaceOfPublication("TestPlace");
        try {
            entryDAO.createEntry(article, rc);
        } catch (CommitException e) {
            assertTrue(e instanceof CommitException);
        }
    }

    @Test
    public void testCreateInvalidPageEnd() {
        JournalArticle article = new JournalArticle();
        article.setJournalTitle("TestTitle");
        article.setArticleTitle("ArticleTitle");
        article.setPageStart("100");
        article.setPageEnd(null);
        article.setPlaceOfPublication("TestPlace");
        try {
            entryDAO.createEntry(article, rc);
        } catch (CommitException e) {
            assertTrue(e instanceof CommitException);
        }
    }


    @Test
    public void testValidJournal() {
        JournalArticle article = null;
        try {
            article = dbTestUtils.createMockJournal(rc);
        } catch (CommitException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        article = entryDAO.get(JournalArticle.class, article.getId());
        assertTrue(article != null);
    }

}
