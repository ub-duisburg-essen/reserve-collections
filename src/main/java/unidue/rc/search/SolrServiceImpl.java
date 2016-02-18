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

import org.apache.cayenne.di.Inject;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.beans.DocumentObjectBinder;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unidue.rc.model.IntPrimaryKey;
import unidue.rc.system.SystemConfigurationService;

import java.io.IOException;
import java.util.concurrent.*;

/**
 * @author Nils Verheyen
 * @since 13.11.14 14:24
 */
public class SolrServiceImpl implements SolrService {

    private static final Logger LOG = LoggerFactory.getLogger(SolrServiceImpl.class);

    private static final int MAX_TIMEOUT_SECONDS = 5;

    @Inject
    private SystemConfigurationService config;

    private ExecutorService queue = Executors.newSingleThreadExecutor();

    @Override
    public void addBean(Object bean, Core core) {

        queue.submit(() -> {

            try {
                SolrClient client = getClient(core);
                client.addBean(bean);
                client.commit();
                LOG.info("added bean " + bean);
            } catch (IOException e) {
                LOG.error("i/o error on solr", e);
            } catch (SolrServerException e) {
                LOG.error("solr server error", e);
            }
        });
    }

    @Override
    public void fullImport(Core core) {

        queue.submit(() -> {

            SolrClient client = getClient(core);
            SolrParams params = buildImportParams("full-import");

            // data import handlers calls are async, so no job must be executed here
            QueryResponse response = null;
            try {
                response = client.query(params);
                LOG.info("full import " + core.name() + " finished");
            } catch (SolrServerException e) {
                LOG.error("solr server error", e);
            } catch (IOException e) {
                LOG.error("i/o error on solr", e);
            }
            LOG.info("full import response: " + response);
        });

    }

    private SolrParams buildImportParams(String command) {

        ModifiableSolrParams params = new ModifiableSolrParams();
        params.set("qt", "/dataimport");
        params.set("command", command);
        params.set("clean", "true");
        params.set("commit", "true");
        return params;
    }

    @Override
    public void deleteByID(IntPrimaryKey id, Core core) {

        queue.submit(() -> {

            try {
                SolrClient client = getClient(core);
                Integer realID = id.getId();
                client.deleteById(realID.toString());
                client.commit();
                LOG.info("deleted bean " + realID + " from core " + core.name());
            } catch (IOException e) {
                LOG.error("i/o error on solr", e);
            } catch (SolrServerException e) {
                LOG.error("solr server error", e);
            }
        });
    }

    @Override
    public SolrQueryBuilder createQueryBuilder() {
        return new SolrQueryBuilder();
    }

    public <T> SolrResponse<T> query(Class<T> viewClass, SolrQuery query) throws SolrServerException {
        SolrClient client = getClient(getCore(viewClass));
        if (client == null)
            throw new SolrServerException("no client for class " + viewClass + " available");

        try {
            QueryResponse response = client.query(query);
            SolrResponse<T> solrResponse = new SolrResponse<>();
            solrResponse.setItems(response.getBeans(viewClass));
            solrResponse.setOffset(response.getResults().getStart());
            solrResponse.setCount(response.getResults().getNumFound());
             return solrResponse;
        } catch (IOException e) {
            LOG.error("could not execute query ", e);
            throw new SolrServerException(e);
        }
    }

    @Override
    public <T> T getById(Class<T> viewClass, String id) throws SolrServerException {
        SolrClient client = getClient(getCore(viewClass));
        if (client == null)
            throw new SolrServerException("no client for class " + viewClass + " available");

        Future<T> future = queue.submit(() -> {

            try {
                SolrDocument document = client.getById(id);
                DocumentObjectBinder objectBinder = new DocumentObjectBinder();
                return objectBinder.getBean(viewClass, document);
            } catch (IOException e) {
                LOG.error("could not execute query ", e);
                throw new SolrServerException(e);
            }
        });
        try {
            T result = future.get(MAX_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            return result;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new SolrServerException("could not get bean " + id + " of class " + viewClass, e);
        }
    }

    /**
     * Returns the {@link Core} for target class of {@link Core#viewClass} if one could be found, <code>null</code>
     * otherwise.
     */
    private Core getCore(Class clazz) {
        for (Core core : Core.values())
            if (clazz.equals(core.viewClass))
                return core;
        return null;
    }

    private SolrClient getClient(Core core) {
        String coreURL = config.getString("solr.core." + core.value);
        return new HttpSolrClient(coreURL);
    }


}
