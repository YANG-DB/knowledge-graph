package org.opensearch.graph.dispatcher.ontology;

/*-
 * #%L
 * opengraph-core
 * %%
 * Copyright (C) 2016 - 2022 org.opensearch
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */






import org.opensearch.graph.model.ontology.Ontology;

import java.io.IOException;
import java.util.*;

import static org.opensearch.graph.model.Utils.asObject;
import static org.opensearch.graph.model.Utils.readJsonFile;

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
