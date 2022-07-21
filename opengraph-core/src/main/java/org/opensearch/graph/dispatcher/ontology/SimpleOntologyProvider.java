package org.opensearch.graph.dispatcher.ontology;



import org.opensearch.graph.model.ontology.Ontology;

import java.io.IOException;
import java.util.*;

import static org.opensearch.graph.model.Utils.asObject;
import static org.opensearch.graph.model.Utils.readJsonFile;

/**
 * Created by lior.perry on 3/16/2017.
 */
public class SimpleOntologyProvider implements OntologyProvider {
    public static final String DRAGONS = "Dragons";
    public static final String ONTOLOGY = "ontology";

    private Map<String, Ontology> ontologyMap;

    public SimpleOntologyProvider(Ontology... ontology) throws IOException {
        ontologyMap = new HashMap<>();
        Arrays.asList(ontology).forEach(ont ->
                ontologyMap.put(ont.getOnt(), ont));
    }

    public SimpleOntologyProvider() throws IOException {
        ontologyMap = new HashMap<>();
        ontologyMap.put(DRAGONS, asObject(readJsonFile(ONTOLOGY + "/" + DRAGONS + ".json"), Ontology.class));
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
        ontologyMap.put(ontology.getOnt(), ontology);
        return ontology;
    }
}
