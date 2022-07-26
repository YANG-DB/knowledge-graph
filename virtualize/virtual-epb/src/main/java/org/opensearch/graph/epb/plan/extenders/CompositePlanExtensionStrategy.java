package org.opensearch.graph.epb.plan.extenders;





import com.google.inject.Inject;
import org.opensearch.graph.dispatcher.epb.PlanExtensionStrategy;
import org.opensearch.graph.model.asgQuery.IQuery;
import org.opensearch.graph.model.execution.plan.IPlan;
import javaslang.collection.Stream;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class CompositePlanExtensionStrategy<P extends IPlan, Q extends IQuery> implements PlanExtensionStrategy<P , Q> {
    //region Constructors
    @Inject
    @SafeVarargs
    public CompositePlanExtensionStrategy(PlanExtensionStrategy<P, Q> ... innerExtenders) {
        this(Stream.of(innerExtenders));
    }

    public CompositePlanExtensionStrategy(Iterable<PlanExtensionStrategy<P, Q>> innerExtenders) {
        this.innerExtenders = Stream.ofAll(innerExtenders).toJavaList();
    }
    //endregion

    //region PlanExtensionStrategy Implementation
    @Override
    public Iterable<P> extendPlan(Optional<P> plan, Q query) {
        List<P> plans = new LinkedList<>();
        for(PlanExtensionStrategy<P,Q> extensionStrategy : innerExtenders){
            extensionStrategy.extendPlan(plan, query).forEach(plans::add);
        }
        return plans;
    }
    //endregion

    //region Fields
    protected Iterable<PlanExtensionStrategy<P,Q>> innerExtenders;
    //endregion
}
