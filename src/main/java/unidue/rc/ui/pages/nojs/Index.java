package unidue.rc.ui.pages.nojs;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.annotations.ActivationRequestParameter;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import se.unbound.tapestry.breadcrumbs.BreadCrumb;
import se.unbound.tapestry.breadcrumbs.BreadCrumbReset;
import unidue.rc.model.solr.SolrCollectionView;
import unidue.rc.search.SolrQueryBuilder;
import unidue.rc.search.SolrResponse;
import unidue.rc.search.SolrService;
import unidue.rc.search.SolrSortField;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by nils on 26.09.16.
 */
@BreadCrumb(titleKey = "html.title")
@BreadCrumbReset
public class Index {

    private static final List<SortMapEntry> SORT_MAPPING = Stream.of(
            new SortMapEntry("location", SolrCollectionView.LOCATION_PROPERTY),
            new SortMapEntry("number", SolrCollectionView.COLLECTION_NUMBER_NUMERIC_PROPERTY),
            new SortMapEntry("expiration", SolrCollectionView.VALID_TO_PROPERTY),
            new SortMapEntry("title", SolrCollectionView.TITLE_PROPERTY)
    ).collect(Collectors.toList());

    private static final String SORT_PARAM_PATTERN = "(\\d+)_(asc|desc)";

    @Inject
    private Logger log;

    @Inject
    private PageRenderLinkSource linkSource;

    @Inject
    private Request request;

    @Inject
    private SolrService solrService;

    @ActivationRequestParameter
    @Property
    private String query;

    @ActivationRequestParameter
    private String location, number, title, expiration;

    @Property
    private SolrCollectionView collectionView;

    @Property
    private String author;

    @Property
    private String sortParam;

    @Property(write = false)
    private SolrResponse<SolrCollectionView> collections;

    @OnEvent(EventConstants.ACTIVATE)
    void onActivate() {
        loadCollections();
    }

    public List<String> getCurrentSortParams() {
        Link link = linkSource.createPageRenderLink(Index.class);
        List<String> result = link.getParameterNames().stream()
                .filter(p -> SORT_MAPPING.stream().anyMatch(m -> StringUtils.equals(p, m.ui)))
                .collect(Collectors.toList());
        return result;
    }

    public String getSortValue() {

        Link link = linkSource.createPageRenderLink(Index.class);
        Optional<String> sortValue = link.getParameterNames().stream()
                .filter(p -> StringUtils.equals(p, sortParam))
                .map(p -> link.getParameterValue(p))
                .findFirst();
        return sortValue.orElse(null);
    }

    public Link getSortLink(String fieldName) {

        /*
        the sort param is of the form <field>=<sort_number>_<order>
        to apply ordering in the correct selected order new fields must get a number higher than the current
        used number. already present fields must retain their number.
         */


        // link back to the current page including all parameters
        Link link = linkSource.createPageRenderLink(Index.class);

        Optional<Integer> max = link.getParameterNames()
                .stream()
                .filter(p -> SORT_MAPPING.stream()
                        .anyMatch(p2 -> p.endsWith(p2.ui))) // filter for all sort params
                .map(p -> link.getParameterValue(p).split("_")) // split for sort number
                .filter(v -> v.length > 0 && NumberUtils.isNumber(v[0]))  // safety check for numeric values
                .map(v -> Integer.valueOf(v[0]))  // map to integer
                .max((v1, v2) -> v1.compareTo(v2));  // get max value if present
        Integer newSortNum = max.isPresent()
                             ? max.get() + 1
                             : 0;

        // current ordering by fieldname
        String[] currentOrder = StringUtils.split(link.getParameterValue(fieldName), "_");

        // wrap to new field to apply the next order
        SolrSortField field = new SolrSortField(fieldName);

        if (currentOrder != null
                && currentOrder.length > 1
                && StringUtils.isNotBlank(currentOrder[1])) {

            field.setOrder(currentOrder[1]);
        }

        // apply new ordering
        field.applyNextSortOrder();

        Optional<String> optionalSortParam = link.getParameterNames()
                .stream()
                .filter(p -> StringUtils.endsWith(p, fieldName))
                .findAny();

        // replace parameter
        link.removeParameter(fieldName);
        SolrQuery.ORDER newOrder = field.getOrder();
        if (newOrder != null) {
            String sortNum = optionalSortParam.isPresent()
                             ? currentOrder[0]
                             : Integer.toString(newSortNum);
            link.addParameter(fieldName, sortNum + "_" + newOrder.name());
        }
        return link;
    }

