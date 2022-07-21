package org.opensearch.graph.gta.strategy.promise;


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
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.Optional;

/**
 * Created by benishue on 08-Mar-17.
 *
 *
 *
 * relOp = outE('promise').has('constraint', P.eq(Constraint.by(__.and(
 *                  __.has('label', P.eq(<Ontology(<rel.rType>)>)),
 *                  __.has('direction', P.eq(TinkerPop.direction(<Rel.dir>)))))))
 *                  .as(source(<EEntityBase.Etag>)-->target(<EEntityBase.Etag></EEntityBase.Etag>))  // A-->B
 *
 */
public class RelationOpTranslationStrategy extends PlanOpTranslationStrategyBase {
    //region Constructors
    public RelationOpTranslationStrategy() {
        super(RelationOp.class);
    }
    //endregion

    //region PlanOpTranslationStrategy Implementation
    @Override
    protected GraphTraversal translateImpl(GraphTraversal traversal, PlanWithCost<Plan, PlanDetailedCost> plan, PlanOp planOp, TranslationContext context) {
        Optional<EntityOp> prev = PlanUtil.prev(plan.getPlan(), planOp, EntityOp.class);
        Optional<EntityOp> next = PlanUtil.next(plan.getPlan(), planOp, EntityOp.class);

        Rel rel = ((RelationOp)planOp).getAsgEbase().geteBase();
        String rTypeName = context.getOnt().$relation$(rel.getrType()).getName();
        return traversal.outE(GlobalConstants.Labels.PROMISE)
                .as(createLabelForRelation(prev.get().getAsgEbase().geteBase(), next.get().getAsgEbase().geteBase()))
                .has(GlobalConstants.HasKeys.CONSTRAINT,
                        Constraint.by(__.and(
                                __.has(T.label, P.eq(rTypeName)),
                                __.has(GlobalConstants.HasKeys.DIRECTION, ConversionUtil.convertDirection(rel.getDir())))));

    }
    //endregion

    //region Private Methods
    private String createLabelForRelation(EEntityBase prev, EEntityBase next) {
        return prev.geteTag() + "-->" + next.geteTag();
    }
    //endregion
}
