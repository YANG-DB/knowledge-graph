package org.opensearch.graph.epb.plan.extenders;



import org.opensearch.graph.dispatcher.epb.PlanExtensionStrategy;
import org.opensearch.graph.dispatcher.utils.PlanUtil;
import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.composite.CompositePlanOp;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import org.opensearch.graph.model.execution.plan.entity.GoToEntityOp;
import org.opensearch.graph.model.query.entity.EEntityBase;
import javaslang.Tuple2;
import javaslang.collection.Map;
import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class GotoJoinExtensionStrategy implements PlanExtensionStrategy<Plan, AsgQuery>  {
    @Override
    public Iterable<Plan> extendPlan(Optional<Plan> plan, AsgQuery query) {
        if (!plan.isPresent()) {
            return Collections.emptyList();
        }

        List<Plan> plans = new ArrayList<>();

        EntityOp lastEntityOp = PlanUtil.last$(plan.get(), EntityOp.class);
        CompositePlanOp flattenedPlan = PlanUtil.flat(plan.get());


        Map<Integer, AsgEBase<EEntityBase>> entities = Stream.ofAll(flattenedPlan.getOps())
                .filter(op -> ((op instanceof EntityOp) && !(op instanceof GoToEntityOp) && !((EntityOp) op).getAsgEbase().equals(lastEntityOp.getAsgEbase())))
                .map(op -> (EntityOp) op)
                .toMap(op -> new Tuple2<>(op.getAsgEbase().geteNum(), op.getAsgEbase()));

        for (AsgEBase<EEntityBase> ancestor: entities.values()) {
            Plan newPlan = plan.get().withOp(new GoToEntityOp(ancestor));
            plans.add(newPlan);
        }

        return plans;
    }
}
