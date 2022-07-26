package org.opensearch.graph.epb.plan.estimation.pattern;





import org.opensearch.graph.epb.plan.estimation.pattern.estimators.PatternCostEstimator;
import org.opensearch.graph.model.execution.plan.PlanWithCost;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.costs.CountEstimatesCost;
import org.opensearch.graph.model.execution.plan.costs.DoubleCost;
import org.opensearch.graph.model.execution.plan.costs.JoinCost;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import org.opensearch.graph.model.execution.plan.entity.EntityJoinOp;

import java.util.Arrays;
import java.util.Optional;

public class EntityJoinPattern extends Pattern {
    //region Constructors
    public EntityJoinPattern(EntityJoinOp entityJoinOp) {
        this.entityJoinOp = entityJoinOp;
    }
    //endregion

    @Override
    public PlanWithCost<Plan, PlanDetailedCost> buildNewPlan(PatternCostEstimator.Result<Plan, CountEstimatesCost> result, Optional<PlanWithCost<Plan, PlanDetailedCost>> previousCost) {
        if(previousCost.isPresent()){
            Plan joinPlan = result.getPlanStepCosts().get(0).getPlan();
            JoinCost joinCost = (JoinCost) result.getPlanStepCosts().get(0).getCost();
            if( result.countsUpdateFactors()!= null){
                joinCost.applyCountsUpdateFactorOnLeftBranch(result.countsUpdateFactors()[0]);
                joinCost.applyCountsUpdateFactorOnRightBranch(result.countsUpdateFactors()[1]);
            }
            PlanDetailedCost planDetailedCost = new PlanDetailedCost(new DoubleCost(joinCost.getCost() + joinCost.getLeftBranchCost().getGlobalCost().cost + joinCost.getRightBranchCost().getGlobalCost().cost),
                    Arrays.asList(new PlanWithCost<>(joinPlan, joinCost)));
            return new PlanWithCost<>( joinPlan, planDetailedCost);
        }else{
            return super.buildNewPlan(result, previousCost);
        }
    }

    //region Properties

    public EntityJoinOp getEntityJoinOp() {
        return entityJoinOp;
    }

    //endregion

    //region Fields
    private EntityJoinOp entityJoinOp;

    //endregion
}
