package org.opensearch.graph.executor.ontology.schema;



import org.opensearch.graph.executor.resource.PersistentResourceStore;

import java.util.Arrays;

public class SystemIndicesProvider implements IndicesProvider{
    @Override
    public Iterable<String> indices() {
        return Arrays.asList(PersistentResourceStore.SYSTEM, PersistentResourceStore.PROJECTION);
    }

}
