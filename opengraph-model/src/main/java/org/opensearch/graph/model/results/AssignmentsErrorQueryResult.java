package org.opensearch.graph.model.results;





import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.opensearch.graph.model.resourceInfo.GraphError;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AssignmentsErrorQueryResult extends AssignmentsQueryResult {
    private GraphError error;

    public AssignmentsErrorQueryResult(GraphError error) {
        this.error = error;
    }

    @Override
    public int getSize() {
        return -1;
    }

    public GraphError error() {
        return error;
    }

    @Override
    public String toString() {
        return error().getErrorDescription();
    }

    @Override
    public String content() {
        return toString();
    }
}
