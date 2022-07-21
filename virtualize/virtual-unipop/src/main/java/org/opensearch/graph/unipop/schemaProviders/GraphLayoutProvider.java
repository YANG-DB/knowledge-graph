package org.opensearch.graph.unipop.schemaProviders;



import java.util.Optional;

public interface GraphLayoutProvider {
    class NoneRedundant implements GraphLayoutProvider {
        public static GraphLayoutProvider getInstance() {
            return instance;
        }
        private static GraphLayoutProvider instance = new NoneRedundant();

        //region GraphLayoutProvider Implementation
        @Override
        public Optional<GraphRedundantPropertySchema> getRedundantProperty(String edgeType, GraphElementPropertySchema property) {
            return Optional.empty();
        }
        //endregion
    }

    Optional<GraphRedundantPropertySchema> getRedundantProperty(String edgeType, GraphElementPropertySchema property);
}
