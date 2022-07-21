package org.opensearch.graph.unipop.schemaProviders;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

public interface GraphRedundantPropertySchema extends GraphElementPropertySchema{
    String getPropertyRedundantName();

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonIgnoreProperties(ignoreUnknown = true)
    class Impl extends GraphElementPropertySchema.Impl implements GraphRedundantPropertySchema {
        //region Constructors
        public Impl(String name, String redundantName, String type) {
            super(name, type);
            this.propertyRedundantName = redundantName;
        }

        public Impl(String name, String redundantName, String type, Iterable<IndexingSchema> indexingSchemes) {
            super(name, type, indexingSchemes);
            this.propertyRedundantName = redundantName;
        }
        //endregion

        //region GraphElementPropertySchema Implementation
        @Override
        public String getPropertyRedundantName() {
            return this.propertyRedundantName;
        }
        //endregion

        //region Fields
        private String propertyRedundantName;
        //endregion
    }
}
