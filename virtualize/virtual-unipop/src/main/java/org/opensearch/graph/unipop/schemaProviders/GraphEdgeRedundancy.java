package org.opensearch.graph.unipop.schemaProviders;



import java.util.Optional;

public interface GraphEdgeRedundancy {
    Optional<String> getRedundantPropertyName(String propertyName);
}
