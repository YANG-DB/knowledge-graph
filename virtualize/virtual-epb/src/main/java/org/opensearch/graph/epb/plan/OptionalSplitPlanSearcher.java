package org.opensearch.graph.epb.plan;

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

import org.opensearch.graph.dispatcher.epb.PlanSearcher;
import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.execution.plan.PlanOp;
import org.opensearch.graph.model.execution.plan.PlanUtil;
import org.opensearch.graph.model.execution.plan.PlanWithCost;
import org.opensearch.graph.model.execution.plan.composite.CompositePlanOp;
import org.opensearch.graph.model.execution.plan.composite.OptionalOp;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import org.opensearch.graph.model.execution.plan.entity.EntityNoOp;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import org.opensearch.graph.model.execution.plan.entity.GoToEntityOp;
import org.opensearch.graph.model.execution.plan.relation.RelationOp;
import org.opensearch.graph.model.query.optional.OptionalComp;
import javaslang.Tuple2;
import javaslang.collection.Stream;

import java.util.*;

public class OptionalSplitPlanSearcher implements PlanSearcher<Plan, PlanDetailedCost, AsgQuery> {
    //region Constructors
    public OptionalSplitPlanSearcher(
            PlanSearcher<Plan, PlanDetailedCost, AsgQuery> mainPlanSearcher,
            PlanSearcher<Plan, PlanDetailedCost, AsgQuery> optionalPlanSearcher) {
        this.mainPlanSearcher = mainPlanSearcher;
        this.optionalPlanSearcher = optionalPlanSearcher;
    }
    //endregion

    //region PlanSearcher Implementation
    @Override
    public PlanWithCost<Plan, PlanDetailedCost> search(AsgQuery query) {
        // strip plan of optionals
        AsgQueryUtil.OptionalStrippedQuery optionalStrippedQuery = AsgQueryUtil.stripOptionals(query);

        //plan main query
        PlanWithCost<Plan, PlanDetailedCost> planWithCost = this.mainPlanSearcher.search(optionalStrippedQuery.getMainQuery());

        if(planWithCost == null) {
            return null;
        }

        //plan each optional
        List<Tuple2<AsgEBase<OptionalComp>, PlanWithCost<Plan, PlanDetailedCost>>> optionalPlansWithCost =
                Stream.ofAll(optionalStrippedQuery.getOptionalQueries())
                        .map(q -> new Tuple2<>(q._1, this.optionalPlanSearcher.search(q._2)))
                        .toJavaList();

        // append optional plans
        Plan mainPlan = planWithCost.getPlan();

        for (Tuple2<AsgEBase<OptionalComp>, PlanWithCost<Plan, PlanDetailedCost>> optionalPlanWithCost : optionalPlansWithCost) {
            Plan optionalPlan = optionalPlanWithCost._2.getPlan();
            if(optionalPlan == null){
                return null;
            }
            CompositePlanOp subPlan = optionalPlan.from(PlanUtil.first$(optionalPlan, RelationOp.class));
            List<PlanOp> ops = new ArrayList<>(subPlan.getOps().size()+1);
            ops.add(new EntityNoOp(PlanUtil.first(optionalPlan, EntityOp.class).get().getAsgEbase()));
            ops.addAll(subPlan.getOps());
            OptionalOp optionalOp = new OptionalOp(optionalPlanWithCost._1 , ops);

            mainPlan = mainPlan.withOp(new GoToEntityOp(PlanUtil.first(optionalPlan, EntityOp.class).get().getAsgEbase()));
            mainPlan = mainPlan.withOp(optionalOp);

        }

        return new PlanWithCost<>(new PlanWithCost<>(mainPlan, planWithCost.getCost()));
    }
    //endregion

    //region Fields
    private PlanSearcher<Plan, PlanDetailedCost, AsgQuery> mainPlanSearcher;
    private PlanSearcher<Plan, PlanDetailedCost, AsgQuery> optionalPlanSearcher;
    //endregion

}
