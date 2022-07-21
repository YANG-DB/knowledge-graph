package org.opensearch.graph.model.execution.plan.composite;




import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.execution.plan.AsgEBasePlanOp;
import org.opensearch.graph.model.execution.plan.PlanOp;
import org.opensearch.graph.model.query.quant.QuantBase;
import javaslang.collection.Stream;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class UnionOp extends AsgEBasePlanOp<QuantBase> {

    private List<Plan> plans;

    public UnionOp() {
        super(new AsgEBase<>());
    }

    public UnionOp(List<List<PlanOp>> plans) {
        this();
        this.plans = Stream.ofAll(plans).map(Plan::new).toJavaList();
    }

    public UnionOp(AsgEBase<QuantBase> unionStep, List<List<PlanOp>> plans) {
        super(unionStep);
        this.plans = Stream.ofAll(plans).map(Plan::new).toJavaList();
    }

    public UnionOp(AsgEBase<QuantBase> unionStep, List<PlanOp>...plans) {
        super(unionStep);
        this.plans = Stream.ofAll(Arrays.asList(plans)).map(Plan::new).toJavaList();
    }

    public List<Plan> getPlans() {
        return plans;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnionOp unionOp = (UnionOp) o;
        return Objects.equals(plans, unionOp.plans);
    }

    @Override
    public int hashCode() {
        return Objects.hash(plans);
    }
}
