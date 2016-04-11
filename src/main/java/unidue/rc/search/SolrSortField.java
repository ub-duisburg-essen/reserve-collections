package unidue.rc.search;


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
