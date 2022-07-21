package org.opensearch.graph.dispatcher.descriptors;





import org.opensearch.graph.dispatcher.resource.QueryResource;
import org.opensearch.graph.model.descriptors.Descriptor;

public class QueryResourceDescriptor implements Descriptor<QueryResource> {
    //region Descriptor Implementation
    @Override
    public String describe(QueryResource item) {
        return String.format("Query{id: %s, name: %s, ont: %s}",
                item.getQueryMetadata().getId(),
                item.getQueryMetadata().getName(),
                item.getQuery().getOnt());
    }
    //endregion
}
