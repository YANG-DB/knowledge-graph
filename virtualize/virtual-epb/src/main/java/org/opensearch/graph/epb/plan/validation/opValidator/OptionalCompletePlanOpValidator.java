package org.opensearch.graph.epb.plan.validation.opValidator;



import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.dispatcher.utils.PlanUtil;
import org.opensearch.graph.model.validation.ValidationResult;
import org.opensearch.graph.epb.plan.validation.ChainedPlanValidator;
import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.AsgEBaseContainer;
import org.opensearch.graph.model.execution.plan.PlanOp;
import org.opensearch.graph.model.execution.plan.composite.CompositePlanOp;
import org.opensearch.graph.model.execution.plan.composite.OptionalOp;
import org.opensearch.graph.model.execution.plan.entity.EntityNoOp;
import org.opensearch.graph.model.query.EBase;
import org.opensearch.graph.model.query.Rel;
import org.opensearch.graph.model.query.entity.EConcrete;
import org.opensearch.graph.model.query.entity.ETyped;
import org.opensearch.graph.model.query.entity.EUntyped;
import org.opensearch.graph.model.query.optional.OptionalComp;
import org.opensearch.graph.model.query.properties.EProp;
import org.opensearch.graph.model.query.properties.EPropGroup;
import org.opensearch.graph.model.query.properties.RelProp;
import org.opensearch.graph.model.query.properties.RelPropGroup;
import javaslang.collection.Stream;

import java.util.Set;

public class OptionalCompletePlanOpValidator implements ChainedPlanValidator.PlanOpValidator {
    //region PlanOpValidator Implementation
    @Override
    public void reset() {

    }

    @Override
    public ValidationResult isPlanOpValid(AsgQuery query, CompositePlanOp compositePlanOp, int opIndex) {
        PlanOp currentPlanOp = compositePlanOp.getOps().get(opIndex);
        if (!OptionalOp.class.isAssignableFrom(currentPlanOp.getClass())) {
            return ValidationResult.OK;
        }

        if (opIndex == compositePlanOp.getOps().size() - 1) {
            return ValidationResult.OK;
        }

        if (!isOptionalOpComplete((OptionalOp)currentPlanOp, query)) {
            return new ValidationResult(
                    false,this.getClass().getSimpleName(),
                    "OptionalOpValidation failed on:" + compositePlanOp.toString() + "<" + opIndex + ">");
        }

        return ValidationResult.OK;
    }
    //endregion

    //region Private Methods
    private boolean isOptionalOpComplete(OptionalOp optionalOp, AsgQuery query) {
        AsgEBase<OptionalComp> optionalComp = AsgQueryUtil.element$(query, optionalOp.getAsgEbase().geteNum());

        final Set<Class<? extends EBase>> classSet = Stream.of(ETyped.class, EConcrete.class, EUntyped.class, Rel.class,
                EProp.class, EPropGroup.class, RelProp.class, RelPropGroup.class)
                .toJavaSet();

        Set<Integer> optionalEnums =
                Stream.ofAll(AsgQueryUtil.descendantBDescendants(optionalComp, asgEBase -> classSet.contains(asgEBase.geteBase().getClass()), asgEBase -> true))
                        .map(asgEbase -> asgEbase.geteBase().geteNum())
                        .toJavaSet();

        Set<Integer> optionalOpEnums = Stream.ofAll(PlanUtil.flat(optionalOp).getOps())
                .filter(planOp -> !EntityNoOp.class.isAssignableFrom(planOp.getClass()))
                .filter(planOp -> AsgEBaseContainer.class.isAssignableFrom(planOp.getClass()))
                .map(planOp -> ((AsgEBaseContainer)planOp).getAsgEbase().geteBase().geteNum())
                .toJavaSet();

        return optionalEnums.equals(optionalOpEnums);
    }
    //endregion
}
