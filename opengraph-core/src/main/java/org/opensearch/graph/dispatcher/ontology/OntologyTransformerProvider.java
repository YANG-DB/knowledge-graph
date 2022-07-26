package org.opensearch.graph.dispatcher.ontology;







import org.opensearch.graph.model.ontology.transformer.OntologyTransformer;

import java.util.Collection;
import java.util.Optional;

public interface OntologyTransformerProvider {
    Optional<OntologyTransformer> transformer(String id);
    Collection<OntologyTransformer> transformation();
}
