package org.opensearch.graph.model.execution.plan.composite;

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

/*-
 *
 * UnionOp.java - opengraph-model - yangdb - 2,016
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
