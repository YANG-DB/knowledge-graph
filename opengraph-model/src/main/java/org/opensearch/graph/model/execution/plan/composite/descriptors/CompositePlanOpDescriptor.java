package org.opensearch.graph.model.execution.plan.composite.descriptors;

/*-
 * #%L
 * opengraph-model
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






import com.google.inject.Inject;
import org.opensearch.graph.model.descriptors.Descriptor;
import org.opensearch.graph.model.execution.plan.PlanOp;
import org.opensearch.graph.model.execution.plan.composite.CompositePlanOp;

/**
 * Created by roman.margolis on 28/11/2017.
 */
public class CompositePlanOpDescriptor implements Descriptor<CompositePlanOp> {
    //region Constructors
    @Inject
    public CompositePlanOpDescriptor(Descriptor<Iterable<PlanOp>> planOpsDescriptor) {
        this.planOpsDescriptor = planOpsDescriptor;
    }
    //endregion

    //region Descriptor Implementation
    @Override
    public String describe(CompositePlanOp compositePlanOp) {
        return new StringBuilder()
                .append(compositePlanOp.getClass().getSimpleName())
                .append("[")
                .append(this.planOpsDescriptor.describe(compositePlanOp.getOps()))
                .append("]")
                .toString();
    }
    //endregion

    //region Fields
    private Descriptor<Iterable<PlanOp>> planOpsDescriptor;
    //endregion
}
