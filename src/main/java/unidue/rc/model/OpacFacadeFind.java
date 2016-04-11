package unidue.rc.model;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @author Nils Verheyen
 * @since 17.09.13 10:16
 */
public class OpacFacadeFind {

    @JsonProperty("sessionId")
    private String sessionId;

    @JsonProperty("count")
    private Integer count;

    @JsonProperty("setNumber")
    private Integer setNumber;

    @JsonProperty("records")
    private List<OpacFacadeBook> books;

    public String getSessionId() {
        return sessionId;
    }

    public Integer getCount() {
        return count;
    }

    public Integer getSetNumber() {
        return setNumber;
    }

    public List<OpacFacadeBook> getBooks() {
        return books;
    }

    public boolean isEmpty() {
        return books == null || books.isEmpty();
    }

    public boolean containsBooks() {
        return books != null && books.size() > 0;
    }
}
