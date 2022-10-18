package org.opensearch.graph.asg.strategy.schema;

/*-
 * #%L
 * opengraph-asg
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


import org.opensearch.graph.asg.strategy.AsgStrategy;
import org.opensearch.graph.dispatcher.ontology.OntologyProvider;
import org.opensearch.graph.executor.ontology.GraphElementSchemaProviderFactory;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;
import org.opensearch.graph.model.ontology.Ontology;

/**
 * the Step reduction strategy attempts to locate steps from the form "Entity->Rel->Entity->Constraint" that can be reduced
 * into "Entity->Constraint" due to the fact that the relation and the following entity and constraint are actually nesting
 * inside the left most entity and therefore can be compacted into embedded/nested constraint and given to the engine to be processed correctly
 *
 * Example:
 *    for the ontology:
 *    entity: A {
 *        fields : aa
 *        nested entity : B {
 *          fields : cc
 *        }
 *    },
 *    entity: D {
 *        fields : dd
 *     }
 *
 *    relations {
 *        hasB [A:B]
 *        hasD [C:D]
 *    }
 *
 *    the query
 *      (a:A)-[hasB]-(b:B { b.cc > 1 })-[hasD]-(d:D { d.dd = "hey" })
 *
 *    should be reduced to
 *
 *      (a:A { a.b.cc > 1})-[hasD]-(d:D { d.dd = "hey" })
 *
 *    notice
 *     - if no constraints are set for nested entities:
 *        - if nested entities are embedded - reduce the nested entities since they will appear in the parent entity
 *        - if nested entities are nested -   reduce the nested entities since they will appear in the parent entity
 *        - if nested entities are child -    reduce the nested entities since they will appear in the parent entity
 *
 */
public class AsgStepReductionStrategy implements AsgStrategy {

    public AsgStepReductionStrategy(OntologyProvider ontologyProvider, GraphElementSchemaProviderFactory schemaProviderFactory) {
        this.ontologyProvider = ontologyProvider;
        this.schemaProviderFactory = schemaProviderFactory;
    }

    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        //only operate on mapping related ontologies
        if (!ontologyProvider.get(query.getOnt()).isPresent())
            return;

        Ontology.Accessor accessor = new Ontology.Accessor(ontologyProvider.get(query.getOnt()).get());

    }


    private OntologyProvider ontologyProvider;
    private GraphElementSchemaProviderFactory schemaProviderFactory;


}
