
package unidue.rc.ui.components;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.annotations.BeginRender;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;

/**
 * Created by marcus.koesters on 10.06.15.
 */
public class Pagination {

    @Inject
    private Logger log;


    @Inject
    private ComponentResources resources;

    @Parameter(required = true)
    private int itemCount;

    private int currentPageNumber = 1;

    @Parameter(required = true)
    private int range;

    @Property
    private int index;

    @Property
    private int pageCount;

    @Property
    private int min;

    @Property
    private int max;

    @Parameter(required = true)
    private int maxRowsPerPage;

    @Property
    @Persist(PersistenceConstants.SESSION)
    private int selectedMaxRows;

    //preselect default-value
    @SetupRender
    void init() {
        if (selectedMaxRows == 0)
            selectedMaxRows = maxRowsPerPage;
    }

    @BeginRender
    void beginRender() {
        if (itemCount == 0)
            pageCount = 1;
        else
            pageCount = itemCount / selectedMaxRows;
        if (itemCount % selectedMaxRows != 0) {
            pageCount++;
        }
        min = currentPageNumber - range;
        max = currentPageNumber + range;
        if (min <= 0) {
            int offset = Math.abs(min) + 1;
            min += offset;
            max += offset;
        }
        if (max > pageCount) {
            max = pageCount;
        }
    }

    public int getCurrentPageNumber() {
        return currentPageNumber;
    }

    public int getPageNumber() {
        return index + min;
    }

    public boolean hasNext() {
        return currentPageNumber < pageCount;
    }

    public boolean isCurrent() {
        return getPageNumber() == currentPageNumber;
    }

    public boolean hasPrev() {
        return currentPageNumber > 1;
    }

    public String getPageName() {
        return resources.getPageName();
    }

    public int getMaxRowsPerPage() {
        return selectedMaxRows == 0
               ? maxRowsPerPage
               : selectedMaxRows;
    }

    public int getRange() {
        return range;
    }

    public int getFirstPage() {
        return 1;
    }

    public int getLastPage() {
        return pageCount;
    }

    public int getNextPage() {

        return currentPageNumber < pageCount
               ? currentPageNumber + 1
               : pageCount;
    }

    public int getPreviousPage() {
        return currentPageNumber > 1
               ? currentPageNumber - 1
               : 1;
    }

    void onUpdateZone(int currentPageNumber) {
        this.currentPageNumber = currentPageNumber;
        resources.triggerEvent("updateZones", new Object[]{currentPageNumber}, null);
    }

    public void resetCurrentPage() {
        currentPageNumber = 1;
    }
}

