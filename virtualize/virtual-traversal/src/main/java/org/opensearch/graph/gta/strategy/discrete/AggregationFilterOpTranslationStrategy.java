package org.opensearch.graph.gta.strategy.discrete;

/*-
 * #%L
 * virtual-traversal
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





import org.opensearch.graph.dispatcher.gta.TranslationContext;
import org.opensearch.graph.dispatcher.utils.PlanUtil;
import org.opensearch.graph.gta.strategy.PlanOpTranslationStrategyBase;
import org.opensearch.graph.gta.strategy.common.EntityTranslationOptions;
import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.execution.plan.PlanOp;
import org.opensearch.graph.model.execution.plan.PlanWithCost;
import org.opensearch.graph.model.execution.plan.composite.CountOp;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import org.opensearch.graph.model.execution.plan.entity.EntityFilterOp;
import org.opensearch.graph.model.execution.plan.entity.EntityGroupByFilterOp;
import org.opensearch.graph.model.execution.plan.entity.EntityGroupByOp;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import org.opensearch.graph.model.execution.plan.relation.RelationFilterOp;
import org.opensearch.graph.model.execution.plan.relation.RelationOp;
import org.opensearch.graph.model.query.aggregation.Agg;
import org.opensearch.graph.model.query.properties.EProp;
import org.opensearch.graph.model.query.properties.EPropGroup;
import org.opensearch.graph.model.query.properties.RelProp;
import org.opensearch.graph.model.query.properties.RelPropGroup;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

import java.util.Optional;

public class AggregationFilterOpTranslationStrategy extends PlanOpTranslationStrategyBase {
    //region Constructors
    public AggregationFilterOpTranslationStrategy( EntityTranslationOptions entityTranslationOptions) {
        super(planOp -> planOp.getClass().equals(CountOp.class));
        this.filterOpEntityTranslationStrategy = new EntityFilterOpTranslationStrategy(entityTranslationOptions);
        this.filterOpRelationTranslationStrategy = new RelationFilterOpTranslationStrategy();
    }
    //endregion

    //region PlanOpTranslationStrategyBase Implementation
    @Override
    protected GraphTraversal<?, ?> translateImpl(GraphTraversal traversal, PlanWithCost<Plan, PlanDetailedCost> planWithCost, PlanOp planOp, TranslationContext context) {
        Optional<PlanOp> previousPlanOp = PlanUtil.adjacentPrev(planWithCost.getPlan(), planOp);
        if (!previousPlanOp.isPresent()) {
            return traversal;
        }
        EntityGroupByFilterOp groupByOp = (EntityGroupByFilterOp) planOp;
        Agg agg = groupByOp.getAsgEbase().geteBase();
        if(previousPlanOp.get() instanceof EntityOp) {
            //activate the entity filter group by translator
            EPropGroup group = new EPropGroup();
            String pType = agg.getATag();//this must be a property to do groupBy on...
            group.addIsNoneExist(new EProp(agg.geteNum(), pType,agg.getCon()));
            AsgEBase<EPropGroup> groupAsgEBase = new AsgEBase<>(group);
            filterOpEntityTranslationStrategy.translateImpl(traversal,planWithCost,new EntityFilterOp(groupAsgEBase),context);
        }
        if(previousPlanOp.get() instanceof RelationOp) {
            //activate the relation filter group by translator
            RelPropGroup group = new RelPropGroup();
            String pType = agg.getATag();//this must be a property to do groupBy on...
            group.addIsNoneExist(new RelProp(agg.geteNum(), pType,agg.getCon()));
            AsgEBase<RelPropGroup> groupAsgEBase = new AsgEBase<>(group);
            filterOpRelationTranslationStrategy.translateImpl(traversal,planWithCost,new RelationFilterOp(groupAsgEBase),context);

        }
        return traversal;
    }
    //endregion

    //region Fields
    private EntityFilterOpTranslationStrategy filterOpEntityTranslationStrategy;
    private RelationFilterOpTranslationStrategy filterOpRelationTranslationStrategy;
    //endregion
}
