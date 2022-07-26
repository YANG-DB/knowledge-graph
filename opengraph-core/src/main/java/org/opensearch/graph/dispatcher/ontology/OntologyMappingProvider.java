package org.opensearch.graph.dispatcher.ontology;






import org.opensearch.graph.model.ontology.mapping.MappingOntologies;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public interface OntologyMappingProvider {
    Optional<MappingOntologies> get(String id);

    Collection<MappingOntologies> getAll();

    MappingOntologies add(MappingOntologies ontology);

    class VoidOntologyMappingProvider implements OntologyMappingProvider {

        @Override
        public Optional<MappingOntologies> get(String id) {
            return Optional.empty();
        }

        @Override
        public Collection<MappingOntologies> getAll() {
            return Collections.emptyList();
        }

        @Override
        public MappingOntologies add(MappingOntologies ontology) {
            return ontology;
        }
    }
}
