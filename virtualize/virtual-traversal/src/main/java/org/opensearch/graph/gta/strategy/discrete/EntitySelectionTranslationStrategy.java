package org.opensearch.graph.gta.strategy.discrete;


import org.opensearch.graph.dispatcher.utils.PlanUtil;
import org.opensearch.graph.gta.strategy.PlanOpTranslationStrategyBase;
import org.opensearch.graph.gta.strategy.utils.TraversalUtil;
import org.opensearch.graph.dispatcher.gta.TranslationContext;
import org.opensearch.graph.model.execution.plan.*;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import org.opensearch.graph.model.execution.plan.entity.EntityFilterOp;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import org.opensearch.graph.model.query.properties.projection.CalculatedFieldProjection;
import org.opensearch.graph.model.GlobalConstants;
import org.opensearch.graph.unipop.predicates.SelectP;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.HasStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.EdgeOtherVertexStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.VertexStep;

import java.util.Optional;

public class EntitySelectionTranslationStrategy extends PlanOpTranslationStrategyBase {
    //region Constructors
    public EntitySelectionTranslationStrategy() {
        super(planOp -> planOp.getClass().equals(EntityFilterOp.class));
    }
    //endregion

    //region PlanOpTranslationStrategyBase Implementation
    @Override
    protected GraphTraversal translateImpl(GraphTraversal traversal, PlanWithCost<Plan, PlanDetailedCost> plan, PlanOp planOp, TranslationContext context) {
        EntityFilterOp lastEntityFilterOp = (EntityFilterOp)planOp;
        Optional<EntityOp> lastEntityOp = PlanUtil.prev(plan.getPlan(), lastEntityFilterOp, EntityOp.class);

        if (!lastEntityOp.isPresent()) {
            return traversal;
        }

        if (Stream.ofAll(lastEntityFilterOp.getAsgEbase().geteBase().getProps())
                .filter(eProp -> eProp.getProj() != null).isEmpty()) {
            return traversal;
        }

        if (!PlanUtil.isFirst(plan.getPlan(), lastEntityOp.get())) {
            Optional<VertexStep> lastVertexStep = TraversalUtil.last(traversal, VertexStep.class);
            if (!lastVertexStep.isPresent()) {
                return traversal;
            }

            if (!isFilterVertexStep(lastVertexStep.get())) {
                traversal.outE(GlobalConstants.Labels.PROMISE_FILTER);
            } else {
                Optional<EdgeOtherVertexStep> lastEdgeOtherVertexStep =
                        TraversalUtil.next(traversal, lastVertexStep.get(), EdgeOtherVertexStep.class);
                lastEdgeOtherVertexStep.ifPresent(edgeOtherVertexStep ->
                        TraversalUtil.remove(traversal, edgeOtherVertexStep));
            }
        }

        Stream.ofAll(TraversalUtil.lastConsecutiveSteps(traversal, HasStep.class))
                .filter(hasStep -> isSelectionHasStep((HasStep<?>)hasStep))
                .forEach(step -> traversal.asAdmin().removeStep(step));

        //process nested (intern) property base group

        //process schematic projection fields exclude calculated field from selection
        Stream.ofAll(lastEntityFilterOp.getAsgEbase().geteBase().getProps())
                .filter(eProp -> eProp.getProj() != null)
                .filter(eProp -> !(eProp.getProj() instanceof CalculatedFieldProjection))
                .forEach(eProp -> traversal.has(context.getOnt().$property$(eProp.getpType()).getName(),
                        SelectP.raw(context.getOnt().$property$(eProp.getpType()).getName())));

        if (PlanUtil.isFirst(plan.getPlan(), lastEntityOp.get())) {
            return traversal;
        }

        Stream.ofAll(TraversalUtil.<Step>lastSteps(traversal, step -> step.getLabels().contains(lastEntityOp.get().getAsgEbase().geteBase().geteTag())))
                .forEach(step -> step.removeLabel(lastEntityOp.get().getAsgEbase().geteBase().geteTag()));

        return traversal.otherV().as(lastEntityOp.get().getAsgEbase().geteBase().geteTag());
    }
    //endregion

    //region Private Methods
    private boolean isFilterVertexStep(VertexStep vertexStep) {
        return !Stream.of(vertexStep.getEdgeLabels())
                .filter(edgeLabel -> edgeLabel.equals(GlobalConstants.Labels.PROMISE_FILTER))
                .isEmpty();

    }

    private boolean isSelectionHasStep(HasStep<?> hasStep) {
        return !Stream.ofAll(hasStep.getHasContainers())
                .filter(hasContainer -> hasContainer.getBiPredicate() instanceof SelectP)
                .isEmpty();
    }
    //endregion
}
