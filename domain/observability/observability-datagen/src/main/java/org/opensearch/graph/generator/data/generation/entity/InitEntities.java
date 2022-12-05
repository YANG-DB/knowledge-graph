
package org.opensearch.graph.generator.data.generation.entity;

/*-
 * #%L
 * observability-datagen
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


import org.opensearch.graph.datagen.entities.OntologyEntity;
import org.opensearch.graph.generator.configuration.EntityConfigurationBase;
import org.opensearch.graph.model.ontology.Ontology;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toMap;
import static org.opensearch.graph.datagen.utilities.EntityGenerator.generate;

/**
 * Entities Factory generator
 */
public class InitEntities {
    private final Map<String, EntityConfigurationBase> settings;
    private final Ontology.Accessor accessor;

    public InitEntities(Map<String,EntityConfigurationBase> settings, Ontology.Accessor accessor  ) {
        this.settings = settings;
        this.accessor = accessor;
    }

    /**
     * factory method to generate map of ontology entities
     * @return
     */
    public Map<String, List<OntologyEntity>> initEntities() {
         return StreamSupport.stream(accessor.entities().spliterator(), false)
                .filter(entityType->settings.containsKey(entityType.geteType()))
                .map(entityType -> generate(settings.get(entityType.geteType()),accessor,entityType))
                .flatMap(Collection::stream)
                .collect(Collectors.groupingBy(e -> e.type));
    }
}
