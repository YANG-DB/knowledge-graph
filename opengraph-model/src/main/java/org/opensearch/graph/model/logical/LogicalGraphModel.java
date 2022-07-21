package org.opensearch.graph.model.logical;



import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LogicalGraphModel {
    private List<LogicalNode> nodes;
    private List<LogicalEdge> edges;

    public LogicalGraphModel() {
        this.edges = new ArrayList<>();
        this.nodes = new ArrayList<>();
    }

    public List<LogicalNode> getNodes() {
        return nodes;
    }

    public LogicalGraphModel with(LogicalNode node) {
        getNodes().add(node);
        return this;
    }

    public List<LogicalEdge> getEdges() {
        return edges;
    }

    public LogicalGraphModel with(LogicalEdge edge) {
        getEdges().add(edge);
        return this;
    }


}
