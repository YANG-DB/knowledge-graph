package org.opensearch.graph.dispatcher.ontology;



import org.opensearch.graph.model.ontology.Ontology;

import java.util.Collection;
import java.util.Optional;

public interface OntologyProvider {
    Optional<Ontology> get(String id);

    Collection<Ontology> getAll();

    Ontology add(Ontology ontology);
}
