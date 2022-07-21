package org.opensearch.graph.model.execution.plan.composite.descriptors;


/*-
 *
 * UnionOpDescriptor.java - opengraph-model - yangdb - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */

import org.opensearch.graph.model.descriptors.Descriptor;
import org.opensearch.graph.model.execution.plan.PlanOp;
import org.opensearch.graph.model.execution.plan.composite.UnionOp;

public class UnionOpDescriptor implements Descriptor<UnionOp> {
    public UnionOpDescriptor(Descriptor<Iterable<PlanOp>> planOpsDescriptor) {
        this.planOpsDescriptor = planOpsDescriptor;
    }

    @Override
    public String describe(UnionOp item) {
        final StringBuilder builder = new StringBuilder()
                .append(item.getClass().getSimpleName())
                .append("(")
                .append(item)
                .append(")");
        item.getPlans().forEach(p ->
                builder.append("[")
                        .append(this.planOpsDescriptor.describe(p.getOps()))
                        .append("]"));
        return builder.toString();
    }

    private Descriptor<Iterable<PlanOp>> planOpsDescriptor;
}
