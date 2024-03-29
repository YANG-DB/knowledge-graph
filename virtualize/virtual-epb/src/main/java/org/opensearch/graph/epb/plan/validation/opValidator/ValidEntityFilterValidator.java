package org.opensearch.graph.epb.plan.validation.opValidator;

/*-
 * #%L
 * virtual-epb
 * %%
 * Copyright (C) 2016 - 2022 org.opensearch
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */





import org.opensearch.graph.dispatcher.utils.PlanUtil;
import org.opensearch.graph.epb.plan.validation.ChainedPlanValidator;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.PlanOp;
import org.opensearch.graph.model.execution.plan.composite.CompositePlanOp;
import org.opensearch.graph.model.execution.plan.entity.EntityFilterOp;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import org.opensearch.graph.model.query.properties.BasePropGroup;
import org.opensearch.graph.model.query.properties.EProp;
import org.opensearch.graph.model.query.properties.EPropGroup;
import org.opensearch.graph.model.query.properties.constraint.WhereByConstraint;
import org.opensearch.graph.model.query.properties.constraint.WhereByFacet;
import org.opensearch.graph.model.validation.ValidationResult;
import javaslang.collection.Stream;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static java.util.stream.Stream.of;

public class ValidEntityFilterValidator implements ChainedPlanValidator.PlanOpValidator {
    @Override
    public void reset() {

    }

    @Override
    public ValidationResult isPlanOpValid(AsgQuery query, CompositePlanOp compositePlanOp, int opIndex) {
        PlanOp planOp = compositePlanOp.getOps().get(opIndex);
        if (planOp instanceof EntityFilterOp) {
            EntityFilterOp filterOp = (EntityFilterOp) planOp;
            EPropGroup group = filterOp.getAsgEbase().geteBase();

            if (!group.getProps().isEmpty() || !group.getGroups().isEmpty()) {
                List<EProp> props = group.getProps();
                List<EProp> groupProps = Stream.ofAll(group.getGroups()).flatMap(BasePropGroup::getProps).toJavaList();
                Optional<EProp> whereClause = of(props, groupProps)
                        .flatMap(Collection::stream)
                        .filter(this::isWhereClause)
                        .findAny();

                if (whereClause.isPresent()) {
                    WhereByFacet constraint = (WhereByFacet) whereClause.get().getCon();
                    Optional<PlanOp> op = PlanUtil.first(compositePlanOp,
                            planHasValidTagged(constraint.getTagEntity()));
                    if(op.isPresent())
                        return ValidationResult.OK;
                    //none valid plan
                    return new ValidationResult(
                            false,this.getClass().getSimpleName(),
                            "ValidEntityFilterValidator(no 'where by' tag found) :Validation failed on:" + compositePlanOp.toString() + "<" + opIndex + ">");

                }

            }

        }
        return ValidationResult.OK;
    }

    private Predicate<PlanOp> planHasValidTagged(String tag) {
        return po->
                 (po instanceof EntityOp)
                        && ((EntityOp) po).getAsgEbase().geteBase().geteTag()!=null
                        && ((EntityOp) po).getAsgEbase().geteBase().geteTag().equals(tag);
    }

    private boolean isWhereClause(EProp p) {
        return p.getCon() != null && p.getCon() instanceof WhereByConstraint;
    }


}
