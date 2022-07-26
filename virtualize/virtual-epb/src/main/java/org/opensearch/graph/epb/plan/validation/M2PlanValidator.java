package org.opensearch.graph.epb.plan.validation;





import org.opensearch.graph.epb.plan.validation.opValidator.*;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.validation.ValidationResult;

import java.util.Arrays;

public class M2PlanValidator extends CompositePlanValidator<Plan,AsgQuery> {

    //region Constructors
    public M2PlanValidator() {
        super(Mode.all);

        //this.validators = Collections.singletonList(new ChainedPlanValidator(buildNestedPlanOpValidator(10)));
        this.validators = Arrays.asList(new ChainedPlanValidator(buildNestedPlanOpValidator(10,3 , false)));
    }
    //endregion

    //region CompositePlanValidator Implementation
    @Override
    public ValidationResult isPlanValid(Plan plan, AsgQuery query) {
        return super.isPlanValid(plan, query);
    }
    //endregion

    //region Private Methods
    private ChainedPlanValidator.PlanOpValidator buildNestedPlanOpValidator(int numNestingLevels, int joinDepth, boolean validateJoinOnly) {
        if (numNestingLevels == 0) {
                    return new CompositePlanOpValidator(CompositePlanOpValidator.Mode.all,
//                            new SingleEntityValidator(),
                            new AdjacentPlanOpValidator(),
                            new NoRedundantRelationOpValidator(),
                            new RedundantGoToEntityOpValidator(),
                            new ReverseRelationOpValidator(),
                            new OptionalCompletePlanOpValidator(),
                            new JoinCompletePlanOpValidator(),
                            new JoinIntersectionPlanOpValidator(),
                            new JoinOpDepthValidator(joinDepth),
                            new SingleEntityJoinValidator(),
                            new JoinBranchSameStartAndEndValidator(),
                            new StraightPathJoinOpValidator());
        }
        ChainedPlanValidator.PlanOpValidator leftJoinBranchValidator = buildNestedPlanOpValidator(numNestingLevels - 1, joinDepth-1, true);
        ChainedPlanValidator.PlanOpValidator rightJoinBranchValidator = buildNestedPlanOpValidator(numNestingLevels - 1, joinDepth-1, validateJoinOnly);
        return new CompositePlanOpValidator(CompositePlanOpValidator.Mode.all,
//                new SingleEntityValidator(),
                new AdjacentPlanOpValidator(),
                new NoRedundantRelationOpValidator(),
                new RedundantGoToEntityOpValidator(),
                new ReverseRelationOpValidator(),
                new OptionalCompletePlanOpValidator(),
                new JoinCompletePlanOpValidator(validateJoinOnly),
                new JoinIntersectionPlanOpValidator(),
                new JoinOpDepthValidator(joinDepth),
                new StraightPathJoinOpValidator(),
                new SingleEntityJoinValidator(),
                new JoinBranchSameStartAndEndValidator(),
                new JoinOpCompositeValidator(
                        new ChainedPlanValidator(leftJoinBranchValidator),
                        new ChainedPlanValidator(rightJoinBranchValidator)),
                new ChainedPlanOpValidator(leftJoinBranchValidator)
                );
    }
    //endregion
}
