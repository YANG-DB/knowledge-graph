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



import com.bpodgursky.jbool_expressions.Expression;
import org.openserach.graph.asg.translator.cypher.strategies.CypherStrategyContext;
import org.openserach.graph.asg.translator.cypher.strategies.CypherUtils;
import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.query.Rel;
import org.opensearch.graph.model.query.properties.RelProp;
import org.opensearch.graph.model.query.properties.RelPropGroup;
import org.opencypher.v9_0.expressions.HasLabels;
import org.opencypher.v9_0.expressions.LabelName;
import org.opencypher.v9_0.expressions.Variable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.opensearch.graph.model.query.properties.constraint.Constraint.of;
import static org.opensearch.graph.model.query.properties.constraint.ConstraintOp.inSet;
import static scala.collection.JavaConverters.asJavaCollectionConverter;

public class HasRelationLabelExpression implements ExpressionStrategies {

    @Override
    public void apply(Optional<com.bpodgursky.jbool_expressions.Expression> parent, com.bpodgursky.jbool_expressions.Expression expression, AsgQuery query, CypherStrategyContext context) {
        HasLabels hasLabels = ((HasLabels) ((CypherUtils.Wrapper) ((com.bpodgursky.jbool_expressions.Variable) expression).getValue()).getExpression());
        Collection<LabelName> labels = asJavaCollectionConverter(hasLabels.labels()).asJavaCollection();
        Variable variable = (Variable) hasLabels.expression();

        //first find the node element by its var name in the query

        final Optional<AsgEBase<Rel>> first = AsgQueryUtil.elements(context.getScope(), Rel.class).stream()
                .filter(p -> p.geteBase().getWrapper().equals(variable.name()))
                .findFirst();

        if (!first.isPresent()) return;


        //when tag is of entity type
        if (Rel.class.isAssignableFrom(first.get().geteBase().getClass())) {
            //update the scope
            context.scope(first.get());
            //add the label eProp constraint

            if (!AsgQueryUtil.bAdjacentDescendant(first.get(), RelPropGroup.class).isPresent()) {
                final int current = Math.max(first.get().getB().stream().mapToInt(p -> p.geteNum()).max().orElse(0), first.get().geteNum());
                first.get().addBChild(new AsgEBase<>(new RelPropGroup(100 * current, CypherUtils.type(parent, Collections.EMPTY_SET))));
            }

            final List<String> labelNames = labels.stream().map(l -> l.name()).collect(Collectors.toList());
            final int current = Math.max(first.get().getB().stream().mapToInt(p -> p.geteNum()).max().orElse(0), first.get().geteNum());
            ((RelPropGroup) AsgQueryUtil.bAdjacentDescendant(first.get(), RelPropGroup.class).get().geteBase())
                    .getProps().add(new RelProp(current + 1, "type", of(inSet, labelNames), 0));
        }
    }

    @Override
    public boolean isApply(Expression expression) {
        return (expression instanceof com.bpodgursky.jbool_expressions.Variable) &&
                ((CypherUtils.Wrapper) ((com.bpodgursky.jbool_expressions.Variable) expression).getValue()).getExpression() instanceof HasLabels;
    }


}
