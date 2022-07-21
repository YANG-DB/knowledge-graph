package org.opensearch.graph.executor.ontology;


import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.unipop.schemaProviders.GraphLayoutProvider;

/**
 * Created by Roman on 23/05/2017.
 */
public interface GraphLayoutProviderFactory {
    GraphLayoutProvider get(Ontology ontology);
}
