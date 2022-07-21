package org.opensearch.graph.dispatcher.ontology;





import org.opensearch.graph.model.ontology.transformer.OntologyTransformer;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public class VoidOntologyTransformerProvider implements OntologyTransformerProvider {
    @Override
    public Optional<OntologyTransformer> transformer(String id) {
        return Optional.empty();
    }

    @Override
    public Collection<OntologyTransformer> transformation() {
        return Collections.emptyList();
    }
}
