package unidue.rc.ui.components;

import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;

import java.util.Arrays;
import java.util.Optional;

/**
 * <p>
 * A <code>AjaxSortLink</code> is rendered as a sort icon with possible values of no sort, ascending and descending.
 * If multiple sort links should be used, set the context, as it will be used as an id to access it in the template.
 * </p>
 */
public class AjaxSortLink {

    private static final String EVENT_NAME = "sort";

    @Parameter(defaultPrefix = "literal", name = "for")
    @Property
    private String id;

    @Parameter
    private SortState sortState;

    @Inject
    private AjaxResponseRenderer ajaxResponseRenderer;

    @Inject
    private Request request;

    @InjectComponent
    private Zone sortZone;

    @SetupRender
    void onSetupRender() {

        if (sortState == null)
            sortState = SortState.NoSort;
    }

    @OnEvent(value = EVENT_NAME)
    boolean onSort(String id, SortState nextSortState) {

        if (nextSortState == null)
            nextSortState = SortState.NoSort;

        sortState = nextSortState;

        if (request.isXHR())
            ajaxResponseRenderer.addRender(sortZone);
        return false;
    }

    public SortState getNextSortState() {
        if (sortState == null)
            return SortState.NoSort;

        Optional<SortState> nextSortState = sortState.next();
        return nextSortState.isPresent()
               ? nextSortState.get()
               : null;
    }

    public String getSortIconName() {
        return sortState != null
               ? sortState.iconName
               : "";
    }

    public String getSortZoneId() {
        return StringUtils.isBlank(id)
               ? "sortZone"
               : "sortZone_" + id;
    }

    public enum SortState {

        NoSort(0, 1, ""),
        Ascending(1, 2, "sort-icon-asc"),
        Descending(2, 0, "sort-icon-desc");

        private final int id;
        private final int successorId;
        private final String iconName;

        SortState(int id, int successorId, String iconName) {
            this.id = id;
            this.successorId = successorId;
            this.iconName = iconName;
        }

        Optional<SortState> next() {
            return Arrays.stream(values())
                    .filter(s -> s.id == this.successorId)
                    .findAny();
        }
    }
}
