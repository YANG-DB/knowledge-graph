package org.opensearch.graph.test.data;

/*-
 * #%L
 * dragons-test
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



import com.jayway.jsonpath.JsonPath;
import org.opensearch.graph.executor.ontology.GraphLayoutProviderFactory;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.unipop.schema.providers.GraphLayoutProvider;
import org.opensearch.graph.unipop.schema.providers.GraphRedundantPropertySchema;
import net.minidev.json.JSONArray;

import javax.inject.Inject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.opensearch.graph.model.Utils.readJsonFile;

/**
 * Created by lior.perry on 6/4/2017.
 */
public class DragonsOntologyGraphLayoutProviderFactory implements GraphLayoutProviderFactory {

    //region Constructors
    public DragonsOntologyGraphLayoutProviderFactory() throws IOException {
        this("DragonsGraphLayoutProviderFactory.json");
    }

    @Inject
    public DragonsOntologyGraphLayoutProviderFactory(String configFile) throws IOException {
        this.graphLayoutProviders = new HashMap<>();
        String conf = readJsonFile("schema/" + configFile);
        this.graphLayoutProviders.put("Dragons", (edgeType, property) -> {
            try {
                JSONArray array = JsonPath.read(conf, "$['entities'][?(@.type == '" + edgeType + "')]['redundant']");
                Optional<Object> first = array.stream().flatMap(v -> ((JSONArray) v).stream()).filter(p -> ((Map) p).get("name").equals(property.getName())).findFirst();
                if(!first.isPresent()) return Optional.empty();

                Map redundantProperty = (Map) first.get();
                return Optional.of(new GraphRedundantPropertySchema.Impl(
                        redundantProperty.get("name").toString(),
                        redundantProperty.get("redundant_name").toString(),
                        "string"));
            } catch (Exception e) {
                return Optional.empty();
            }
        });
    }
    //endregion

    //region GraphLayoutProviderFactory Implementation
    @Override
    public GraphLayoutProvider get(Ontology ontology) {
        return this.graphLayoutProviders.get(ontology.getOnt());
    }
    //endregion

    //region Fields
    private Map<String, GraphLayoutProvider> graphLayoutProviders;
    //endregion
}
