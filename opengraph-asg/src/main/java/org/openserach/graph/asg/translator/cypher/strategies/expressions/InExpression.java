package org.openserach.graph.asg.translator.cypher.strategies.expressions;





import com.bpodgursky.jbool_expressions.Expression;
import org.openserach.graph.asg.translator.cypher.strategies.CypherUtils;
import org.opensearch.graph.model.query.properties.constraint.Constraint;
import org.opencypher.v9_0.expressions.In;
import org.opencypher.v9_0.expressions.ListLiteral;

import java.util.stream.Collectors;

import static org.opensearch.graph.model.query.properties.constraint.Constraint.of;
import static org.opensearch.graph.model.query.properties.constraint.ConstraintOp.inSet;
import static scala.collection.JavaConverters.asJavaCollectionConverter;

public class InExpression extends BaseEqualityExpression<In> {

    @Override
    protected In get(org.opencypher.v9_0.expressions.Expression expression) {
        return (In) expression;
    }

    protected Constraint constraint(String operator, org.opencypher.v9_0.expressions.Expression literal) {
        switch (operator) {
            case "IN": return of(inSet,asJavaCollectionConverter(literal.arguments()).asJavaCollection().stream().map(p->p.asCanonicalStringVal()).collect(Collectors.toList()));
        }
        throw new IllegalArgumentException("condition "+literal.asCanonicalStringVal()+" doesn't match any supported V1 constraints");
    }

    @Override
    protected org.opencypher.v9_0.expressions.Expression literal(org.opencypher.v9_0.expressions.Expression lhs, org.opencypher.v9_0.expressions.Expression rhs) {
        return ListLiteral.class.isAssignableFrom(lhs.getClass()) ? ((ListLiteral) lhs) :
                ListLiteral.class.isAssignableFrom(rhs.getClass()) ? ((ListLiteral) rhs) : null;
    }

    @Override
    public boolean isApply(Expression expression) {
        return ((expression instanceof com.bpodgursky.jbool_expressions.Variable) &&
                ((CypherUtils.Wrapper) ((com.bpodgursky.jbool_expressions.Variable) expression).getValue()).getExpression() instanceof In);
    }
}
