package org.opensearch.graph.dispatcher.ontology;


import org.opensearch.graph.model.ontology.Ontology;

import java.util.Collection;
import java.util.Optional;

/**
 * Created by lior.perry on 3/16/2017.
 */
public interface OntologyProvider {
    Optional<Ontology> get(String id);

    Collection<Ontology> getAll();

    Ontology add(Ontology ontology);
}
