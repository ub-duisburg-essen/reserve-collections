package unidue.rc.search;

/**
 * Created by nils on 17.06.16.
 */
public class SolrTextQueryField implements SolrQueryField {

    private String key;
    private String value;

    public SolrTextQueryField(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public void add(SolrQueryBuilder queryBuilder) {
        queryBuilder.singleCondition(key, value);
    }
}
