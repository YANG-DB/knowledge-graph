package org.opensearch.graph.epb.plan.validation.opValidator;



import org.opensearch.graph.model.execution.plan.entity.EntityJoinOp;
import org.opensearch.graph.model.validation.ValidationResult;
import org.opensearch.graph.epb.plan.validation.ChainedPlanValidator;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.composite.CompositePlanOp;
import org.opensearch.graph.model.execution.plan.PlanOp;
import org.opensearch.graph.model.execution.plan.relation.RelationOp;

import java.util.HashSet;
import java.util.Set;

public class NoRedundantRelationOpValidator implements ChainedPlanValidator.PlanOpValidator{
    //region Constructors
    public NoRedundantRelationOpValidator() {
        this.relationEnums = new HashSet<>();
    }
    //endregion

    //region ChainedPlanValidator Implementation
    @Override
    public void reset() {
        this.relationEnums.clear();
    }

    @Override
    public ValidationResult isPlanOpValid(AsgQuery query, CompositePlanOp compositePlanOp, int opIndex) {
        PlanOp planOp = compositePlanOp.getOps().get(opIndex);
        if(planOp instanceof EntityJoinOp){
            boolean isOk = recursiveJoinTraversal((EntityJoinOp) planOp);
            if(isOk) {
                return ValidationResult.OK;
            }
            else{
                return new ValidationResult(
                        false,this.getClass().getSimpleName(),
                        "NoRedundant:Validation failed on:" + compositePlanOp.toString() + "<" + opIndex + ">");
            }
        }

        if (!(planOp instanceof RelationOp)) {
            return ValidationResult.OK;
        }

        if (!this.relationEnums.contains(((RelationOp) planOp).getAsgEbase().geteNum())){
            this.relationEnums.add(((RelationOp) planOp).getAsgEbase().geteNum());
            return ValidationResult.OK;
        }

        return new ValidationResult(
                false,this.getClass().getSimpleName(),
                "NoRedundant:Validation failed on:" + compositePlanOp.toString() + "<" + opIndex + ">");
    }

    private boolean recursiveJoinTraversal(EntityJoinOp planOp) {
        boolean isOk = true;
        for (PlanOp op : planOp.getLeftBranch().getOps()) {
            if(op instanceof EntityJoinOp){
                isOk &= recursiveJoinTraversal((EntityJoinOp) op);
            }
            if(op instanceof RelationOp) {
                if (!this.relationEnums.contains(((RelationOp) op).getAsgEbase().geteNum())) {
                    this.relationEnums.add(((RelationOp) op).getAsgEbase().geteNum());
                } else {
                    isOk = false;
                }
            }
        }
        for (PlanOp op : planOp.getRightBranch().getOps()) {
            if(op instanceof EntityJoinOp){
                isOk &= recursiveJoinTraversal((EntityJoinOp) op);
            }
            if(op instanceof RelationOp) {
                if (!this.relationEnums.contains(((RelationOp) op).getAsgEbase().geteNum())) {
                    this.relationEnums.add(((RelationOp) op).getAsgEbase().geteNum());
                } else {
                    isOk = false;
                }
            }
        }
        return isOk;
    }
    //endregion

    //region Fields
    private Set<Integer> relationEnums;
    //endregion
}
