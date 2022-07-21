package org.opensearch.graph.epb.plan.extenders;


import org.opensearch.graph.dispatcher.epb.PlanExtensionStrategy;
import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.*;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import org.opensearch.graph.model.execution.plan.relation.RelationFilterOp;
import org.opensearch.graph.model.execution.plan.relation.RelationOp;
import org.opensearch.graph.model.query.EBase;
import org.opensearch.graph.model.query.Rel;
import org.opensearch.graph.model.query.entity.EEntityBase;
import org.opensearch.graph.model.query.properties.RelProp;
import javaslang.Tuple2;

import java.util.*;

public class AllDirectionsPlanExtensionStrategy implements PlanExtensionStrategy<Plan, AsgQuery> {
    public AllDirectionsPlanExtensionStrategy() {}

    @Override
    public Iterable<Plan> extendPlan(Optional<Plan> plan, AsgQuery query) {
        List<Plan> plans = new LinkedList<>();
        if(plan.isPresent()){
            Map<Integer, AsgEBase> queryParts = SimpleExtenderUtils.flattenQuery(query);

            Tuple2<List<AsgEBase>, Map<Integer, AsgEBase>> partsTuple = SimpleExtenderUtils.removeHandledQueryParts(plan.get(), queryParts);
            List<AsgEBase> handledParts = partsTuple._1();
            Map<Integer, AsgEBase> remainingQueryParts = partsTuple._2();

            // If we have query parts that need further handling
            if(remainingQueryParts.size() > 0){
                for(AsgEBase handledPart : handledParts){
                    plans.addAll(extendPart(handledPart, remainingQueryParts, plan.get()));
                }
            }
        }
/*
        for(Plan<C> newPlan : plans){
            newPlan.setPlanComplete(SimpleExtenderUtils.checkIfPlanIsComplete(newPlan, query));
        }
*/

        return plans;
    }

    private Collection<Plan> extendPart(AsgEBase<? extends EBase> handledPartToExtend, Map<Integer, AsgEBase> queryPartsNotHandled, Plan originalPlan) {
        List<Plan> plans = new ArrayList<>();
        if(((AsgEBase) handledPartToExtend).getNext() != null){
            for(AsgEBase<? extends EBase> next : handledPartToExtend.getNext()){
                if(SimpleExtenderUtils.shouldAddElement(next) && queryPartsNotHandled.containsKey(next.geteNum())){
                    PlanOp op = createOpForElement(next);
                    plans.add(new Plan(originalPlan.getOps()).withOp(op));
                }
            }
        }

        if(SimpleExtenderUtils.shouldAdvanceToParents(handledPartToExtend)){
            for(AsgEBase<? extends  EBase> parent : handledPartToExtend.getParents()){
                if(SimpleExtenderUtils.shouldAddElement(parent) && queryPartsNotHandled.containsKey(parent.geteNum())){
                    PlanOp op = createOpForElement(parent, true);
                    plans.add(new Plan(originalPlan.getOps()).withOp(op));
                    /*Plan<C> newPlan = Plan.PlanBuilder.search(originalPlan.getPlanOps())
                            .operation(new PlanOpWithCost<C>(op,costEstimator.estimateCost(originalPlan,op)))
                            .estimation(costEstimator)
                            .compose();*/
                }
            }
        }
        return plans;
    }

    private PlanOp createOpForElement(AsgEBase element) {
        return createOpForElement(element, false);
    }

    private PlanOp createOpForElement(AsgEBase element, boolean reverseDirection) {
        if(element.geteBase() instanceof EEntityBase){
            EntityOp op = new EntityOp(element);
            return op;
        }
        if(element.geteBase() instanceof Rel){
            AsgEBase<Rel> rel = element;
            if(reverseDirection){
                Rel rel1 = rel.geteBase();
                Rel rel2 = rel1.clone();
                if(rel1.getDir().equals(Rel.Direction.L)){
                    rel2.setDir(Rel.Direction.R);
                }else if(rel1.getDir().equals(Rel.Direction.R)){
                    rel2.setDir(Rel.Direction.L);
                }else{
                    rel2.setDir(rel1.getDir());
                }
                rel = AsgEBase.Builder.<Rel>get().withEBase(rel2).build();
            }
            RelationOp op = new RelationOp(rel);
            return op;
        }
        if(element.geteBase() instanceof RelProp){
            RelationFilterOp op = new RelationFilterOp(element);
            return op;
        }
        throw new UnsupportedOperationException();
    }




}
