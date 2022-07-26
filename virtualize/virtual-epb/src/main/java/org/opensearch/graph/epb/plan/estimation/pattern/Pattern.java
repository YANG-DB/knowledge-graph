package org.opensearch.graph.epb.plan.estimation.pattern;





import org.opensearch.graph.dispatcher.utils.PlanUtil;
import org.opensearch.graph.epb.plan.estimation.pattern.estimators.PatternCostEstimator;
import org.opensearch.graph.model.execution.plan.*;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.costs.CountEstimatesCost;
import org.opensearch.graph.model.execution.plan.costs.DoubleCost;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import org.opensearch.graph.model.execution.plan.entity.EntityFilterOp;
import org.opensearch.graph.model.execution.plan.entity.EntityJoinOp;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import org.opensearch.graph.model.execution.plan.entity.GoToEntityOp;
import org.opensearch.graph.model.execution.plan.relation.RelationFilterOp;
import org.opensearch.graph.model.execution.plan.relation.RelationOp;
import javaslang.collection.Stream;

import java.util.*;

import static org.opensearch.graph.model.asgQuery.AsgQuery.Builder.ePropGroup;

public abstract class Pattern {
    public PlanWithCost<Plan, PlanDetailedCost> buildNewPlan(PatternCostEstimator.Result<Plan, CountEstimatesCost> result,
                                                             Optional<PlanWithCost<Plan, PlanDetailedCost>> previousCost){
        DoubleCost previousPlanGlobalCost = previousPlanGlobalCost(previousCost);
        List<PlanWithCost<Plan, CountEstimatesCost>> previousPlanStepCosts = previousPlanStepCosts(previousCost);

        updatePreviousCosts(result, previousPlanStepCosts);

        List<PlanWithCost<Plan, CountEstimatesCost>> planStepCosts =
                Stream.ofAll(result.getPlanStepCosts())
                        .filter(planStepCost -> !previousCost.isPresent() ||
                                !PlanUtil.first(previousCost.get().getPlan(), planStepCost.getPlan().getOps().get(0)).isPresent())
                        .toJavaList();

        double sumOfPlanStepCosts = Stream.ofAll(planStepCosts).map(planStepCost -> planStepCost.getCost().getCost()).sum().doubleValue();
        double newCost = previousPlanGlobalCost.getCost() + sumOfPlanStepCosts;
        List<PlanWithCost<Plan, CountEstimatesCost>> newPlanStepCosts = Stream.ofAll(previousPlanStepCosts).appendAll(planStepCosts).toJavaList();

        Plan newPlan = new Plan(Stream.ofAll(newPlanStepCosts).flatMap(planStepCost -> Stream.ofAll(planStepCost.getPlan().getOps())).toJavaList());
        PlanDetailedCost newDetailedCost = new PlanDetailedCost(new DoubleCost(newCost), newPlanStepCosts);
        return new PlanWithCost<>(newPlan, newDetailedCost);
    }

    private void updatePreviousCosts(PatternCostEstimator.Result<Plan, CountEstimatesCost> result, List<PlanWithCost<Plan, CountEstimatesCost>> previousPlanStepCosts) {
        double countsUpdateFactor = result.countsUpdateFactors()[0];
        previousPlanStepCosts.forEach(planStepCost -> {
            if(planStepCost.getPlan().getOps().get(0) instanceof EntityOp) {
                planStepCost.getCost().applyCountsUpdateFactor(countsUpdateFactor);
            }
        });
    }

    private List<PlanWithCost<Plan, CountEstimatesCost>> previousPlanStepCosts(Optional<PlanWithCost<Plan, PlanDetailedCost>> previousCost) {
        if (previousCost.isPresent()) {
            return Stream.ofAll(previousCost.get().getCost().getPlanStepCosts())
                    .map(planStepCost -> {
                        try {
                            return new PlanWithCost<>(
                                    planStepCost.getPlan(),
                                    (CountEstimatesCost)planStepCost.getCost().clone());
                        } catch (CloneNotSupportedException e) {
                            return new PlanWithCost<>(
                                    planStepCost.getPlan(),
                                    new CountEstimatesCost(planStepCost.getCost().getCost(), planStepCost.getCost().getCountEstimates()));
                        }
                    })
                    .toJavaList();
        } else {
            return new ArrayList<>();
        }
    }

