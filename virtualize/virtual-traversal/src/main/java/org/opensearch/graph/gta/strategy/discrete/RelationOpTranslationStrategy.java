package org.opensearch.graph.gta.strategy.discrete;


import org.opensearch.graph.dispatcher.utils.PlanUtil;
import org.opensearch.graph.gta.strategy.PlanOpTranslationStrategyBase;
import org.opensearch.graph.gta.strategy.utils.ConversionUtil;
import org.opensearch.graph.dispatcher.gta.TranslationContext;
import org.opensearch.graph.model.execution.plan.*;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import org.opensearch.graph.model.execution.plan.relation.RelationOp;
import org.opensearch.graph.model.query.Rel;
import org.opensearch.graph.model.query.entity.EEntityBase;
import org.opensearch.graph.model.GlobalConstants;
import org.opensearch.graph.unipop.promise.Constraint;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.opensearch.graph.unipop.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.Optional;

public class RelationOpTranslationStrategy extends PlanOpTranslationStrategyBase {
    //region Constructors
    public RelationOpTranslationStrategy() {
        super(planOp -> planOp.getClass().equals(RelationOp.class));
    }
    //endregion

    //region PlanOpTranslationStrategy Implementation
    @Override
    protected GraphTraversal translateImpl(GraphTraversal traversal, PlanWithCost<Plan, PlanDetailedCost> plan, PlanOp planOp, TranslationContext context) {

        Optional<EntityOp> prev = PlanUtil.prev(plan.getPlan(), planOp, EntityOp.class);
        Optional<EntityOp> next = PlanUtil.next(plan.getPlan(), planOp, EntityOp.class);

        Rel rel = ((RelationOp)planOp).getAsgEbase().geteBase();
        String rTypeName = context.getOnt().$relation$(rel.getrType()).getName();

        if(prev.isPresent()) {

            switch (rel.getDir()) {
                case R:
                    traversal.outE();
                    break;
                case L:
                    traversal.inE();
                    break;
                case RL:
                    traversal.bothE();
                    break;
            }
        }else{
            traversal = context.getGraphTraversalSource().E();
        }
        String label;
        if(next.isPresent()) {
            label = createLabelForRelation(prev.get().getAsgEbase().geteBase(), rel.getDir(), next.get().getAsgEbase().geteBase());
        }else{
            label = prev.get().getAsgEbase().geteBase().geteTag() + ConversionUtil.convertDirectionGraphic(rel.getDir()) + rTypeName;
            if(next.isPresent()){
                label += next.get().getAsgEbase().geteBase().geteTag();
            }
        }

        return traversal.as(label)
                .has(GlobalConstants.HasKeys.CONSTRAINT, Constraint.by(__.start().has(T.label, P.eq(rTypeName))));

    }
    //endregion

    //region Private Methods
    private String createLabelForRelation(EEntityBase prev, Rel.Direction direction, EEntityBase next) {
        return prev.geteTag() + ConversionUtil.convertDirectionGraphic(direction) + next.geteTag();
    }
    //endregion
}
