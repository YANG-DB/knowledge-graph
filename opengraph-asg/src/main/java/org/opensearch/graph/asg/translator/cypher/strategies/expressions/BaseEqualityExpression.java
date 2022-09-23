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
import org.opensearch.graph.asg.translator.cypher.strategies.CypherStrategyContext;
import org.opensearch.graph.asg.translator.cypher.strategies.CypherUtils;
import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.query.EBase;
import org.opensearch.graph.model.query.Rel;
import org.opensearch.graph.model.query.entity.EEntityBase;
import org.opensearch.graph.model.query.properties.EPropGroup;
import org.opensearch.graph.model.query.properties.RelPropGroup;
import org.opensearch.graph.model.query.properties.constraint.Constraint;
import org.opencypher.v9_0.expressions.BinaryOperatorExpression;
import org.opencypher.v9_0.expressions.Literal;
import org.opencypher.v9_0.expressions.Property;
import org.opencypher.v9_0.expressions.Variable;

import java.util.Collections;
import java.util.Optional;

public abstract class BaseEqualityExpression<T extends BinaryOperatorExpression> extends BaseExpressionStrategy {

    @Override
    public void apply(Optional<Expression> parent, Expression expression, AsgQuery query, CypherStrategyContext context) {
        T exp = get((((CypherUtils.Wrapper) ((com.bpodgursky.jbool_expressions.Variable) expression).getValue()).getExpression()));
        org.opencypher.v9_0.expressions.Expression lhs = getLhs(exp);
        org.opencypher.v9_0.expressions.Expression rhs = getRhs(exp);

        Optional<String> keyNameOp = getKeyName(lhs);
        if(!keyNameOp.isPresent()) return;

        String keyName = keyNameOp.get();
        String tag = getTagName(lhs);

        //first find the node element by its var name in the query
        Optional<AsgEBase<EBase>> byTag = AsgQueryUtil.getByTag(context.getScope(), tag);
        if (!byTag.isPresent())
            byTag = AsgQueryUtil.getByTag(query.getStart(), tag);

        if (!byTag.isPresent()) return;

        //when tag is of entity type
        if (EEntityBase.class.isAssignableFrom(byTag.get().geteBase().getClass())) {

            //update the scope
            context.scope(byTag.get());
            //change scope to quant
            final AsgEBase<? extends EBase> quantAsg = CypherUtils.quant(byTag.get(), parent, query, context);
            //add the label eProp constraint
            final int current = Math.max(quantAsg.getNext().stream().mapToInt(p -> p.geteNum()).max().orElse(0), quantAsg.geteNum());

            if (!AsgQueryUtil.nextAdjacentDescendant(quantAsg, EPropGroup.class).isPresent()) {
                quantAsg.addNext(new AsgEBase<>(new EPropGroup(current + 1, CypherUtils.type(parent, Collections.EMPTY_SET))));
            }

            ((EPropGroup) AsgQueryUtil.nextAdjacentDescendant(quantAsg, EPropGroup.class).get().geteBase())
                    .getProps().add(addPredicate(current, keyName, constraint(exp.canonicalOperatorSymbol(), rhs)));
        }

        //when tag is of relation type
        if (Rel.class.isAssignableFrom(byTag.get().geteBase().getClass())) {
            //update the scope
            context.scope(byTag.get());

            if (!AsgQueryUtil.bAdjacentDescendant(byTag.get(), RelPropGroup.class).isPresent()) {
                final int current = Math.max(byTag.get().getB().stream().mapToInt(p -> p.geteNum()).max().orElse(0), byTag.get().geteNum());
                byTag.get().addBChild(new AsgEBase<>(new RelPropGroup(100 * current, CypherUtils.type(parent, Collections.EMPTY_SET))));
            }

            final int current = Math.max(byTag.get().getB().stream().mapToInt(p -> p.geteNum()).max().orElse(0), byTag.get().geteNum());
            ((RelPropGroup) AsgQueryUtil.bAdjacentDescendant(byTag.get(), RelPropGroup.class).get().geteBase())
                    .getProps().add(addRelPredicate(current + 1, keyName,
                    constraint(exp.canonicalOperatorSymbol(), (org.opencypher.v9_0.expressions.Expression) literal(lhs, rhs))));
        }
    }

    protected String getTagName(org.opencypher.v9_0.expressions.Expression prop) {
        return CypherUtils.var(prop).get(0).name();
    }

    protected Optional<String> getKeyName(org.opencypher.v9_0.expressions.Expression prop) {
        Property property = null;
        if(Property.class.isAssignableFrom(prop.getClass())) {
            property = ((Property) prop);
        }

        if (CypherUtils.var(property).isEmpty())
            return Optional.empty();

        //in case of compound name (a.b.c) we need to take the complete name after the first var (a)
        if (property.asCanonicalStringVal().contains(CypherUtils.var(property).get(0).asCanonicalStringVal())) {
            return Optional.of(property.asCanonicalStringVal().substring(
                    CypherUtils.var(property).get(0).asCanonicalStringVal().length()+1));
        }
        return Optional.of(((Property) prop).propertyKey().name());
    }

    protected Optional<String> getExpressionName(org.opencypher.v9_0.expressions.Expression lhs) {

        Property property = null;
        if (Property.class.isAssignableFrom(lhs.getClass())) {
            property = ((Property) lhs);
        }

        if (CypherUtils.var(property).isEmpty()) return Optional.empty();

        Variable variable = CypherUtils.var(property).get(0);
        //first find the node element by its var name in the query

        return Optional.of(variable.name());
    }

    protected Object literal(org.opencypher.v9_0.expressions.Expression lhs,
                             org.opencypher.v9_0.expressions.Expression rhs) {
        return Literal.class.isAssignableFrom(lhs.getClass()) ? ((Literal) lhs) :
                Literal.class.isAssignableFrom(rhs.getClass()) ? ((Literal) rhs) : null;
    }

    protected abstract T get(org.opencypher.v9_0.expressions.Expression expression);

    protected org.opencypher.v9_0.expressions.Expression getRhs(T exp) {
        return exp.rhs();
    }

    protected org.opencypher.v9_0.expressions.Expression getLhs(T exp) {
        return exp.lhs();
    }

    protected abstract Constraint constraint(String operator, org.opencypher.v9_0.expressions.Expression literal);

    @Override
    public abstract boolean isApply(Expression expression);


}