    private DoubleCost previousPlanGlobalCost(Optional<PlanWithCost<Plan, PlanDetailedCost>> previousCost) {
        if (previousCost.isPresent()) {
            return previousCost.get().getCost().getGlobalCost();
        }else{
            return new DoubleCost(0);
        }
    }

    public static GoToEntityRelationEntityPattern buildGoToRelationEntityPattern(Plan plan, Map<RegexPatternCostEstimator.PatternPart, PlanOp> patternParts) {
        GoToEntityOp startGoTo = (GoToEntityOp) patternParts.get(RegexPatternCostEstimator.PatternPart.GOTO_ENTITY);

        /*EntityOp start = (EntityOp) plan.getOps().stream().
                filter(op -> (op instanceof EntityOp) && ((EntityOp) op).getAsgEbase().geteBase().equals(startGoTo.getAsgEbase().geteBase())).
                findFirst().get();*/
        EntityOp start = PlanUtil.findGotoEntity(plan, startGoTo).get();

        //EntityFilterOp startFilter = (EntityFilterOp) PlanUtil.adjacentNext(plan, start).get();
        EntityFilterOp startFilter = (EntityFilterOp) PlanUtil.adjacentNext(PlanUtil.flat(plan), start).get();

        //relation
        RelationOp rel = (RelationOp) patternParts.get(RegexPatternCostEstimator.PatternPart.RELATION);

        if (!patternParts.containsKey(RegexPatternCostEstimator.PatternPart.OPTIONAL_REL_FILTER)) {
            patternParts.put(RegexPatternCostEstimator.PatternPart.OPTIONAL_REL_FILTER, new RelationFilterOp());
        }
        RelationFilterOp relFilter = (RelationFilterOp) patternParts.get(RegexPatternCostEstimator.PatternPart.OPTIONAL_REL_FILTER);
        relFilter.setRel(rel.getAsgEbase());

        //entity
        EntityOp end = (EntityOp) patternParts.get(RegexPatternCostEstimator.PatternPart.ENTITY_TWO);
        EntityFilterOp endFilter = (EntityFilterOp) patternParts.get(RegexPatternCostEstimator.PatternPart.OPTIONAL_ENTITY_TWO_FILTER);
        endFilter.setEntity(end.getAsgEbase());

        return new GoToEntityRelationEntityPattern(startGoTo, start, startFilter, rel, relFilter, end, endFilter);
    }

    public static EntityPattern buildEntityPattern(Map<RegexPatternCostEstimator.PatternPart, PlanOp> patternParts) {
        EntityOp start = (EntityOp) patternParts.get(RegexPatternCostEstimator.PatternPart.ENTITY_ONLY);
        EntityFilterOp startFilter = (EntityFilterOp) patternParts.get(RegexPatternCostEstimator.PatternPart.OPTIONAL_ENTITY_ONLY_FILTER);
        startFilter.setEntity(start.getAsgEbase());
        return new EntityPattern(start, startFilter);
    }

