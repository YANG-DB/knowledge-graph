package org.opensearch.graph.model.ontology;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opensearch.graph.model.descriptors.Descriptor;

public class OntologyDescriptor implements Descriptor<Ontology> {
    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public String describe(Ontology item) {
        try {
            return mapper.writeValueAsString(item);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
