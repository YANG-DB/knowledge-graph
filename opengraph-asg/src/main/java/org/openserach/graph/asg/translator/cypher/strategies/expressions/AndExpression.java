package org.openserach.graph.asg.translator.cypher.strategies.expressions;

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



import com.bpodgursky.jbool_expressions.And;
import com.bpodgursky.jbool_expressions.Expression;
import org.openserach.graph.asg.translator.cypher.strategies.CypherStrategyContext;
import org.openserach.graph.asg.translator.cypher.strategies.CypherUtils;
import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.query.EBase;
import org.opensearch.graph.model.query.quant.QuantBase;

import java.util.List;
import java.util.Optional;

public class AndExpression implements ExpressionStrategies {

    public AndExpression(Iterable<ExpressionStrategies> strategies) {
        this.strategies = strategies;
    }

    @Override
    public void apply(Optional<Expression> parent, Expression expression, AsgQuery query, CypherStrategyContext context) {
        //filter only AND expressions
        //todo parent is empty - create a 'all'-quant as query start
        if (!parent.isPresent()) {
            if(!AsgQueryUtil.nextAdjacentDescendant(query.getStart(), QuantBase.class).isPresent()) {
                CypherUtils.quant(query.getStart().getNext().get(0), Optional.of(expression), query, context);
            }
            context.scope(query.getStart());
        }

        And and = (And) expression;
        CypherUtils.reverse(((List<Expression>) and.getChildren()))
                .forEach(c -> {
                    final AsgEBase<? extends EBase> base = context.getScope();
                    strategies.forEach(s -> {
                        if(s.isApply(c)) s.apply(Optional.of(and), c, query, context);
                    });
                    context.scope(base);
                });
    }

    @Override
    public boolean isApply(Expression expression) {
        return expression instanceof com.bpodgursky.jbool_expressions.And;
    }

    private Iterable<ExpressionStrategies> strategies;

}
