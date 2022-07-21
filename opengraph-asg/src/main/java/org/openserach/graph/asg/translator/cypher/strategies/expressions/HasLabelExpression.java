package org.openserach.graph.asg.translator.cypher.strategies.expressions;



import org.openserach.graph.asg.translator.cypher.strategies.CypherStrategyContext;
import org.openserach.graph.asg.translator.cypher.strategies.CypherUtils;
import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.query.EBase;
import org.opensearch.graph.model.query.entity.EEntityBase;
import org.opensearch.graph.model.query.properties.EPropGroup;
import org.opencypher.v9_0.expressions.*;

import java.util.*;
import java.util.stream.Collectors;

import static org.opensearch.graph.model.query.properties.constraint.Constraint.of;
import static org.opensearch.graph.model.query.properties.constraint.ConstraintOp.inSet;
import static scala.collection.JavaConverters.asJavaCollectionConverter;

public class HasLabelExpression extends BaseExpressionStrategy {

    @Override
    public void apply(Optional<com.bpodgursky.jbool_expressions.Expression> parent, com.bpodgursky.jbool_expressions.Expression expression, AsgQuery query, CypherStrategyContext context) {
        HasLabels hasLabels = ((HasLabels) ((CypherUtils.Wrapper) ((com.bpodgursky.jbool_expressions.Variable) expression).getValue()).getExpression());
        Collection<LabelName> labelNames = asJavaCollectionConverter(hasLabels.labels()).asJavaCollection();
        Variable variable = (Variable) hasLabels.expression();

        if (!parent.isPresent()) {
            CypherUtils.quant(query.getStart().getNext().isEmpty() ? query.getStart() : query.getStart().getNext().get(0), Optional.of(expression), query, context);
            context.scope(query.getStart());
        }

        //first find the node element by its var name in the query
        final Optional<AsgEBase<EBase>> byTag = AsgQueryUtil.getByTag(context.getScope(), variable.name());
        if(!byTag.isPresent()) return;

        //when tag is of entity type
        if(EEntityBase.class.isAssignableFrom(byTag.get().geteBase().getClass())) {

            //update the scope
            context.scope(byTag.get());
            //change scope to quant
            final AsgEBase<? extends EBase> quantAsg = CypherUtils.quant(byTag.get(), parent, query, context);
            //add the label eProp constraint
            final int current = Math.max(quantAsg.getNext().stream().mapToInt(p -> p.geteNum()).max().orElse(0), quantAsg.geteNum());
            final List<String> labels = labelNames.stream().map(l -> l.name()).collect(Collectors.toList());

            if (!AsgQueryUtil.nextAdjacentDescendant(quantAsg, EPropGroup.class).isPresent()) {
                quantAsg.addNext(new AsgEBase<>(new EPropGroup(current + 1, CypherUtils.type(parent, Collections.EMPTY_SET))));
            }

            ((EPropGroup) AsgQueryUtil.nextAdjacentDescendant(quantAsg, EPropGroup.class).get().geteBase())
                    .getProps().add(addPredicate(current, "type", of(inSet, labels)));
        }
    }

    @Override
    public boolean isApply(com.bpodgursky.jbool_expressions.Expression expression) {
        return (expression instanceof com.bpodgursky.jbool_expressions.Variable) &&
                ((CypherUtils.Wrapper) ((com.bpodgursky.jbool_expressions.Variable) expression).getValue()).getExpression() instanceof HasLabels;
    }


}
