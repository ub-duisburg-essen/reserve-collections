package unidue.rc.search;

/**
 * Created by nils on 17.06.16.
 */
public class SolrNumberQueryField implements SolrQueryField {

    private String key;
    private Number value;

    public SolrNumberQueryField(String key, Number value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public void add(SolrQueryBuilder queryBuilder) {
        queryBuilder.singleEqualCondition(key, value.toString());
    }
}
