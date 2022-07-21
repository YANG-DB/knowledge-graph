package org.opensearch.graph.unipop.controller.common.context;


import org.opensearch.graph.unipop.schemaProviders.GraphElementSchemaProvider;

/**
 * Created by roman.margolis on 12/09/2017.
 */
public interface SchemaProviderContext {
    GraphElementSchemaProvider getSchemaProvider();
}
