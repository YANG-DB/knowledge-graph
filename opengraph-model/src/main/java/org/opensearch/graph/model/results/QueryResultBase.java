package org.opensearch.graph.model.results;




import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "resultType")
@JsonSubTypes({
        @JsonSubTypes.Type(name = "assignments", value = AssignmentsQueryResult.class),
        @JsonSubTypes.Type(name = "csv", value = CsvQueryResult.class)
})
public abstract class QueryResultBase {
    private String queryId;
    private String cursorId;
    private long timestamp;
    @JsonIgnore
    public abstract int getSize();

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setQueryId(String queryId) {
        this.queryId = queryId;
    }

    public String getQueryId() {
        return queryId;
    }

    public String getCursorId() {
        return cursorId;
    }

    public void setCursorId(String cursorId) {
        this.cursorId = cursorId;
    }
}
