package org.openserach.graph.asg.translator.cypher.strategies.expressions;







import com.bpodgursky.jbool_expressions.Expression;
import org.openserach.graph.asg.translator.cypher.strategies.CypherUtils;
import org.opensearch.graph.model.query.properties.EProp;
import org.opensearch.graph.model.query.properties.FunctionEProp;
import org.opensearch.graph.model.query.properties.FunctionRelProp;
import org.opensearch.graph.model.query.properties.constraint.Constraint;
import org.opensearch.graph.model.query.properties.constraint.CountConstraintOp;
import org.opencypher.v9_0.expressions.FunctionInvocation;
import org.opencypher.v9_0.expressions.Variable;

import java.util.Optional;

import static org.opensearch.graph.model.query.properties.constraint.Constraint.of;
import static org.opensearch.graph.model.query.properties.constraint.ConstraintOp.*;

public class CountExpression extends BaseEqualityExpression<org.opencypher.v9_0.expressions.InequalityExpression> {

    public static final String COUNT = "count";

    @Override
    protected org.opencypher.v9_0.expressions.InequalityExpression get(org.opencypher.v9_0.expressions.Expression expression) {
        return (org.opencypher.v9_0.expressions.InequalityExpression) expression;
    }

    @Override
    protected Optional<String> getKeyName(org.opencypher.v9_0.expressions.Expression prop) {
        return Optional.of(((FunctionInvocation)prop.findAggregate().get()).name());
    }

    @Override
    protected String getTagName(org.opencypher.v9_0.expressions.Expression prop) {
        return ((Variable)prop.inputs().seq().last()._1).name();
    }

    protected Constraint constraint(String operator, org.opencypher.v9_0.expressions.Expression literal) {
        switch (operator) {
            case "<": return of(CountConstraintOp.lt,literal.asCanonicalStringVal());
            case "<=":return of(CountConstraintOp.le,literal.asCanonicalStringVal());
            case ">": return of(CountConstraintOp.gt,literal.asCanonicalStringVal());
            case ">=":return of(CountConstraintOp.ge,literal.asCanonicalStringVal());
        }
        throw new IllegalArgumentException("condition "+literal.asCanonicalStringVal()+" doesn't match any supported V1 constraints");
    }

    @Override
    protected FunctionEProp addPredicate(int current, String propery, Constraint constraint) {
        return new FunctionEProp(current,propery,constraint);
    }
    @Override
    protected FunctionRelProp addRelPredicate(int current, String propery, Constraint constraint) {
        return new FunctionRelProp(current,propery,constraint);
    }

    @Override
    public boolean isApply(Expression expression) {
        return (expression instanceof com.bpodgursky.jbool_expressions.Variable) &&
                (((CypherUtils.Wrapper) ((com.bpodgursky.jbool_expressions.Variable) expression).getValue()).getExpression() instanceof org.opencypher.v9_0.expressions.InequalityExpression) &&
                !(((CypherUtils.Wrapper) ((com.bpodgursky.jbool_expressions.Variable) expression).getValue()).getExpression()).findAggregate().isEmpty() &&
                ((org.opencypher.v9_0.expressions.FunctionInvocation)(((CypherUtils.Wrapper) ((com.bpodgursky.jbool_expressions.Variable) expression).getValue()).getExpression()).findAggregate().get()).name().equals(COUNT);
    }
}
