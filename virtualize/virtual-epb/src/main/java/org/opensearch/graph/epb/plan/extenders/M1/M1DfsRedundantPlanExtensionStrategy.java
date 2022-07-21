package org.opensearch.graph.epb.plan.extenders.M1;


import com.google.inject.Inject;
import org.opensearch.graph.dispatcher.ontology.OntologyProvider;
import org.opensearch.graph.dispatcher.epb.PlanExtensionStrategy;
import org.opensearch.graph.epb.plan.extenders.*;
import org.opensearch.graph.executor.ontology.GraphElementSchemaProviderFactory;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import javaslang.collection.Stream;

public class M1DfsRedundantPlanExtensionStrategy extends CompositePlanExtensionStrategy<Plan, AsgQuery> {
    //region Constructors
    @Inject
    public M1DfsRedundantPlanExtensionStrategy(
            OntologyProvider ontologyProvider,
            GraphElementSchemaProviderFactory schemaProviderFactory) {
        super();

        this.innerExtenders = Stream.<PlanExtensionStrategy<Plan, AsgQuery>>of(
                        new ChainPlanExtensionStrategy<>(
                                new CompositePlanExtensionStrategy<>(
                                        new InitialPlanGeneratorExtensionStrategy(),
                                        new StepAdjacentDfsStrategy(),
                                        new OptionalOpExtensionStrategy(this)
                                ),
                                new OptionalInitialExtensionStrategy(),
                                new RedundantFilterPlanExtensionStrategy(
                                        ontologyProvider,
                                        schemaProviderFactory),
                                new RedundantSelectionFilterPlanExtensionStrategy(
                                        ontologyProvider,
                                        schemaProviderFactory)
                        )
        ).toJavaList();
    }
    //endregion
}
