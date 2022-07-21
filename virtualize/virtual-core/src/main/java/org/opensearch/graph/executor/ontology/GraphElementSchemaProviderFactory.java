package org.opensearch.graph.executor.ontology;


import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.unipop.schemaProviders.GraphElementSchemaProvider;

/**
 * Created by Roman on 25/05/2017.
 */
public interface GraphElementSchemaProviderFactory {
    GraphElementSchemaProvider get(Ontology ontology);
}
