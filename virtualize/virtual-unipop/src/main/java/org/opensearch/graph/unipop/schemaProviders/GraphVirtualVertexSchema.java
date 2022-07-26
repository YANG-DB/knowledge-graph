package org.opensearch.graph.unipop.schemaProviders;





import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

public interface GraphVirtualVertexSchema extends GraphVertexSchema {
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonIgnoreProperties(ignoreUnknown = true)
    class Impl extends GraphVertexSchema.Impl implements GraphVirtualVertexSchema {
        //region Constructors
        public Impl(String label) {
            super(label);
        }
        //endregion
    }
}
