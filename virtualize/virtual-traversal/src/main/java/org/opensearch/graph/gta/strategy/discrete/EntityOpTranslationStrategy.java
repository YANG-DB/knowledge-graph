package org.opensearch.graph.gta.strategy.discrete;


import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.T;
import org.opensearch.graph.dispatcher.gta.TranslationContext;
import org.opensearch.graph.dispatcher.utils.PlanUtil;
import org.opensearch.graph.gta.strategy.PlanOpTranslationStrategyBase;
import org.opensearch.graph.gta.strategy.common.EntityTranslationOptions;
import org.opensearch.graph.gta.strategy.utils.EntityTranslationUtil;
import org.opensearch.graph.model.GlobalConstants;
import org.opensearch.graph.model.execution.plan.PlanOp;
import org.opensearch.graph.model.execution.plan.PlanWithCost;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import org.opensearch.graph.model.execution.plan.relation.RelationFilterOp;
import org.opensearch.graph.model.execution.plan.relation.RelationOp;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.query.entity.EConcrete;
import org.opensearch.graph.model.query.entity.EEntityBase;
import org.opensearch.graph.model.query.entity.ETyped;
import org.opensearch.graph.model.query.entity.EUntyped;
import org.opensearch.graph.unipop.process.traversal.dsl.graph.__;
import org.opensearch.graph.unipop.promise.Constraint;

import java.util.List;
import java.util.Optional;

/**
 * Created by Roman on 10/05/2017.
 */
public class EntityOpTranslationStrategy extends PlanOpTranslationStrategyBase {
    //region Constructors
    public EntityOpTranslationStrategy(EntityTranslationOptions options) {
        super(planOp -> planOp.getClass().equals(EntityOp.class));
        this.options = options;
    }
    //endregion

    //region PlanOpTranslationStrategy Implementation
    @Override
    protected GraphTraversal translateImpl(GraphTraversal traversal, PlanWithCost<Plan, PlanDetailedCost> plan, PlanOp planOp, TranslationContext context) {
        EntityOp entityOp = (EntityOp) planOp;

        if (PlanUtil.isFirst(plan.getPlan(), planOp)) {
            traversal = context.getGraphTraversalSource().V().as(entityOp.getAsgEbase().geteBase().geteTag());
            appendEntity(traversal, entityOp.getAsgEbase().geteBase(), context.getOnt());
        } else {
            Optional<PlanOp> previousPlanOp = PlanUtil.adjacentPrev(plan.getPlan(), planOp);
            if (previousPlanOp.isPresent() &&
                    (previousPlanOp.get() instanceof RelationOp ||
                            previousPlanOp.get() instanceof RelationFilterOp)) {
                switch (this.options) {
                    case none:
                        return traversalEntity(traversal, context, plan, entityOp);
                    case filterEntity:
                        traversalEntity(traversal, context, entityOp);
                }
            }
        }

        return traversal;
    }

    private GraphTraversal traversalEntity(GraphTraversal traversal, TranslationContext context, EntityOp entityOp) {
        traversal.otherV();
        traversal.outE(GlobalConstants.Labels.PROMISE_FILTER);
        appendEntity(traversal, entityOp.getAsgEbase().geteBase(), context.getOnt());
        traversal.otherV().as(entityOp.getAsgEbase().geteBase().geteTag());
        return traversal;
    }

    private GraphTraversal traversalEntity(GraphTraversal traversal, TranslationContext context, PlanWithCost<Plan, PlanDetailedCost> plan, EntityOp entityOp) {
        if (PlanUtil.isLast(plan.getPlan(), entityOp)) {
            //last step that is an entity step
            traversal.otherV().as(entityOp.getAsgEbase().geteBase().geteTag());
            appendEntity(traversal, entityOp.getAsgEbase().geteBase(), context.getOnt());
        } else {
            traversal.otherV().as(entityOp.getAsgEbase().geteBase().geteTag());
        }
        return traversal;
    }
    //endregion

    //region Private Methods
    private GraphTraversal appendEntity(GraphTraversal traversal,
                                        EEntityBase entity,
                                        Ontology.Accessor ont) {

        if (entity instanceof EConcrete) {
            traversal.has(GlobalConstants.HasKeys.CONSTRAINT,
                    P.eq(Constraint.by(__.start().and(
                            __.start().has(T.id, P.eq(((EConcrete) entity).geteID())),
                            __.start().has(T.label, P.eq(EntityTranslationUtil.getValidEntityNames(ont, entity).get(0)))))));
        } else if (entity instanceof ETyped || entity instanceof EUntyped) {
            List<String> eTypeNames = EntityTranslationUtil.getValidEntityNames(ont, entity);
            if (eTypeNames.isEmpty()) {
                traversal.has(GlobalConstants.HasKeys.CONSTRAINT,
                        Constraint.by(__.start().has(T.label, P.eq(GlobalConstants.Labels.NONE))));
            } else if (eTypeNames.size() == 1) {
                traversal.has(GlobalConstants.HasKeys.CONSTRAINT,
                        Constraint.by(__.start().has(T.label, P.eq(eTypeNames.get(0)))));
            } else if (eTypeNames.size() > 1) {
                traversal.has(GlobalConstants.HasKeys.CONSTRAINT,
                        Constraint.by(__.start().has(T.label, P.within(eTypeNames))));
            }
        }
        return traversal;
    }
    //endregion

    //region Fields
    private EntityTranslationOptions options;
    //endregion
}
