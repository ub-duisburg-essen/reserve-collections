package unidue.rc.ui.pages.nojs;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.annotations.ActivationRequestParameter;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;
import org.slf4j.Logger;
import se.unbound.tapestry.breadcrumbs.BreadCrumb;
import se.unbound.tapestry.breadcrumbs.BreadCrumbReset;
import unidue.rc.model.solr.SolrCollectionView;
import unidue.rc.search.SolrResponse;
import unidue.rc.search.SolrSortField;

import java.text.Format;
import java.text.SimpleDateFormat;

/**
 * Created by nils on 26.09.16.
 */
@BreadCrumb(titleKey = "html.title")
@BreadCrumbReset
public class Index {

    private static final Format SMALL_DATE_FORMAT = new SimpleDateFormat("dd.MM.yy");

    @Inject
    private Logger log;

    @Inject
    private PageRenderLinkSource linkSource;

    @Inject
    private Request request;

    @ActivationRequestParameter
    @Property
    private String query;

    @ActivationRequestParameter
    private String location, number, title, expiration;

    @Property
    private SolrCollectionView collectionView;

    @Property
    private String author;

    public Link getSortLink(String fieldName) {

        // link back to the current page including all parameters
        Link link = linkSource.createPageRenderLink(Index.class);

        // current ordering by fieldname
        String currentOrder = link.getParameterValue(fieldName);

        // wrap to new field to apply the next order
        SolrSortField field = new SolrSortField(fieldName);

        if (StringUtils.isNotBlank(currentOrder)) {
            field.setOrder(currentOrder);
        }

        // apply new ordering
        field.applyNextSortOrder();

        // replace parameter
        link.removeParameter(fieldName);
        SolrQuery.ORDER newOrder = field.getOrder();
        if (newOrder != null)
            link.addParameter(fieldName, newOrder.name());
        return link;
    }

    public String getClassForSort(String fieldName) {
        // link back to the current page including all parameters
        Link link = linkSource.createPageRenderLink(Index.class);

        // current ordering by fieldname
        String currentOrder = link.getParameterValue(fieldName);

        if (StringUtils.isBlank(currentOrder)) {
            return StringUtils.EMPTY;
        } else if (StringUtils.equals(currentOrder, SolrQuery.ORDER.asc.name())) {
            return "sort-icon-asc";
        } else {
            return "sort-icon-desc";
        }
    }

    public Format getSmallDateFormat() {
        return SMALL_DATE_FORMAT;
    }

    public int getCollectionCount() {
        return 0;
    }

    public SolrResponse getCollections() {
        // link back to the current page including all parameters
        Link link = linkSource.createPageRenderLink(Index.class);

        for (String param : link.getParameterNames()) {

        }
        return new SolrResponse();
    }

}
