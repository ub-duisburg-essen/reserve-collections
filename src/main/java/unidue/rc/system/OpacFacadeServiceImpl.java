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
package unidue.rc.system;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.cayenne.di.Inject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unidue.rc.model.OpacFacadeBook;
import unidue.rc.model.OpacFacadeFind;
import unidue.rc.model.OpacFacadeLibraryData;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;

/**
 * @author Nils Verheyen
 * @since 16.09.13 15:44
 */
public class OpacFacadeServiceImpl implements OpacFacadeService {

    private static final Logger LOG = LoggerFactory.getLogger(OpacFacadeServiceImpl.class);

    @Inject
    private SystemConfigurationService config;

    @Override
    public OpacFacadeFind search(String searchString) {

        try {
            searchString = URLEncoder.encode(searchString, Charset.defaultCharset().name());
        } catch (UnsupportedEncodingException e) {
            // ignored exception
            LOG.error("unsupported encoding", e);
        }

        // first search for signature
        String signatureURL = String.format(config.getString("opac.facade.sig.url"), searchString);
        OpacFacadeFind result = get(signatureURL);

        if (result == null) {
            // backup search for simple query
            String searchURL = String.format(config.getString("opac.facade.query.url"), searchString);
            LOG.debug("running query: " + searchURL);
            result = get(searchURL);
        }

        return result;
    }

    @Override
    public OpacFacadeBook getDetails(String docNumber) {

        String searchURL = String.format(config.getString("opac.facade.details.url"), docNumber);

        // execute query on opac facade
        HttpEntity entity = execute(searchURL);

        // parse result returned from facade
        OpacFacadeBook result = null;
        if (entity != null)
            result = parse(OpacFacadeBook.class, entity);

        return result;
    }

    @Override
    public OpacFacadeLibraryData getLibraryData(String docNumber) {
        String searchURL = String.format(config.getString("opac.facade.library.data.url"), docNumber);
        LOG.debug("Building SearchURL for doc: " + docNumber +" for url "+ searchURL);
        // execute query on opac facade
        HttpEntity entity = execute(searchURL);

        // parse result returned from facade
        OpacFacadeLibraryData result = null;
        if (entity != null)
            result = parse(OpacFacadeLibraryData.class, entity);

        return result;
    }

    /**
     * Tries to load data from target url and parse it as an {@link OpacFacadeFind} object.
     *
     * @param url url which must be correctly encoded
     * @return an {@link OpacFacadeFind} object if get and parse was successful
     */
    private OpacFacadeFind get(String url) {

        // execute query on opac facade
        HttpEntity entity = execute(url);

        // parse result returned from facade
        OpacFacadeFind result = null;
        if (entity != null)
            result = parse(OpacFacadeFind.class, entity);

        return result;
    }

    /**
     * Executes a new http get request on target url.
     *
     * @param queryURL
     * @return a new {@link HttpEntity} if the request was successful, <code>null</code> otherwise
     */
    private HttpEntity execute(String queryURL) {

        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(queryURL);
        HttpEntity entity = null;
        try {
            HttpResponse response = client.execute(request);

            entity = response.getEntity();
        } catch (IOException e) {
            LOG.error("could not execute get to " + queryURL, e);
        }
        return entity;
    }

    /**
     * Parses the input of target {@link HttpEntity} and returns an object of target type.
     *
     * @param clazz  class of object which should be returned
     * @param entity {@link HttpEntity} to read from
     * @return an object of target type if parse was successful, <code>null</code> otherwise
     */
    private <T> T parse(Class<T> clazz, HttpEntity entity) {

        T result = null;
        try {

            Charset charset = ContentType.getOrDefault(entity).getCharset();

            String responseStr = EntityUtils.toString(entity, charset);

            // use jackson to parse input
            ObjectMapper mapper = new ObjectMapper();

            result = mapper.readValue(responseStr, clazz);

            EntityUtils.consume(entity);
        } catch (IOException e) {
            LOG.error("could not parse input " + e.getMessage());
        }
        return result;
    }
}
