package org.opensearch.graph.model.execution.plan.composite;






import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.execution.plan.PlanOp;
import org.opensearch.graph.model.query.optional.OptionalComp;
import javaslang.collection.Stream;

/**
 * Created by lior.perry on 23/02/2017.
 */
public class OptionalOp extends CompositeAsgEBasePlanOp<OptionalComp> {
    //region Constructors

    public OptionalOp() {}

    public OptionalOp(AsgEBase<OptionalComp> asgEBase, Iterable<PlanOp> ops) {
        super(asgEBase, ops);
    }

    public OptionalOp(AsgEBase<OptionalComp> asgEBase, PlanOp...ops) {
        this(asgEBase, Stream.of(ops));
    }
    //endregion
}
