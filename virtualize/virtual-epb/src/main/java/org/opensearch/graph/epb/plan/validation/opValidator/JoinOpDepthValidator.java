package org.opensearch.graph.epb.plan.validation.opValidator;


import org.opensearch.graph.epb.plan.validation.ChainedPlanValidator;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.composite.CompositePlanOp;
import org.opensearch.graph.model.execution.plan.composite.descriptors.IterablePlanOpDescriptor;
import org.opensearch.graph.model.execution.plan.entity.EntityJoinOp;
import org.opensearch.graph.model.validation.ValidationResult;

public class JoinOpDepthValidator implements ChainedPlanValidator.PlanOpValidator {
    public JoinOpDepthValidator(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public JoinOpDepthValidator() {
        this(3);
    }

    @Override
    public void reset() {

    }

    @Override
    public ValidationResult isPlanOpValid(AsgQuery query, CompositePlanOp compositePlanOp, int opIndex) {
        if(compositePlanOp.getOps().get(opIndex) instanceof EntityJoinOp){
            if(maxDepth <= 0){
                return new ValidationResult(false,this.getClass().getSimpleName(), "Too many nested joins , " + IterablePlanOpDescriptor.getSimple().describe(compositePlanOp.getOps()));
            }
        }
        return ValidationResult.OK;
    }
    private int maxDepth;
}
