package org.opensearch.graph.model.query.properties.constraint;

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



import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collections;
import java.util.Set;


@JsonIgnoreProperties(ignoreUnknown = true)
public class OptionalUnaryParameterizedConstraint extends ParameterizedConstraint {

    public OptionalUnaryParameterizedConstraint() {}

    public OptionalUnaryParameterizedConstraint(ConstraintOp defaultValue, Set<ConstraintOp> ops, NamedParameter parameter) {
        this(defaultValue,null,ops,parameter);
    }

    public OptionalUnaryParameterizedConstraint(ConstraintOp defaultValue,Object exp, Set<ConstraintOp> ops, NamedParameter parameter) {
        //set defaultValue as the op field of the base class (calling OptionalUnaryParameterizedConstraint.getOps() will result with the default value)
        super(defaultValue,exp,parameter);
        this.operations = ops;
    }

    public Set<ConstraintOp> getOperations() {
        return operations;
    }

    @Override
    public OptionalUnaryParameterizedConstraint clone() {
        return new OptionalUnaryParameterizedConstraint(getOp(),getExpr(),getOperations(), getParameter());
    }

    private Set<ConstraintOp> operations = Collections.emptySet();
}
