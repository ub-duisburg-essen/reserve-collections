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
package unidue.rc.search;


import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * A <code>SolrQueryBuilder</code> is a convenience class to build {@link org.apache.solr.client.solrj.SolrQuery}.
 *
 * @author Nils Verheyen
 * @see SolrService#query(Class, org.apache.solr.client.solrj.SolrQuery)
 * @since 20.11.14 14:27
 */
public class SolrQueryBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(SolrQueryBuilder.class);

    private static final String DIVIDER = ":";
    private static final String WILDCARD_SIGN = "*";

    private Map<String, SolrQuery.ORDER> sortFields = new LinkedHashMap<>();

    private SolrQuery query;

    private StringBuilder queryStringBuilder = new StringBuilder();

    public SolrQueryBuilder() {
        query = new SolrQuery();
    }

    public SolrQueryBuilder addSortField(String field, SolrQuery.ORDER order) {
        sortFields.put(field, order);
        return this;
    }

    public SolrQueryBuilder setOffset(int offset) {
        query.setStart(offset);
        return this;
    }

    public SolrQueryBuilder setCount(int count) {
        query.setRows(count);
        return this;
    }

    /**
     * Starts a new matching condition for a field that value must match. The condition must be closed with {@link
     * SolrQueryBuilder#closeCondition}.
     *
     * @return this instance
     */
    public SolrQueryBuilder startCondition() {
        queryStringBuilder.append("+(");
        return this;
    }

    /**
     * Closes the current matching condition for a field.
     *
     * @return this instance
     */
    public SolrQueryBuilder closeCondition() {
        queryStringBuilder.append(")");
        return this;
    }

    /**
     * Adds a field match against a part of the value by target value.
     *
     * @param field field to query
     * @param value matching value
     * @return this instance
     */
    public SolrQueryBuilder query(String field, String value) {
        addLike(field, value);
        return this;
    }

    /**
     * Adds a field match against a part of the value by target value.
     *
     * @param field field to query
     * @param value matching value
     * @return this instance
     */
    public SolrQueryBuilder qEqual(String field, String value) {
        addEquals(field, value);
        return this;
    }

    /**
     * Adds an or field match against a part of the value by target value.
     *
     * @param field field to query
     * @param value matching value
     * @return this instance
     */
    public SolrQueryBuilder or(String field, String value) {
        if (queryStringBuilder.length() > 0)
            queryStringBuilder.append(" or ");
        addLike(field, value);
        return this;
    }

    /**
     * Adds an or field match against a part of the value by target value.
     *
     * @param field field to query
     * @param value matching value
     * @return this instance
     */
    public SolrQueryBuilder orEqual(String field, String value) {
        if (queryStringBuilder.length() > 0)
            queryStringBuilder.append(" or ");
        addEquals(field, value);
        return this;
    }

    /**
     * Adds an and field match against a part of the value by target value.
     *
     * @param field field to query
     * @param value matching value
     * @return this instance
     */
    public SolrQueryBuilder and(String field, String value) {
        if (queryStringBuilder.length() > 0)
            queryStringBuilder.append(" and ");
        addLike(field, value);
        return this;
    }

    /**
     * Adds an and field match against a part of the value by target value.
     *
     * @param field field to query
     * @param value matching value
     * @return this instance
     */
    public SolrQueryBuilder andEqual(String field, String value) {
        if (queryStringBuilder.length() > 0)
            queryStringBuilder.append(" and ");
        addEquals(field, value);
        return this;
    }

    /**
     * Adds a condition with target field and value to this query.
     *
     * @param field field to query
     * @param value matching value
     * @return this instance
     */
    public SolrQueryBuilder singleCondition(String field, String value) {
        startCondition();
        addLike(field, value);
        closeCondition();
        return this;
    }

    /**
     * Adds a condition with target field and value to this query where value has to be equal to field.
     *
     * @param field field to query
     * @param value matching value
     * @return this instance
     */
    public SolrQueryBuilder singleEqualCondition(String field, String value) {
        startCondition();
        addEquals(field, value);
        closeCondition();
        return this;
    }

    /**
     * Builds the {@link SolrQuery} object with all given conditions.
     *
     * @return the new query
     */
    public SolrQuery build() {
        for (Map.Entry<String, SolrQuery.ORDER> item : sortFields.entrySet()) {
            query.addSort(item.getKey(), item.getValue());
        }
        query.setQuery(queryStringBuilder.toString());
        if (queryStringBuilder.length() == 0)
            query.setQuery(WILDCARD_SIGN);

        return query;
    }

    private void addLike(String field, String value) {
        queryStringBuilder.append(field);
        queryStringBuilder.append(DIVIDER);
        queryStringBuilder.append(WILDCARD_SIGN);
        queryStringBuilder.append(trimSearchValue(value));
        queryStringBuilder.append(WILDCARD_SIGN);
    }

    private void addEquals(String field, String value) {
        queryStringBuilder.append(field);
        queryStringBuilder.append(DIVIDER);
        queryStringBuilder.append(value);
    }

    private String trimSearchValue(String value) {
        if (value == null)
            return null;

        value = StringUtils.removePattern(value, "^([\\s\\" + WILDCARD_SIGN + "])*");
        value = StringUtils.removePattern(value, "([\\s\\" + WILDCARD_SIGN + "])*$");
        return value;
    }
}
