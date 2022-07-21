package org.opensearch.graph.asg.strategy;


import com.google.inject.Inject;
import org.opensearch.graph.asg.strategy.schema.ExactConstraintTransformationAsgStrategy;
import org.opensearch.graph.asg.strategy.schema.LikeAnyConstraintTransformationAsgStrategy;
import org.opensearch.graph.asg.strategy.schema.LikeConstraintTransformationAsgStrategy;
import org.opensearch.graph.dispatcher.ontology.OntologyProvider;
import org.opensearch.graph.executor.ontology.GraphElementSchemaProviderFactory;
import org.openserach.graph.asg.strategy.AsgNamedParametersStrategy;
import org.openserach.graph.asg.strategy.AsgStrategy;
import org.openserach.graph.asg.strategy.AsgStrategyRegistrar;
import org.openserach.graph.asg.strategy.constraint.*;
import org.openserach.graph.asg.strategy.propertyGrouping.EPropGroupingAsgStrategy;
import org.openserach.graph.asg.strategy.propertyGrouping.HQuantPropertiesGroupingAsgStrategy;
import org.openserach.graph.asg.strategy.propertyGrouping.Quant1PropertiesGroupingAsgStrategy;
import org.openserach.graph.asg.strategy.propertyGrouping.RelPropGroupingAsgStrategy;
import org.openserach.graph.asg.strategy.selection.DefaultETagAsgStrategy;
import org.openserach.graph.asg.strategy.selection.DefaultSelectionAsgStrategy;
import org.openserach.graph.asg.strategy.type.RelationPatternRangeAsgStrategy;
import org.openserach.graph.asg.strategy.type.UntypedInferTypeLeftSideRelationAsgStrategy;
import org.openserach.graph.asg.strategy.type.UntypedRelationInferTypeAsgStrategy;

import java.util.Arrays;

public class M2AsgStrategyRegistrar  implements AsgStrategyRegistrar {
    //region Constructors
    @Inject
    public M2AsgStrategyRegistrar(
            OntologyProvider ontologyProvider,
            GraphElementSchemaProviderFactory schemaProviderFactory) {
        this.ontologyProvider = ontologyProvider;
        this.schemaProviderFactory = schemaProviderFactory;
    }
    //endregion

    //region AsgStrategyRegistrar Implementation
    @Override
    public Iterable<AsgStrategy> register() {
        return Arrays.asList(
                new DefaultETagAsgStrategy(this.ontologyProvider),
                new AsgNamedParametersStrategy(),
                new UntypedInferTypeLeftSideRelationAsgStrategy(),
                new RelationPatternRangeAsgStrategy(),
                new UntypedRelationInferTypeAsgStrategy(),
                new EPropGroupingAsgStrategy(),
                new HQuantPropertiesGroupingAsgStrategy(),
                new Quant1PropertiesGroupingAsgStrategy(),
                new RelPropGroupingAsgStrategy(),
                new ConstraintTypeTransformationAsgStrategy(),
                new ConstraintIterableTransformationAsgStrategy(),
                new RedundantLikeConstraintAsgStrategy(),
                new RedundantLikeAnyConstraintAsgStrategy(),
                new AggFilterTransformationAsgStrategy(),
                new LikeToEqTransformationAsgStrategy(),
                new ExactConstraintTransformationAsgStrategy(this.ontologyProvider, this.schemaProviderFactory),
                new LikeConstraintTransformationAsgStrategy(this.ontologyProvider, this.schemaProviderFactory),
                new LikeAnyConstraintTransformationAsgStrategy(this.ontologyProvider, this.schemaProviderFactory),
                new RedundantInSetConstraintAsgStrategy(),
                new RedundantInRangeConstraintAsgStrategy(),
                new RedundantPropGroupAsgStrategy(),
                new DefaultSelectionAsgStrategy(this.ontologyProvider)
        );
    }
    //endregion

    //region Fields
    private OntologyProvider ontologyProvider;
    private GraphElementSchemaProviderFactory schemaProviderFactory;
    //endregion
}
