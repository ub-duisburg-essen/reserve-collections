package unidue.rc.search;

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

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import unidue.rc.model.IntPrimaryKey;
import unidue.rc.model.solr.SolrBookJobView;
import unidue.rc.model.solr.SolrCollectionView;
import unidue.rc.model.solr.SolrCopyrightView;
import unidue.rc.model.solr.SolrScanJobView;

import java.io.IOException;

/**
 * A <code>SolrService</code> can be used to perform CRUD-operations on the solr server and its cores used by this
 * application.
 *
 * @author Nils Verheyen
 * @since 13.11.14 14:24
 */
public interface SolrService {


    /**
     * This application requires a solr multicore architecture. Each used core is defined at this point.
     */
    enum Core {
        ReserveCollection("collections", SolrCollectionView.class),
        Copyright("copyright", SolrCopyrightView.class),
        ScanJob("scanjobs", SolrScanJobView.class),
        BookJob("bookjobs", SolrBookJobView.class);

        /**
         * Contains the name of the core which is used to build solr core server urls.
         */
        final public String value;

        /**
         * Contains the view class that is returned via queries from the core.
         */
        Class viewClass;

        Core(String name, Class viewClass) {
            this.value = name;
            this.viewClass = viewClass;
        }
    }

    /**
     * Tries to add target bean to target {@link Core}.
     *
     * @param bean bean to add
     * @param core core of the bean
     * @see unidue.rc.model.solr
     */
    void addBean(Object bean, Core core);

    /**
     * Runs the {@code full-import} command on target core. Be careful, because a full import may take some time
     * according to the used core.
     *
     * @param core core to use
     * @throws SolrServerException thrown on any solr error
     * @throws IOException         on any io error
     */
    void fullImport(Core core) throws SolrServerException, IOException;

    /**
     * Deletes the document with target id of target {@link SolrService.Core}.
     *
     * @param id   id of the object
     * @param core core of the object
     */
    void deleteByID(IntPrimaryKey id, Core core);

    /**
     * Creates a new {@link SolrQueryBuilder} with which a {@link
     * org.apache.solr.client.solrj.SolrQuery} can be build.
     *
     * @return new solr query builder instance
     */
    SolrQueryBuilder createQueryBuilder();

    /**
     * Runs target query against the {@link Core} which belongs to target view class.
     *
     * @param viewClass class used inside a core
     * @param query     query to use
     * @param <T>       concrete class
     * @return a list of items that where found by target query or an empty list.
     * @throws SolrServerException on any solr error
     */
    <T> SolrResponse<T> query(Class<T> viewClass, SolrQuery query) throws SolrServerException;

    /**
     * Returns the object with target class, that is identified by target id if one exists, <code>null</code>
     * otherwise.
     *
     * @param viewClass class used inside a core
     * @param <T>       concrete class
     * @param id        id of the object
     * @return the object or <code>null</code> if none was found
     * @throws SolrServerException on any solr error
     */
    <T> T getById(Class<T> viewClass, String id) throws SolrServerException;
}
