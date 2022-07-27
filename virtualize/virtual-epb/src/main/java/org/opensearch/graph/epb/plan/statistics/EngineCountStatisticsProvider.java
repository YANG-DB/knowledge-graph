package org.opensearch.graph.epb.plan.statistics;

/*-
 * #%L
 * virtual-epb
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





import com.google.inject.Provider;
import org.opensearch.graph.dispatcher.gta.PlanTraversalTranslator;
import org.opensearch.graph.dispatcher.gta.TranslationContext;
import org.opensearch.graph.epb.plan.statistics.configuration.EngineCountStatisticsConfig;
import org.opensearch.graph.executor.ontology.UniGraphProvider;
import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.execution.plan.Direction;
import org.opensearch.graph.model.execution.plan.PlanWithCost;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.costs.CountEstimatesCost;
import org.opensearch.graph.model.execution.plan.costs.DoubleCost;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import org.opensearch.graph.model.execution.plan.entity.EntityFilterOp;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import org.opensearch.graph.model.execution.plan.relation.RelationFilterOp;
import org.opensearch.graph.model.execution.plan.relation.RelationOp;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.query.EBase;
import org.opensearch.graph.model.query.Rel;
import org.opensearch.graph.model.query.entity.EEntityBase;
import org.opensearch.graph.model.query.entity.ETyped;
import org.opensearch.graph.model.query.properties.EProp;
import org.opensearch.graph.model.query.properties.EPropGroup;
import org.opensearch.graph.model.query.properties.RedundantRelProp;
import org.opensearch.graph.model.query.properties.RelPropGroup;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class EngineCountStatisticsProvider implements StatisticsProvider  {

    public EngineCountStatisticsProvider(PlanTraversalTranslator planTraversalTranslator, Ontology ontology, Provider<UniGraphProvider> uniGraphProvider, EngineCountStatisticsConfig engineCountStatisticsConfig) {
        this.planTraversalTranslator = planTraversalTranslator;
        this.ontology = ontology;
        this.uniGraphProvider = uniGraphProvider;
        this.engineCountStatisticsConfig = engineCountStatisticsConfig;
    }

    @Override
    public Statistics.SummaryStatistics getNodeStatistics(EEntityBase item) {
        Plan plan = new Plan(new EntityOp(new AsgEBase<>(item)));
        return getSummaryStatistics(plan);
    }

    private Statistics.SummaryStatistics getSummaryStatistics(Plan plan) {
        GraphTraversal<?, ?> traversal;
        try {
             traversal = planTraversalTranslator.translate(new PlanWithCost<>(plan, new PlanDetailedCost(new DoubleCost(1), Collections.singleton(new PlanWithCost<>(plan, new CountEstimatesCost(1, 1)))))
                    , new TranslationContext(new Ontology.Accessor(ontology), uniGraphProvider.get().getGraph(ontology).traversal()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        long count = traversal.count().next();

        return new Statistics.SummaryStatistics(count, count);
    }

    @Override
    public Statistics.SummaryStatistics getNodeFilterStatistics(EEntityBase item, EPropGroup entityFilter) {
        Plan plan = new Plan(new EntityOp(new AsgEBase<>(item)), new EntityFilterOp(new AsgEBase<>(entityFilter)));
        return getSummaryStatistics(plan);
    }

    @Override
    public Statistics.SummaryStatistics getEdgeStatistics(Rel item, EEntityBase source) {
        Plan plan = new Plan(new EntityOp(new AsgEBase<>(source)), new RelationOp(new AsgEBase<>(item)));
        return getSummaryStatistics(plan);
    }

    @Override
    public Statistics.SummaryStatistics getEdgeFilterStatistics(Rel item, RelPropGroup entityFilter, EEntityBase source) {
        Plan plan = new Plan(new EntityOp(new AsgEBase<>(source)), new RelationOp(new AsgEBase<>(item)), new RelationFilterOp(new AsgEBase<>(entityFilter)));
        return getSummaryStatistics(plan);
    }

    @Override
    public Statistics.SummaryStatistics getRedundantNodeStatistics(EEntityBase entity, RelPropGroup relPropGroup) {
        List<RedundantRelProp> pushdownProps = relPropGroup.getProps().stream().filter(prop -> prop instanceof RedundantRelProp).
                map(RedundantRelProp.class::cast).collect(Collectors.toList());

        EPropGroup ePropGroup = new EPropGroup(pushdownProps.stream().map(prop -> EProp.of(prop.geteNum(), prop.getpType(), prop.getCon())).collect(Collectors.toList()));
        return getNodeFilterStatistics(entity, ePropGroup);
    }

    @Override
    public long getGlobalSelectivity(Rel rel, RelPropGroup filter, EBase entity, Direction direction) {
        if(entity instanceof ETyped) {
            ETyped eTyped = (ETyped) entity;
            return engineCountStatisticsConfig.getRelationSelectivity(rel.getrType(), eTyped.geteType(), direction);
        }
        return engineCountStatisticsConfig.getRelationSelectivity(rel.getrType(), "",direction);
    }

    private PlanTraversalTranslator planTraversalTranslator;
    private Ontology ontology;
    private Provider<UniGraphProvider> uniGraphProvider;
    private EngineCountStatisticsConfig engineCountStatisticsConfig;


}