    public static EntityRelationEntityPattern buildEntityRelationEntityPattern(Map<RegexPatternCostEstimator.PatternPart, PlanOp> patternParts) {
        //entity one
        EntityOp start = (EntityOp) patternParts.get(RegexPatternCostEstimator.PatternPart.ENTITY_ONE);
        EntityFilterOp startFilter = (EntityFilterOp) patternParts.get(RegexPatternCostEstimator.PatternPart.OPTIONAL_ENTITY_ONE_FILTER);
        startFilter.setEntity(start.getAsgEbase());

        //relation
        RelationOp rel = (RelationOp) patternParts.get(RegexPatternCostEstimator.PatternPart.RELATION);
        RelationFilterOp relFilter = (RelationFilterOp) patternParts.get(RegexPatternCostEstimator.PatternPart.OPTIONAL_REL_FILTER);
        relFilter.setRel(rel.getAsgEbase());

        //entity
        EntityOp end = (EntityOp) patternParts.get(RegexPatternCostEstimator.PatternPart.ENTITY_TWO);
        EntityFilterOp endFilter = (EntityFilterOp) patternParts.get(RegexPatternCostEstimator.PatternPart.OPTIONAL_ENTITY_TWO_FILTER);
        endFilter.setEntity(end.getAsgEbase());

        return new EntityRelationEntityPattern(start, startFilter, rel, relFilter, end, endFilter);
    }

    public static EntityJoinPattern buildEntityJoinPattern(Map<RegexPatternCostEstimator.PatternPart, PlanOp> patternParts) {
        EntityJoinOp entityJoinOp = (EntityJoinOp) patternParts.get(RegexPatternCostEstimator.PatternPart.JOIN);
        return new EntityJoinPattern(entityJoinOp);
    }

    public static Pattern buildEntityJoinEntityPattern(Map<RegexPatternCostEstimator.PatternPart, PlanOp> patternParts) {
        EntityJoinOp entityJoinOp = (EntityJoinOp) patternParts.get(RegexPatternCostEstimator.PatternPart.JOIN);

        //relation
        RelationOp rel = (RelationOp) patternParts.get(RegexPatternCostEstimator.PatternPart.RELATION);
        RelationFilterOp relFilter = (RelationFilterOp) patternParts.get(RegexPatternCostEstimator.PatternPart.OPTIONAL_REL_FILTER);
        relFilter.setRel(rel.getAsgEbase());

        //entity
        EntityOp end = (EntityOp) patternParts.get(RegexPatternCostEstimator.PatternPart.ENTITY_TWO);
        EntityFilterOp endFilter = (EntityFilterOp) patternParts.get(RegexPatternCostEstimator.PatternPart.OPTIONAL_ENTITY_TWO_FILTER);
        endFilter.setEntity(end.getAsgEbase());

        return new EntityJoinEntityPattern(entityJoinOp, rel, relFilter, end, endFilter);
    }

    public static GotoPattern buildGotoPattern(Plan plan, Map<RegexPatternCostEstimator.PatternPart, PlanOp> patternParts){
        GoToEntityOp goToEntityOp = (GoToEntityOp) patternParts.get(RegexPatternCostEstimator.PatternPart.GOTO_ENTITY);

        EntityOp entityOp = PlanUtil.findGotoEntity(plan, goToEntityOp).get();

        return new GotoPattern(goToEntityOp, entityOp);
    }

    public static Pattern buildPattern(RegexPatternCostEstimator.Pattern regexPattern, Map<RegexPatternCostEstimator.PatternPart, PlanOp> patternParts , Plan plan ){
        return regexPattern.equals(RegexPatternCostEstimator.Pattern.ENTITY) ?  buildEntityPattern(patternParts) :
            regexPattern.equals(RegexPatternCostEstimator.Pattern.ENTITY_RELATION_ENTITY) ? buildEntityRelationEntityPattern(patternParts) :
            regexPattern.equals(RegexPatternCostEstimator.Pattern.GOTO_ENTITY_RELATION_ENTITY) ? buildGoToRelationEntityPattern(plan, patternParts) :
            regexPattern.equals(RegexPatternCostEstimator.Pattern.ENTITY_JOIN) ? buildEntityJoinPattern(patternParts):
            regexPattern.equals(RegexPatternCostEstimator.Pattern.ENTITY_JOIN_RELATION_ENTITY) ? buildEntityJoinEntityPattern(patternParts ) :
            regexPattern.equals(RegexPatternCostEstimator.Pattern.GOTO)? buildGotoPattern(plan, patternParts): null;
    }
}
