package org.opensearch.graph.model.schema;

/*-
 * #%L
 * opengraph-model
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
import org.opensearch.graph.model.validation.ValidationResult;
import org.opensearch.graph.model.validation.ValidationResult.ValidationResults;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.opensearch.graph.model.ontology.OntologyFinalizer.*;

/**
 *
 */

/**
 * schema validation utility - this code will verify both logical & physical config is valid and in sync with each other
 * <p><br>
 *
 * Ontology-entities - checks
 * <br> - verify each top level entity has both ID & TYPE metadata fields
 * <br> - verify each top level entity all its properties
 * <br> - verify that if a top level entity has nested entities - these entities has top level representation in addition to the nesting
 * <br> - verify all cascading fields appear in the properties
 * <br> - verify properties has a valid type (primitive or entity type)
 *
 * <p><br>
 * Ontology-relations - checks
 *  <br>- verify each top level relation has both ID & TYPE metadata fields
 *  <br>- verify each top level relation all its properties
 *  <br>- verify that if a top level relation has nested relations - these relations has top level representation in addition to the nesting
 *  <br>- verify all cascading fields appear in the properties
 *  <br>- verify all relation pairs has matching existing entities
 *
 * <p><br>
 * Index-provider - checks
 * <br> - verify all index entities has corresponding ontology entities
 * <br> - verify all index relations has corresponding ontology relations
 * <br> - verify relation redundant fields have correct type and legit entity side
 */
public class SchemaValidator {

    public ValidationResults validate(IndexProvider provider, Ontology.Accessor accessor) {
        ValidationResults results = new ValidationResults();

        /*
         * Ontology-entities / relations - checks
         *  verify each top level entity has both ID & TYPE metadata fields
         *  verify each top level entity all its properties
         *  verify that if a top level entity has nested entities - these entities has top level representation in addition to the nesting
         *  verify all cascading fields appear in the properties
         *  verify properties has a valid type (primitive or entity type)
         */

        // ID field verification
        StreamSupport.stream(accessor.entities().spliterator(), false)
                .filter(e -> e.containsProperty(ID_FIELD_PTYPE)).collect(Collectors.toList())
                .forEach(e -> results.with(new ValidationResult(false, String.format("%s entity missing ID field", e.getName()))));

        accessor.relations().stream()
                .filter(r -> r.containsProperty(ID_FIELD_PTYPE)).collect(Collectors.toList())
                .forEach(r -> results.with(new ValidationResult(false, String.format("%s relation missing ID field", r.getName()))));

        // type field verification
        StreamSupport.stream(accessor.entities().spliterator(), false)
                .filter(e-> e.containsProperty(TYPE_FIELD_PTYPE)).collect(Collectors.toList())
                .forEach(e -> results.with(new ValidationResult(false, String.format("%s entity missing TYPE field", e.getName()))));

        accessor.relations().stream()
                .filter(r-> r.containsProperty(TYPE_FIELD_PTYPE)).collect(Collectors.toList())
                .forEach(r -> results.with(new ValidationResult(false, String.format("%s relation missing TYPE field", r.getName()))));

        // general entities/relations properties verification
        StreamSupport.stream(accessor.entities().spliterator(), false)
                .forEach(e -> e.getProperties()
                        .stream().filter(p -> !accessor.pNameOrType(p).isPresent())
                        .collect(Collectors.toList())
                        .forEach(p -> results.with(new ValidationResult(false, String.format("%s entity missing %s property definition",e.getName(), p))))
                );

        accessor.relations().stream()
                .forEach(e -> e.getProperties()
                        .stream().filter(p -> !accessor.pNameOrType(p).isPresent())
                        .collect(Collectors.toList())
                        .forEach(p -> results.with(new ValidationResult(false, String.format("%s relation missing %s property definition",e.getName(), p))))
                );

        // nested entities/relations properties verification
        StreamSupport.stream(accessor.entities().spliterator(), false)
                .map(e -> accessor.nested(e.geteType()))
                .forEach(ne ->
                        ne.stream()
                                .filter(p -> !accessor.pNameOrType(p).isPresent())
                                .forEach(p -> results.with(new ValidationResult(false, String.format("%s nested entity definition is missing",p))))
                );

        accessor.relations().stream()
                .map(r -> accessor.nested(r.getrType()))
                .forEach(nr ->
                        nr.stream()
                                .filter(p -> !accessor.pNameOrType(p).isPresent())
                                .forEach(p -> results.with(new ValidationResult(false, String.format("%s nested relation definition is missing",p))))
                );


        // cascading entities/relations properties verification
        StreamSupport.stream(accessor.entities().spliterator(), false)
                .map(e -> accessor.cascadingElementFieldsPType(e.geteType()))
                .forEach(ne ->
                        ne.stream()
                                .filter(p -> !accessor.pNameOrType(p).isPresent())
                                .forEach(p -> results.with(new ValidationResult(false, String.format("%s nested entity cascading field definition is missing",p))))
                );

        StreamSupport.stream(accessor.relations().spliterator(), false)
                .map(r -> accessor.cascadingElementFieldsPType(r.getrType()))
                .forEach(nr ->
                        nr.stream()
                                .filter(p -> !accessor.pNameOrType(p).isPresent())
                                .forEach(p -> results.with(new ValidationResult(false, String.format("%s nested relation cascading field definition is missing",p))))
                );

       accessor.relations()
                .forEach(r -> r.getePairs()
                    .forEach(pair -> {
                        if(!accessor.entity(pair.geteTypeA()).isPresent())
                            results.with(new ValidationResult(false, String.format("%s relation pair sideA %s type is missing from ontology",r.getrType(),pair.geteTypeA())));
                        if(!accessor.entity(pair.geteTypeB()).isPresent())
                            results.with(new ValidationResult(false, String.format("%s relation pair sideB %s type is missing from ontology",r.getrType(),pair.geteTypeB())));
                        })
                );

            /*
            *        Index-provider - checks
            *             verify all index entities has corresponding ontology entities
            *             verify all index relations has corresponding ontology relations
            *             verify all nested index entities / relations has corresponding top level index definitions as embedded
            *             verify relation redundant fields have correct type and legit entity side
            */

        provider.getEntities().stream()
                .filter(i->!accessor.entity(i.getType().getName()).isPresent())
                .forEach(i->results.with(new ValidationResult(false, String.format("%s entity index definition is missing from ontology",i.getType().getName()))));

        provider.getRelations().stream()
                .filter(i->!accessor.relation(i.getType().getName()).isPresent())
                .forEach(i->results.with(new ValidationResult(false, String.format("%s relation index definition is missing from ontology",i.getType().getName()))));

        provider.getTopLevelEntities()
                .forEach(e->e.getNested().stream()
                            .filter(ne->!provider.getEntity(ne.getType().getName()).isPresent())
                            .forEach(ne->results.with(new ValidationResult(false, String.format("%s nested index entity %s definition is missing from top level indices",e.getType(),ne))))
                            );

        provider.getTopLevelRelations()
                .forEach(r->r.getNested().stream()
                            .filter(ne->!provider.getRelation(ne.getType().getName()).isPresent())
                            .forEach(ne->results.with(new ValidationResult(false, String.format("%s nested index relation %s definition is missing from top level indices",r.getType(),ne))))
                            );

        provider.getRelations()
                .forEach(r->r.getRedundant().stream()
                        .filter(rp->!accessor.pNameOrType(rp.getName()).isPresent())
                        .forEach(rp->results.with(new ValidationResult(false, String.format("%s Redundant index %s relation property definition is missing from ontology",r.getType(),rp))))
                );

        return results;
    }
}
