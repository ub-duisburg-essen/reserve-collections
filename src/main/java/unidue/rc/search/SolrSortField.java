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

/**
 * A <code>SolrSortField</code> can be used to apply consistend ordering of different fields in solr.
 *
 * @author Nils Verheyen
 * @since 24.11.14 09:26
 */
public class SolrSortField {

    private String fieldName;

    private SolrQuery.ORDER order;

    public SolrSortField(String fieldName) {
        this.fieldName = fieldName;
    }

    public void resetSortOrder() {
        order = null;
    }

    /**
     * Apply the next {@link SolrQuery.ORDER} that should be used for ordering.
     */
    public void applyNextSortOrder() {

        if (order == null)
            order = SolrQuery.ORDER.asc;
        else if (order == SolrQuery.ORDER.asc)
            order = SolrQuery.ORDER.desc;
        else if (order == SolrQuery.ORDER.desc)
            order = null;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public SolrQuery.ORDER getOrder() {
        return order;
    }

    public void setOrder(SolrQuery.ORDER order) {
        this.order = order;
    }
}
