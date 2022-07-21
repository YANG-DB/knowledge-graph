package org.opensearch.graph.unipop.schemaProviders;


public interface GraphElementRouting {
    GraphElementPropertySchema getRoutingProperty();

    class Impl implements GraphElementRouting {
        //region Constructors
        public Impl(GraphElementPropertySchema routingProperty) {
            this.routingProperty = routingProperty;
        }
        //endregion

        //region GraphElementRouting Implementation
        @Override
        public GraphElementPropertySchema getRoutingProperty() {
            return this.routingProperty;
        }
        //endregion

        //region Fields
        private GraphElementPropertySchema routingProperty;
        //endregion
    }
}
