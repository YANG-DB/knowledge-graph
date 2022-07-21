package org.opensearch.graph.model.transport;



import com.fasterxml.jackson.annotation.JsonInclude;
import org.opensearch.graph.model.transport.cursor.CreateCursorRequest;

public interface CreateQueryRequestMetadata<T> {
    String TYPE_V1QL = "v1";
    String TYPE_CYPHERQL = "cypher";
    String TYPE_GRAPHQL = "graphQL";
    String TYPE_SPARQL = "sparkQL";

    boolean isSearchPlan();

    String getId();

    String getName();

    String getOntology();

    T getQuery();

    StorageType getStorageType();

    String getType();

    /**
     * Type of physical query -
     *  - concrete - to be executed
     *  - parameterized - to be saved as parameterized template
     * @return
     */
    QueryType getQueryType();

    @JsonInclude(JsonInclude.Include.NON_NULL)
    CreateCursorRequest getCreateCursorRequest();

    long getTtl();

    PlanTraceOptions getPlanTraceOptions();

    enum QueryLanguage {
        v1,cypher,sparql,graphql
    }

    enum StorageType {
        _stored,
        _volatile;
    }

    enum QueryType {
        concrete,
        parameterized


    }
}

