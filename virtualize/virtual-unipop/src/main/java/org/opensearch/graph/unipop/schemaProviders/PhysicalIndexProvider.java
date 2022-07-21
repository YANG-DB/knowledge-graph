package org.opensearch.graph.unipop.schemaProviders;


import org.opensearch.graph.unipop.schemaProviders.indexPartitions.IndexPartitions;
import org.opensearch.graph.unipop.structure.*;
import org.opensearch.graph.unipop.structure.ElementType;


/**
 * Created by benishue on 23-Mar-17.
 */
public interface PhysicalIndexProvider {
    class Constant implements PhysicalIndexProvider {
        //region Constructors
        public Constant(IndexPartitions indexPartitions) {
            this.indexPartitions = indexPartitions;
        }
        //endregion

        //region PhysicalIndexProvider Implementation
        @Override
        public IndexPartitions getIndexPartitionsByLabel(String label, ElementType elementType) {
            return this.indexPartitions;
        }
        //endregion

        //region Fields
        private IndexPartitions indexPartitions;
        //endregion
    }

    IndexPartitions getIndexPartitionsByLabel(String label, ElementType elementType);
}
