package org.opensearch.graph.unipop.controller;



import org.opensearch.graph.unipop.schemaProviders.GraphElementSchemaProvider;

public interface UniGraphElementSchemaProviderFactory {
    GraphElementSchemaProvider getSchemaProvider();
}
