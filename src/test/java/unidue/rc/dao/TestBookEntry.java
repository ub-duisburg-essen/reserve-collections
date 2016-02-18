package unidue.rc.dao;

/*
 * #%L
 * Semesterapparate
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 Universitaet Duisburg Essen
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

import org.apache.cayenne.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import unidue.rc.DbTestUtils;
import unidue.rc.model.Book;
import unidue.rc.model.Reference;
import unidue.rc.model.ReserveCollection;

/**
 * Created with IntelliJ IDEA. User: mkoesters Date: 20.02.14 Time: 11:08 To change this template use File | Settings |
 * File Templates.
 */
public class TestBookEntry extends Assert {
    private static final Logger LOG = LoggerFactory.getLogger(TestBookEntry.class);


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
        Book book = new Book();
        book.setTitle(null);

        try {
            entryDAO.createEntry(book, rc);
        } catch (CommitException e) {
            assertTrue(e instanceof CommitException);
        }
    }


    @Test
    public void testValidBook() {
        Book book = null;

        try {
            book = dbTestUtils.createMockBook(rc);
        } catch (CommitException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        book = entryDAO.get(Book.class, book.getId());
        assertTrue(book != null);
    }

    @Test
    public void testValidReference() {
        Reference reference = new Reference();
        reference.setTitle("BookTitle");
        try {
            entryDAO.createEntry(reference, rc);
        } catch (CommitException e) {
            e.printStackTrace();
        }
        reference = entryDAO.get(Reference.class, reference.getId());
        assertTrue(reference != null);
    }

}
