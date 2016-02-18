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

import unidue.rc.model.Book;
import unidue.rc.plugins.alephsync.AlephBook;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by nils on 06.10.15.
 */
public interface AlephDAO {

    String SERVICE_NAME = "AlephDAO";

    /**
     * Retrieves the signatures of all loans by the user with the given systemID
     *
     * @param systemID alephs system id of a book
     * @return {@link List} with all available aleph books
     * @throws SQLException if an error occured during aleph db access
     **/
    List<AlephBook> listSignatures(String systemID) throws SQLException;

    /**
     * Retrieves all information of aleph by target signature and sets found information into given book.
     *
     * @param book      object whose data should be set
     * @param signature signature of the book
     * @throws SQLException if an error occured during aleph db access
     */
    void setBookData(Book book, String signature) throws SQLException;
}
