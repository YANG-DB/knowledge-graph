package org.opensearch.graph.epb.utils;

import org.opensearch.graph.dispatcher.ontology.OntologyProvider;
import org.opensearch.graph.epb.plan.estimation.CostEstimationConfig;
import org.opensearch.graph.epb.plan.estimation.IncrementalEstimationContext;
import org.opensearch.graph.epb.plan.estimation.pattern.estimators.PatternCostEstimator;
import org.opensearch.graph.epb.plan.estimation.pattern.estimators.rule.RulesBasedPatternCostEstimator;
import org.opensearch.graph.epb.plan.statistics.RuleBasedStatisticalProvider;
import org.opensearch.graph.epb.plan.statistics.Statistics;
import org.opensearch.graph.epb.plan.statistics.StatisticsProvider;
import org.opensearch.graph.model.GlobalConstants;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.Direction;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.costs.CountEstimatesCost;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.query.EBase;
import org.opensearch.graph.model.query.Rel;
import org.opensearch.graph.model.query.entity.EConcrete;
import org.opensearch.graph.model.query.entity.EEntityBase;
import org.opensearch.graph.model.query.entity.ETyped;
import org.opensearch.graph.model.query.entity.EUntyped;
import org.opensearch.graph.model.query.properties.EPropGroup;
import org.opensearch.graph.model.query.properties.RelPropGroup;
import org.opensearch.graph.unipop.schema.providers.*;
import org.opensearch.graph.unipop.schema.providers.indexPartitions.StaticIndexPartitions;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.opensearch.graph.model.schema.BaseTypeElement.*;

/**
 * Created by lior.perry on 2/20/2018.
 */
public interface DfsTestUtils {
    static PatternCostEstimator<Plan, CountEstimatesCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>> ruleBaseEstimator(Ontology.Accessor ont) {
        return new RulesBasedPatternCostEstimator(
                new CostEstimationConfig(1, 1),
                (ontology) -> statsProvider(),
                new OntologyProvider() {
                    @Override
                    public Ontology add(Ontology ontology) {
                        return ontology;
                    }
                    @Override
                    public Optional<Ontology> get(String id) {
                        return Optional.of(ont.get());
                    }

                    @Override
                    public Collection<Ontology> getAll() {
                        return Collections.singleton(ont.get());
                    }


                });
    }

    static StatisticsProvider statsProvider() {
        return new RuleBasedStatisticalProvider() {
            @Override
            public Statistics.SummaryStatistics getNodeStatistics(EEntityBase item) {
                if (item instanceof EConcrete)
                    return new Statistics.SummaryStatistics(1, 1);
                if (item instanceof ETyped)
                    return new Statistics.SummaryStatistics(100, 100);
                if (item instanceof EUntyped)
                    return new Statistics.SummaryStatistics(1000, 1000);

                return new Statistics.SummaryStatistics(1, 1);
            }

            @Override
            public Statistics.SummaryStatistics getNodeFilterStatistics(EEntityBase item, EPropGroup entityFilter) {
                if (entityFilter == null)
                    return new Statistics.SummaryStatistics(Integer.MAX_VALUE, Integer.MAX_VALUE);
                //reduce estimation due to filter existance
                if (item instanceof EConcrete)
                    return new Statistics.SummaryStatistics(1, 1);
                if (item instanceof ETyped)
                    return new Statistics.SummaryStatistics(80, 80);
                if (item instanceof EUntyped)
                    return new Statistics.SummaryStatistics(800, 800);

                //default
                return new Statistics.SummaryStatistics(1, 1);
            }

            @Override
            public Statistics.SummaryStatistics getEdgeStatistics(Rel item, EEntityBase source) {
                return new Statistics.SummaryStatistics(1, 1);
            }

            @Override
            public Statistics.SummaryStatistics getEdgeFilterStatistics(Rel item, RelPropGroup entityFilter, EEntityBase source) {
                return new Statistics.SummaryStatistics(1, 1);
            }

            @Override
            public Statistics.SummaryStatistics getRedundantNodeStatistics(EEntityBase entity, RelPropGroup relPropGroup) {
                return new Statistics.SummaryStatistics(1, 1);
            }

            @Override
            public long getGlobalSelectivity(Rel rel, RelPropGroup filter, EBase entity, Direction direction) {
                return 1;
            }
        };
    }

    static GraphElementSchemaProvider buildSchemaProvider(Ontology.Accessor ont) {
        Iterable<GraphVertexSchema> vertexSchemas =
                Stream.ofAll(ont.entities())
                        .map(entity -> (GraphVertexSchema) new GraphVertexSchema.Impl(
                                Type.of(entity.geteType()),
                                new StaticIndexPartitions(Collections.singletonList("index"))))
                        .toJavaList();

        Iterable<GraphEdgeSchema> edgeSchemas =
                Stream.ofAll(ont.relations())
                        .map(relation -> (GraphEdgeSchema) new GraphEdgeSchema.Impl(
                                Type.of(relation.getrType()),
                                new GraphElementConstraint.Impl(__.has(T.label, relation.getrType())),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(GlobalConstants.EdgeSchema.SOURCE_ID),
                                        Optional.of(relation.getePairs().get(0).geteTypeA()))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(GlobalConstants.EdgeSchema.DEST_ID),
                                        Optional.of(relation.getePairs().get(0).geteTypeB()),
                                        Arrays.asList(
                                                new GraphRedundantPropertySchema.Impl("firstName", "entityB.firstName", ont.pName$("firstName").getType()),
                                                new GraphRedundantPropertySchema.Impl("gender", "entityB.gender", ont.pName$("gender").getType()),
                                                new GraphRedundantPropertySchema.Impl("id", GlobalConstants.EdgeSchema.DEST_ID, ont.pName$("firstName").getType()),
                                                new GraphRedundantPropertySchema.Impl("type", GlobalConstants.EdgeSchema.DEST_TYPE, ont.pName$("type").getType())
                                        ))),
                                org.apache.tinkerpop.gremlin.structure.Direction.OUT,
                                Optional.of(new GraphEdgeSchema.DirectionSchema.Impl(GlobalConstants.EdgeSchema.DIRECTION, "out", "in")),
                                Optional.empty(),
                                Optional.of(new StaticIndexPartitions(Collections.singletonList("index"))),
                                Collections.emptyList()))
                        .toJavaList();

        return new OntologySchemaProvider(ont.get(), new GraphElementSchemaProvider.Impl(vertexSchemas, edgeSchemas));
    }

}
