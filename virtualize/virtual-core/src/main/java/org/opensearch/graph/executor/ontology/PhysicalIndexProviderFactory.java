package org.opensearch.graph.executor.ontology;


import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.unipop.schemaProviders.PhysicalIndexProvider;

public interface PhysicalIndexProviderFactory {
    PhysicalIndexProvider get(Ontology ontology);
}
