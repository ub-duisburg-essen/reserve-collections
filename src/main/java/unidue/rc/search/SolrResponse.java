package unidue.rc.search;


import java.util.Collections;
import java.util.List;

/**
 * Created by marcus.koesters on 15.06.15.
 */
public class SolrResponse<T> 
{

    private long count;

    private long offset;

    private List<T> items;

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public List<T> getItems() {
        return items == null
                ? Collections.EMPTY_LIST
                : items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }
}
