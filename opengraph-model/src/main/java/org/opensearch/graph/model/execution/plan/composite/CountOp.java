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






import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.execution.plan.PlanOp;
import org.opensearch.graph.model.query.aggregation.CountComp;
import javaslang.collection.Stream;

/**
 * Created by lior.perry on 23/02/2017.
 */
public class CountOp extends CompositeAsgEBasePlanOp<CountComp> {
    //region Constructors

    public CountOp() {}

    public CountOp(AsgEBase<CountComp> asgEBase, Iterable<PlanOp> ops) {
        super(asgEBase, ops);
    }

    public CountOp(AsgEBase<CountComp> asgEBase, PlanOp...ops) {
        this(asgEBase, Stream.of(ops));
    }
    //endregion
}
