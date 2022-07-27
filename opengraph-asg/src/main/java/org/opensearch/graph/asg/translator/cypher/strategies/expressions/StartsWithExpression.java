package org.opensearch.graph.asg.translator.cypher.strategies.expressions;

/*-
 * #%L
 * opengraph-asg
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







import com.bpodgursky.jbool_expressions.Expression;
import org.opensearch.graph.asg.translator.cypher.strategies.CypherUtils;
import org.opensearch.graph.model.query.properties.constraint.Constraint;
import org.opencypher.v9_0.expressions.*;

import static org.opensearch.graph.model.query.properties.constraint.Constraint.of;
import static org.opensearch.graph.model.query.properties.constraint.ConstraintOp.startsWith;

public class StartsWithExpression extends BaseEqualityExpression<StartsWith> {

    @Override
    protected StartsWith get(org.opencypher.v9_0.expressions.Expression expression) {
        return (StartsWith) expression;
    }

    protected Constraint constraint(String operator, org.opencypher.v9_0.expressions.Expression literal) {
        switch (operator) {
            case "STARTSWITH": return of(startsWith,literal.asCanonicalStringVal());
        }
        throw new IllegalArgumentException("condition "+literal.asCanonicalStringVal()+" doesn't match any supported V1 constraints");
    }

    @Override
    public boolean isApply(Expression expression) {
        return ((expression instanceof com.bpodgursky.jbool_expressions.Variable) &&
                ((CypherUtils.Wrapper) ((com.bpodgursky.jbool_expressions.Variable) expression).getValue()).getExpression() instanceof StartsWith);
    }
}
