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
import org.apache.cayenne.query.RelationshipQuery;
import org.apache.cayenne.query.SelectQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unidue.rc.model.BookChapter;
import unidue.rc.model.JournalArticle;
import unidue.rc.model.LibraryItem;
import unidue.rc.model.LibraryLocation;
import unidue.rc.model.ReserveCollection;
import unidue.rc.model.ReserveCollectionsDatamap;
import unidue.rc.model.ScanJob;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author Nils Verheyen
 * @since 11.12.13 16:44
 */
public class ScanJobDAOImpl extends BaseDAOImpl implements ScanJobDAO {

    private static final Logger LOG = LoggerFactory.getLogger(ScanJobDAOImpl.class);

    @Override
    public void create(Object object) throws CommitException {

        if (object instanceof ScanJob) {
            ScanJob job = (ScanJob) object;
            job.setModified(new Date());
        }
        super.create(object);
    }


    @Override
    public void update(Object object) throws CommitException {

        if (object instanceof ScanJob) {
            ScanJob job = (ScanJob) object;
            job.setModified(new Date());
        }
        super.update(object);
    }

    @Override
    public List<ScanJob> getJobs() {

        ObjectContext context = BaseContext.getThreadObjectContext();

        SelectQuery query = new SelectQuery(ScanJob.class);

        List<ScanJob> result = context.performQuery(query);
        return result != null ? result : Collections.EMPTY_LIST;
    }

    @Override
    public LibraryItem getLibraryItem(ScanJob scanJob) {

        RelationshipQuery query = new RelationshipQuery(scanJob.getObjectId(), ScanJob.JOURNAL_ARTICLE_PROPERTY);
        ObjectContext context = BaseContext.getThreadObjectContext();
        JournalArticle article = (JournalArticle) Cayenne.objectForQuery(context, query);

        if (article == null) {

            query = new RelationshipQuery(scanJob.getObjectId(), ScanJob.BOOK_CHAPTER_PROPERTY);
            return (BookChapter) Cayenne.objectForQuery(context, query);
        }

        return article;
    }
}
