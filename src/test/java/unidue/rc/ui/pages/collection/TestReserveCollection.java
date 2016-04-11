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
package unidue.rc.ui.pages.collection;


import com.thoughtworks.selenium.Wait;
import org.apache.cayenne.validation.ValidationException;
import org.apache.tapestry5.test.SeleniumTestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import unidue.rc.DbTestUtils;
import unidue.rc.dao.*;
import unidue.rc.model.Book;
import unidue.rc.model.BookChapter;
import unidue.rc.model.CopyrightReviewStatus;
import unidue.rc.model.Entry;
import unidue.rc.model.File;
import unidue.rc.model.Headline;
import unidue.rc.model.Html;
import unidue.rc.model.JournalArticle;
import unidue.rc.model.ReserveCollection;
import unidue.rc.model.Resource;
import unidue.rc.model.WebLink;
import unidue.rc.system.SystemConfigurationService;
import unidue.rc.system.SystemConfigurationServiceImpl;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class TestReserveCollection extends SeleniumTestCase {

    private static final Logger LOG = LoggerFactory
            .getLogger(TestReserveCollection.class);

    private DbTestUtils dbTestUtils;
    private ReserveCollection rc;
    private EntryDAO entryDAO;
    private Headline headline1;
    private Headline headline2;
    private BookChapter chapter;
    private Book book;
    private Html html;
    private JournalArticle article;
    private WebLink weblink;
    private ResourceDAO resourceDAO;
    private SystemConfigurationService configService;
    private java.io.File downloadFile;
    private File file;

    @BeforeClass
    public void setup() throws Exception {
        LOG.info("running " + this.getClass().getName() + " tests");

        try {
            dbTestUtils = new DbTestUtils();
            dbTestUtils.setupdb();
            rc = dbTestUtils.createMockReserveCollection("TestA");
            headline1 = dbTestUtils.createMockHeadline("Headline", rc);
            headline2 = dbTestUtils.createMockHeadline("Headline2", rc);
            chapter = dbTestUtils.createMockBookChapter(rc);
            book = dbTestUtils.createMockBook(rc);
            html = dbTestUtils.createMockHtml(rc);
            article = dbTestUtils.createMockJournal(rc);
            weblink = dbTestUtils.createMockWeblink(rc);
            entryDAO = new EntryDAOImpl();
            configService = new SystemConfigurationServiceImpl(dbTestUtils.getDatabaseConfiguration(), new SettingDAOImpl());
            resourceDAO = new ResourceDAOImpl(configService);
            file = createFileEntry(rc);
            downloadFile = createFile();
        } catch (DatabaseException | ValidationException e) {
            LOG.error("could not setup " + this.getClass().getSimpleName() + " tests: " + e.getMessage());
            throw e;
        }
    }

    @Test
    public void testIsWeblinkPresent() {
        open("/collection/view/" + rc.getId());
        LOG.debug("collection: " + rc);
        for (Entry entry : rc.getEntries()) {
            LOG.debug("current entry: " + entry);
            LOG.debug("current value: " + entry.getValue());
        }
        new Wait() {

            @Override
            public boolean until() {
                return isElementPresent("id=" + weblink.getEntry().getId());
            }
        }.wait("error should be visible", 5000);


    }

    @Test
    public void testIsBookPresent() {
        open("/collection/view/" + rc.getId());
        new Wait() {

            @Override
            public boolean until() {
                return isElementPresent("id=" + book.getEntry().getId());
            }
        }.wait("error should be visible", 5000);
    }

    @Test
    public void testIsHTMLPresent() {
        open("/collection/view/" + rc.getId());
        new Wait() {

            @Override
            public boolean until() {
                return isElementPresent("id=" + html.getEntry().getId());
            }
        }.wait("error should be visible", 5000);
    }

    @Test
    public void testIsJournalPresent() {
        open("/collection/view/" + rc.getId());
        new Wait() {

            @Override
            public boolean until() {
                return isElementPresent("id=" + article.getEntry().getId());
            }
        }.wait("error should be visible", 5000);
    }

    @Test
    public void testIsBookChapterPresent() {
        open("/collection/view/" + rc.getId());
        new Wait() {

            @Override
            public boolean until() {
                return isElementPresent("id=" + chapter.getEntry().getId());
            }
        }.wait("error should be visible", 5000);
    }

    @Test
    public void testIsFilePresent() {
        open("/collection/view/" + rc.getId());
        new Wait() {

            @Override
            public boolean until() {
                return isElementPresent("id=" + file.getEntry().getId());
            }
        }.wait("error should be visible", 5000);
    }

    //TODO CHECK http://saucelabs.com/resources/selenium/css-selectors
    @Test
    public void testIndexHeadline1Success() {
        open("/collection/view/" + rc.getId());
        new Wait() {

            @Override
            public boolean until() {
                return isElementPresent("css=div[class=\"list-highlight toc t-zone\"] ul li:nth-of-type(1) " +
                        "a[href=\"#" + headline1.getEntry().getId() + "\"]");
            }
        }.wait("error should be visible", 5000);

    }

    @Test
    public void testIndexHeadline2Success() {
        open("/collection/view/" + rc.getId());
        new Wait() {

            @Override
            public boolean until() {
                return isElementPresent("css=div[class=\"list-highlight toc t-zone\"] ul li:nth-of-type(2) " +
                        "a[href=\"#" + headline2.getEntry().getId() + "\"]");
            }
        }.wait("error should be visible", 5000);
    }

    @Test(dependsOnMethods = {"testIsFilePresent"})
    public void testDownloadFile() {
        String downloadedFilePath = "";

        String downloadBaseURL = getBaseURL()
                + "collection/view:download/"
                + file.getResource().getId() + "?t:ac=" + rc.getId();
        try {
            downloadedFilePath = download(downloadBaseURL,
                    downloadFile);

        } catch (Exception e) {
            LOG.error("could not download file from " + downloadBaseURL + " to " + downloadFile.getAbsolutePath(), e);
        }
        assertTrue(new java.io.File(downloadedFilePath).exists());
    }

    @Test(dependsOnMethods = {"testDownloadFile"})
    public void testDeleteEntry() {
        open("/collection/view:deleteentry/" + file.getEntry().getId() + "?t:ac=" + rc.getId());
        new Wait() {

            @Override
            public boolean until() {
                return !isElementPresent("id=" + file.getEntry().getId());
            }
        }.wait("entry should not be visible", 5000);
    }

    protected void typeInField(final String fieldId, String value) {
        type("//input[@type='text'][@id='" + fieldId + "']", value);
    }

    private File createFileEntry(ReserveCollection rc) throws CommitException {
        unidue.rc.model.File fileEntry = new unidue.rc.model.File();
        // create file entry
        entryDAO.createEntry(fileEntry, rc);
        return fileEntry;
    }

    private java.io.File createFile() throws CommitException, IOException {

        java.io.File testFile = java.io.File.createTempFile("file", "txt");

        // create resource
        Resource resource = resourceDAO.createResourceFromStream(testFile.getName(), new FileInputStream(testFile), file);
        file.setResource(resource);


        // update entry with mime and path
        resource.setMimeType(resourceDAO.detectMimeType(testFile));
        resourceDAO.update(file);

        return testFile;
    }

    public String download(String address, java.io.File file) throws IOException {
        InputStream in = null;
        URL url = new URL(address);
        URLConnection conn = url.openConnection();
        HttpURLConnection httpConn = (HttpURLConnection) conn;
        httpConn.setAllowUserInteraction(false);
        httpConn.setInstanceFollowRedirects(true);
        httpConn.setRequestMethod("GET");
        httpConn.setRequestProperty("Cookie", "testcookie");
        httpConn.connect();
        int response = httpConn.getResponseCode();
        if (response == HttpURLConnection.HTTP_OK) {
            in = httpConn.getInputStream();
        }
        String filepath = "/tmp/downloaded_" + file.getName();
        FileOutputStream myFileOutputStream = new FileOutputStream(filepath);
        byte buf[] = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0)
            myFileOutputStream.write(buf, 0, len);
        myFileOutputStream.close();
        return filepath;
    }

    @AfterClass
    public void shutdown() {
        dbTestUtils.shutdown();
        LOG.info("Test of " + this.getClass().getName() + " done...");
    }

}
