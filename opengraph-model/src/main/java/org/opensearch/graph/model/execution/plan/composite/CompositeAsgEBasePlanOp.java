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
 * CompositeAsgEBasePlanOp.java - opengraph-model - yangdb - 2,016
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
import org.opensearch.graph.model.execution.plan.AsgEBaseContainer;
import org.opensearch.graph.model.execution.plan.PlanOp;
import org.opensearch.graph.model.query.EBase;
import javaslang.collection.Stream;

/**
 * Created by Roman on 11/25/2017.
 */
public class CompositeAsgEBasePlanOp<T extends EBase> extends CompositePlanOp implements AsgEBaseContainer<T> {
    //region Constructors
    public CompositeAsgEBasePlanOp() {}

    public CompositeAsgEBasePlanOp(AsgEBase<T> asgElement, Iterable<PlanOp> ops) {
        super(ops);
        this.asgEbase = asgElement;
    }

    public CompositeAsgEBasePlanOp(AsgEBase<T> asgElement, PlanOp...ops) {
        this(asgElement, Stream.of(ops));
    }

    public CompositeAsgEBasePlanOp(AsgEBase<T> asgElement, CompositePlanOp compositePlanOp) {
        this(asgElement, compositePlanOp.getOps());
    }
    //endregion

    //region Override Methods
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(" + this.asgEbase.toString() + ")";
    }
    //endregion

    //region Properties
    @Override
    public AsgEBase<T> getAsgEbase() {
        return asgEbase;
    }

    public void setAsgElement(AsgEBase<T> value) {
        this.asgEbase = value;
    }
    //endregion

    //region Fields
    private AsgEBase<T> asgEbase;
    //endregion
}
