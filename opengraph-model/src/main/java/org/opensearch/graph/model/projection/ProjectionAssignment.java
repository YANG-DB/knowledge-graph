package org.opensearch.graph.model.projection;



import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.opensearch.graph.model.GlobalConstants;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectionAssignment {
    private List<ProjectionNode> nodes;
    private long id;
    private String queryId;
    private String cursorId;
    private long timestamp;

    public ProjectionAssignment(long id,String queryId,String cursorId,long timestamp) {
        this.id = id;
        this.queryId = queryId;
        this.cursorId = cursorId;
        this.timestamp = timestamp;
        this.nodes = new ArrayList<>();
    }

    public List<ProjectionNode> getNodes() {
        return nodes;
    }

    public long getId() {
        return id;
    }

    public String getQueryId() {
        return queryId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getType() {
        return GlobalConstants.ProjectionConfigs.PROJECTION;
    }

    public ProjectionAssignment with(ProjectionNode node) {
        getNodes().add(node);
        return this;
    }

    public ProjectionAssignment withAll(List<ProjectionNode> nodes) {
        this.nodes.addAll(nodes);
        return this;
    }

    public String getCursorId() {
        return cursorId;
    }
}
