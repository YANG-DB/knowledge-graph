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
import com.bpodgursky.jbool_expressions.Or;
import com.bpodgursky.jbool_expressions.Variable;
import org.opensearch.graph.asg.translator.cypher.strategies.CypherStrategyContext;
import org.opensearch.graph.asg.translator.cypher.strategies.CypherUtils;
import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.query.EBase;
import org.opensearch.graph.model.query.quant.Quant1;
import org.opensearch.graph.model.query.quant.QuantBase;
import org.opensearch.graph.model.query.quant.QuantType;

import java.util.*;
import java.util.stream.Collectors;

import static org.opensearch.graph.model.asgQuery.AsgQueryUtil.maxEntityNum;

public class OrExpression implements ExpressionStrategies {

    public OrExpression(Iterable<ExpressionStrategies> strategies) {
        this.strategies = strategies;
    }

    @Override
    public void apply(Optional<com.bpodgursky.jbool_expressions.Expression> parent, com.bpodgursky.jbool_expressions.Expression expression, AsgQuery query, CypherStrategyContext context) {
        //todo parent is empty - create a 'all'-quant as query start
        //rechain elements after start for new root quant
        List<AsgEBase<? extends EBase>> chain = context.getScope().getNext();
        int maxEnum = maxEntityNum(query);

        if (!parent.isPresent()) {
            context.scope(query.getStart());
            chain = context.getScope().getNext();
            //next find the quant associated with this element - if none found create one
            if (!AsgQueryUtil.nextAdjacentDescendant(context.getScope(), QuantBase.class).isPresent()) {
                final int current = Math.max(context.getScope().geteNum(), maxEntityNum(query));
                //quants will get enum according to the next formula = scopeElement.enum * 100
                final AsgEBase<Quant1> quantAsg = new AsgEBase<>(new Quant1(current * 100, QuantType.some, new ArrayList<>(), 0));
                context.getScope().setNext(Arrays.asList(quantAsg));
            }

            //set root quant at scope
            context.scope(AsgQueryUtil.nextAdjacentDescendant(context.getScope(), QuantBase.class).get());
        }

        com.bpodgursky.jbool_expressions.Or or = (com.bpodgursky.jbool_expressions.Or) expression;
        //max enum


        List<AsgEBase<? extends EBase>> finalChain = chain;
        CypherUtils.reverse(((List<Expression>) or.getChildren()))
                .forEach(c -> {
                    //todo count distinct variables
                    int newMaxEnum = Math.max(maxEnum, maxEntityNum(query));

                    //base quant to add onto
                    final AsgEBase<? extends EBase> base = context.getScope();
                    //duplicate query from scope to end
                    final AsgEBase<? extends EBase> clone = AsgQueryUtil.deepCloneWithEnums(new int[] {newMaxEnum}, finalChain.get(0),
                            e -> true,
                            e -> true);
                    //add duplication to scope
                    context.getScope().addNext(clone);
                    //run strategies on current scope
                    context.scope(clone);
                    strategies.forEach(s -> {
                        if (s.isApply(c)) s.apply(Optional.of(or), c, query, context);
                    });
                    context.scope(base);

                });
    }

    @Override
    public boolean isApply(Expression expression) {
        return expression instanceof com.bpodgursky.jbool_expressions.Or;
    }

    private void groupByNameTags(int maxEnum, AsgQuery query, Or or, CypherStrategyContext context, final List<AsgEBase<? extends EBase>> chain) {
        //group by same tag names
        Map<Optional<String>, List<Expression>> map = ((List<Expression>) or.getChildren()).stream()
                .filter(c -> Variable.class.isAssignableFrom(c.getClass()))
                .collect(Collectors.groupingBy(o -> ((CypherUtils.Wrapper) ((Variable) o).getValue()).getVar()));

        CypherUtils.reverse(map.keySet()).forEach(col -> {
            //todo count distinct variables
            int newMaxEnum = Math.max(maxEnum, maxEntityNum(query));

            //base quant to add onto
            final AsgEBase<? extends EBase> base = context.getScope();
            //duplicate query from scope to end
            final AsgEBase<? extends EBase> clone = AsgQueryUtil.deepCloneWithEnums(new int[] {newMaxEnum}, chain.get(0), e -> true, e -> true);
            //add duplication to scope
            context.getScope().addNext(clone);

            //run strategies on current scope
            context.scope(clone);
            //for each expression in same variable group do strategies
            map.get(col)
                    .forEach(c -> strategies
                            .forEach(s -> {
                                if(s.isApply(c)) s.apply(Optional.of(or), c, query, context);
                            }));
            context.scope(base);

        });

    }

    private Iterable<ExpressionStrategies> strategies;

}
