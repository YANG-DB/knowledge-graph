package org.opensearch.graph.model.execution.plan.composite;






import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.execution.plan.PlanOp;
import org.opensearch.graph.model.query.aggregation.CountComp;
import javaslang.collection.Stream;

/**
 * Created by lior.perry on 23/02/2017.
 */
public class CountOp extends CompositeAsgEBasePlanOp<CountComp> {
    //region Constructors

    public CountOp() {}

    public CountOp(AsgEBase<CountComp> asgEBase, Iterable<PlanOp> ops) {
        super(asgEBase, ops);
    }

    public CountOp(AsgEBase<CountComp> asgEBase, PlanOp...ops) {
        this(asgEBase, Stream.of(ops));
    }
    //endregion
}
