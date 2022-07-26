package org.opensearch.graph.executor.ontology.schema.load;





import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.opensearch.graph.model.resourceInfo.GraphError;
import org.opensearch.graph.model.results.LoadResponse;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Response implements LoadResponse.CommitResponse<String, GraphError> {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<GraphError> failures;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> success;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String index;

    public Response() {
    }

    public Response(String index) {
        this.index = index;
        this.failures = new ArrayList<>();
        this.success = new ArrayList<>();
    }

    public Response failure(GraphError err) {
        failures.add(err);
        return this;
    }

    public Response failure(List<GraphError> failed) {
        this.failures.addAll(failed);
        return this;
    }

    public Response success(String itemId) {
        success.add(itemId);
        return this;
    }

    public Response success(List<String> itemIds) {
        success.addAll(itemIds);
        return this;
    }

    public List<GraphError> getFailures() {
        return failures;
    }

    public List<String> getSuccesses() {
        return success;
    }

    public String getIndex() {
        return index;
    }

}
