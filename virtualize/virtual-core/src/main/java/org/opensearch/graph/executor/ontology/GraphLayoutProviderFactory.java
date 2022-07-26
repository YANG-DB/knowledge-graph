package org.opensearch.graph.executor.ontology;





import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.unipop.schemaProviders.GraphLayoutProvider;

public interface GraphLayoutProviderFactory {
    GraphLayoutProvider get(Ontology ontology);
}
