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






import org.opensearch.graph.model.GlobalConstants;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.ontology.OntologyFinalizer;

import java.io.IOException;
import java.util.*;

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
