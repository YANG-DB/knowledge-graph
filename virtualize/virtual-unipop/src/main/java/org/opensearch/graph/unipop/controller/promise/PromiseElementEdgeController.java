package org.opensearch.graph.unipop.controller.promise;


import com.codahale.metrics.MetricRegistry;
import org.opensearch.graph.unipop.controller.OpensearchGraphConfiguration;
import org.opensearch.graph.unipop.schemaProviders.GraphElementSchemaProvider;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.opensearch.client.Client;
import org.unipop.query.search.SearchQuery;
import org.unipop.structure.UniGraph;

import java.util.Iterator;

/**
 * Created by lior.perry on 4/2/2017.
 */ //region PromiseElementEdgeController Implementation
public class PromiseElementEdgeController implements SearchQuery.SearchController {
    public PromiseElementEdgeController(Client client, OpensearchGraphConfiguration configuration, UniGraph graph, GraphElementSchemaProvider schemaProvider, MetricRegistry metricRegistry) {}

    //region SearchQuery.SearchController Implementation
    @Override
    public <E extends Element> Iterator<E> search(SearchQuery<E> searchQuery) {
        return null;
    }
    //endregion
}
