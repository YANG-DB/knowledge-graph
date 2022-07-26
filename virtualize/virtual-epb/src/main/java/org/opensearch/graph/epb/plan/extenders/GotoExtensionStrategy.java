package org.opensearch.graph.epb.plan.extenders;





import org.opensearch.graph.dispatcher.utils.PlanUtil;
import org.opensearch.graph.dispatcher.epb.PlanExtensionStrategy;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import org.opensearch.graph.model.execution.plan.entity.GoToEntityOp;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class GotoExtensionStrategy implements PlanExtensionStrategy<Plan, AsgQuery> {
    private boolean addInitialPlan;

    public GotoExtensionStrategy(boolean addInitialPlan) {
        this.addInitialPlan = addInitialPlan;
    }

    public GotoExtensionStrategy() {
        this(false);
    }

    @Override
    public Iterable<Plan> extendPlan(Optional<Plan> plan, AsgQuery query) {
        if (!plan.isPresent()) {
            return Collections.emptyList();
        }

        List<Plan> plans = new ArrayList<>();

        EntityOp lastEntityOp = PlanUtil.last$(plan.get(), EntityOp.class);

        List<EntityOp> entityOps = Stream.ofAll(plan.get().getOps())
                .filter(op -> ((op instanceof EntityOp) && !(op instanceof GoToEntityOp) && !op.equals(lastEntityOp)))
                .map(op -> (EntityOp)op)
                .toJavaList();

        for (EntityOp ancestorEntityOp : entityOps) {
            Plan newPlan = plan.get().withOp(new GoToEntityOp(ancestorEntityOp.getAsgEbase()));

            plans.add(newPlan);
        }

        if(this.addInitialPlan){
            plans.add(plan.get());
        }

        return plans;
    }
}
