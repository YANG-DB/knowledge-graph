package org.openserach.graph.asg.translator.cypher.strategies.expressions;







import com.bpodgursky.jbool_expressions.Expression;
import org.openserach.graph.asg.translator.cypher.strategies.CypherUtils;
import org.opensearch.graph.model.query.properties.constraint.Constraint;

import static org.opensearch.graph.model.query.properties.constraint.Constraint.of;
import static org.opensearch.graph.model.query.properties.constraint.ConstraintOp.*;

public class InequalityExpression extends BaseEqualityExpression<org.opencypher.v9_0.expressions.InequalityExpression> {

    @Override
    protected org.opencypher.v9_0.expressions.InequalityExpression get(org.opencypher.v9_0.expressions.Expression expression) {
        return (org.opencypher.v9_0.expressions.InequalityExpression) expression;
    }

    protected Constraint constraint(String operator, org.opencypher.v9_0.expressions.Expression literal) {
        switch (operator) {
            case "<": return of(lt,literal.asCanonicalStringVal());
            case "<=":return of(le,literal.asCanonicalStringVal());
            case ">": return of(gt,literal.asCanonicalStringVal());
            case ">=":return of(ge,literal.asCanonicalStringVal());
        }
        throw new IllegalArgumentException("condition "+literal.asCanonicalStringVal()+" doesn't match any supported V1 constraints");
    }


    @Override
    public boolean isApply(Expression expression) {
        return (expression instanceof com.bpodgursky.jbool_expressions.Variable) &&
                (((CypherUtils.Wrapper) ((com.bpodgursky.jbool_expressions.Variable) expression).getValue()).getExpression() instanceof org.opencypher.v9_0.expressions.InequalityExpression);
    }
}
