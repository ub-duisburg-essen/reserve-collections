package unidue.rc.dao;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unidue.rc.model.Book;
import unidue.rc.plugins.alephsync.AlephBook;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by marcus.koesters on 11.11.15.
 */
public class TestAlephDAOImpl implements AlephDAO {

    private static final Logger LOG = LoggerFactory.getLogger(TestAlephDAOImpl.class);

    @Override
    public List<AlephBook> listSignatures(String systemID) throws SQLException {
        LOG.debug("List Signatures called");
        return null;
    }

    @Override
    public void setBookData(Book book, String signature) throws SQLException {
        LOG.debug("Set BookData called");
        book.setTitle("TESTTITEL");
        book.setAuthors("TestAUTOR");
        book.setEdition("Edition");
        book.setPlaceOfPublication("TESTPLACE");
        book.setIsbn("234234234234");
        book.setPublisher("test");
        book.setVolume("123af");

    }
}
