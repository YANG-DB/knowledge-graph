package org.opensearch.graph.executor.ontology;


import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.unipop.schemaProviders.PhysicalIndexProvider;

/**
 * Created by Roman on 11/05/2017.
 */
public interface PhysicalIndexProviderFactory {
    PhysicalIndexProvider get(Ontology ontology);
}
