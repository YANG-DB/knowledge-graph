package org.opensearch.graph.dispatcher.ontology;



import org.opensearch.graph.model.ontology.mapping.MappingOntologies;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by lior.perry on 3/16/2017.
 */
public class SimpleOntologyMappingProvider implements OntologyMappingProvider {

    private Map<String, MappingOntologies> ontologyMap;

    public SimpleOntologyMappingProvider(MappingOntologies ontology)  {
        ontologyMap = new HashMap<>();
        ontologyMap.put(ontology.getSourceOntology(), ontology);
    }

    @Override
    public Optional<MappingOntologies> get(String id) {
        return Optional.ofNullable(ontologyMap.get(id));
    }

    @Override
    public Collection<MappingOntologies> getAll() {
        return ontologyMap.values();
    }

    public MappingOntologies add(MappingOntologies ontology) {
        ontologyMap.put(ontology.getSourceOntology(),ontology);
        return ontology;
    }
}
