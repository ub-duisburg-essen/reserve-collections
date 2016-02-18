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

import org.apache.cayenne.BaseContext;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.query.NamedQuery;
import org.apache.cayenne.query.SelectQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unidue.rc.model.BookJob;
import unidue.rc.model.LibraryLocation;
import unidue.rc.model.ReserveCollection;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author Nils Verheyen
 * @since 29.11.13 09:50
 */
public class BookJobDAOImpl extends BaseDAOImpl implements BookJobDAO {

    private static final Logger LOG = LoggerFactory.getLogger(BookJobDAOImpl.class);

    @Override
    public List<BookJob> getJobs() {

        ObjectContext context = BaseContext.getThreadObjectContext();
        SelectQuery query = new SelectQuery(BookJob.class);
        List<BookJob> result = context.performQuery(query);

        return result != null
               ? result
               : Collections.EMPTY_LIST;
    }

    @Override
    public BookJob getJob(Integer bookJobID) {
        return Cayenne.objectForPK(BaseContext.getThreadObjectContext(), BookJob.class, bookJobID);
    }

    public void create(BookJob job) throws CommitException {
        job.setModified(new Date());
        super.create(job);
    }

    public void update(BookJob job) throws CommitException, IOException, SolrServerException {

        job.setModified(new Date());
        super.update(job);
    }
}
