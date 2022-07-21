package org.opensearch.graph.epb.plan.validation.opValidator;


import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.validation.ValidationResult;
import org.opensearch.graph.epb.plan.validation.ChainedPlanValidator;
import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.*;
import org.opensearch.graph.model.execution.plan.composite.CompositePlanOp;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import org.opensearch.graph.model.execution.plan.relation.RelationOp;
import org.opensearch.graph.model.query.EBase;
import org.opensearch.graph.model.query.Rel;
import org.opensearch.graph.model.query.entity.EEntityBase;

import java.util.*;

public class ReverseRelationOpValidator implements ChainedPlanValidator.PlanOpValidator {
    //region ChainedPlanValidator.PlanOpValidator Implementation
    @Override
    public void reset() {

    }

    @Override
    public ValidationResult isPlanOpValid(AsgQuery query, CompositePlanOp compositePlanOp, int opIndex) {
        if (opIndex == 0) {
            return ValidationResult.OK;
        }

        PlanOp planOp = compositePlanOp.getOps().get(opIndex);
        if (!(planOp instanceof RelationOp)) {
            return ValidationResult.OK;
        }

        Optional<EntityOp> previousEntityOp = getPreviousOp(compositePlanOp, opIndex, EntityOp.class);
        if (!previousEntityOp.isPresent()) {
            return ValidationResult.OK;
        }

        AsgEBase<EEntityBase> previousEntityAsg = previousEntityOp.get().getAsgEbase();
        AsgEBase<Rel> relAsg = ((RelationOp)planOp).getAsgEbase();

        ValidationResult context = ValidationResult.OK;
        boolean result = areEntityAndRelationReversed(query, previousEntityAsg, relAsg);
        if(!result) {
            context = new ValidationResult(
                    result,this.getClass().getSimpleName(),
                    "Reverse:Validation failed on:" + compositePlanOp.toString() + "<" + opIndex + ">");
        }
        return context;
    }
    //endregion

    //region Private Methods
    private <T extends PlanOp> Optional<T> getPreviousOp(CompositePlanOp compositePlanOp, int opIndex, Class<?> klass) {
        while(opIndex > 0) {
            PlanOp planOp = compositePlanOp.getOps().get(--opIndex);
            if (klass.isAssignableFrom(planOp.getClass())) {
                return Optional.of((T)planOp);
            }
        }

        return Optional.empty();
    }

    private boolean areEntityAndRelationReversed(AsgQuery query, AsgEBase<EEntityBase> asgEntity, AsgEBase<Rel> asgRelation) {
        Set<Integer> entityAndRelationEnums = new HashSet<>(Arrays.asList(asgEntity.geteNum(), asgRelation.geteNum()));

        List<AsgEBase<EBase>> elements = AsgQueryUtil.elements(query, asgEBase -> entityAndRelationEnums.contains(asgEBase.geteNum()));

        boolean isReversed = Rel.class.isAssignableFrom(elements.get(0).geteBase().getClass());

        return isReversed ? ((Rel)(elements.get(0).geteBase())).getDir() != asgRelation.geteBase().getDir() :
                ((Rel)(elements.get(1).geteBase())).getDir() == asgRelation.geteBase().getDir();
    }
    //endregion
}
