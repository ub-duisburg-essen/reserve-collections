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


import org.apache.cayenne.BaseContext;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.query.NamedQuery;
import unidue.rc.model.Book;
import unidue.rc.model.ReserveCollection;

import java.util.Collections;
import java.util.List;

/**
 * @author Nils Verheyen
 * @since 29.11.13 10:25
 */
public class BookDAOImpl extends EntryDAOImpl implements BookDAO {

    @Override
    public List<Book> getBooksByCollection(ReserveCollection collection) {
        NamedQuery query = new NamedQuery("select_books_by_collection", Collections.singletonMap("collectionID",
                collection.getId()));

        ObjectContext context = BaseContext.getThreadObjectContext();
        List<Book> books = context.performQuery(query);
        return books != null ? books : Collections.EMPTY_LIST;
    }
}
