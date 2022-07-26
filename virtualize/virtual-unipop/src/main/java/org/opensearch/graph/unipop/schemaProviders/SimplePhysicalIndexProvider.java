package org.opensearch.graph.unipop.schemaProviders;





import org.opensearch.graph.unipop.schemaProviders.indexPartitions.IndexPartitions;
import org.opensearch.graph.unipop.schemaProviders.indexPartitions.StaticIndexPartitions;
import org.opensearch.graph.unipop.structure.ElementType;

import java.util.Collections;

public class SimplePhysicalIndexProvider implements PhysicalIndexProvider {
    //region Constructors
    public SimplePhysicalIndexProvider(String vertexIndexName, String edgeIndexName) {
        this.vertexIndexName = vertexIndexName;
        this.edgeIndexName = edgeIndexName;
    }
    //endregion

    //region PhysicalIndexProvider Implementation
    @Override
    public IndexPartitions getIndexPartitionsByLabel(String label, ElementType elementType) {
        switch (elementType) {
            case edge: return new StaticIndexPartitions(Collections.singletonList(this.edgeIndexName));
            case vertex: return new StaticIndexPartitions(Collections.singletonList(this.vertexIndexName));
            default: return null;
        }
    }
    //endregion

    //region Fields
    private String vertexIndexName;
    private String edgeIndexName;
    //endregion
}
