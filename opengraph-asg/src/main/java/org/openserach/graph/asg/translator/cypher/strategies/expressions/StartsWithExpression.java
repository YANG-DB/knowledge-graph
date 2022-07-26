package org.openserach.graph.asg.translator.cypher.strategies.expressions;







import com.bpodgursky.jbool_expressions.Expression;
import org.openserach.graph.asg.translator.cypher.strategies.CypherUtils;
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
