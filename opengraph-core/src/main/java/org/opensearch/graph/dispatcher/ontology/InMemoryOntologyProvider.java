package org.opensearch.graph.dispatcher.ontology;



import org.opensearch.graph.model.GlobalConstants;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.ontology.OntologyFinalizer;

import java.io.IOException;
import java.util.*;

/**
 * Created by lior.perry on 3/16/2017.
 */
public class InMemoryOntologyProvider implements OntologyProvider {
    public static final String ONTOLOGY = GlobalConstants.ONTOLOGY;

    protected Map<String, Ontology> ontologyMap;

    public InMemoryOntologyProvider(Ontology... ontology) throws IOException {
        ontologyMap = new HashMap<>();
        Arrays.asList(ontology).forEach(ont ->
                ontologyMap.put(ont.getOnt(), ont));
    }

    @Override
    public Optional<Ontology> get(String id) {
        return Optional.ofNullable(ontologyMap.get(id));
    }

    @Override
    public Collection<Ontology> getAll() {
        return ontologyMap.values();
    }

    @Override
    public Ontology add(Ontology ontology) {
        ontologyMap.put(ontology.getOnt(), OntologyFinalizer.finalize(ontology));
        return ontology;
    }
}