    public String getClassForSort(String fieldName) {
        // link back to the current page including all parameters
        Link link = linkSource.createPageRenderLink(Index.class);

        // current ordering by fieldname
        String currentOrder = link.getParameterValue(fieldName);

        if (StringUtils.isBlank(currentOrder)) {
            return StringUtils.EMPTY;
        } else if (StringUtils.endsWith(currentOrder, SolrQuery.ORDER.asc.name())) {
            return "sort-icon-asc";
        } else {
            return "sort-icon-desc";
        }
    }

    public String format(String dateFormat, Date date) {
        return DateTimeFormat.forPattern(dateFormat).print(date.getTime());
    }

    public long getCollectionCount() {
        return collections.getCount();
    }

    void loadCollections() {
        // link back to the current page including all parameters
        Link link = linkSource.createPageRenderLink(Index.class);

        SolrQueryBuilder queryBuilder = solrService.createQueryBuilder();
        for (String param : link.getParameterNames()) {
            if (StringUtils.equals(param, "query") && StringUtils.isNotBlank(query)) {
                queryBuilder.singleCondition(SolrCollectionView.SEARCH_FIELD_PROPERTY, query);
            }
            link.getParameterNames().stream()
                    // filter for any sort parameter
                    .filter(p -> SORT_MAPPING.stream().anyMatch(pair -> StringUtils.equals(p, pair.ui)))
                    // filter any sort parameter that contains a valid pattern
                    .filter(p -> link.getParameterValue(p).matches(SORT_PARAM_PATTERN))
                    // split up to tuple [(number, order(asc|desc)), ui field name]
                    .map(p -> Pair.of(link.getParameterValue(p).split("_"), p))
                    // split up to triple [number, order(asc|desc), ui field name]
                    .map(p -> Triple.of(p.getLeft()[0], p.getLeft()[1], p.getRight()))
                    // sort by number
                    .sorted((obj1, obj2) -> Integer.valueOf(obj1.getLeft()).compareTo(Integer.valueOf(obj2.getLeft())))
                    // add sort field for each
                    .forEach(obj -> addSortField(obj.getRight(), obj.getMiddle(), queryBuilder));
        }
        SolrQuery query = queryBuilder.setCount(100).build();
        try {
            this.collections = solrService.query(SolrCollectionView.class, query);
        } catch (SolrServerException e) {
            log.error("could not query solr", e);
        }
    }

    private void addSortField(String uiFieldName, String order, SolrQueryBuilder queryBuilder) {
        // find mapping
        Optional<String> optionalSortParam = SORT_MAPPING.stream()
                .filter(m -> m.ui.equals(uiFieldName))
                .map(p -> p.backend)
                .findAny();

        if (optionalSortParam.isPresent()) {
            String sortParam = optionalSortParam.get();

            // create sort field
            SolrSortField sortField = new SolrSortField(sortParam);

            // set order according to given string value
            sortField.setOrder(order);

            SolrQuery.ORDER realOrder = sortField.getOrder();
            // add order to query if the string order contained a valid value
            if (realOrder != null)
                queryBuilder.addSortField(sortParam, realOrder);
        }
    }

    private static class SortMapEntry {
        private String ui;
        private String backend;

        public SortMapEntry(String ui, String backend) {
            this.ui = ui;
            this.backend = backend;
        }
    }
}
