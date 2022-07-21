package org.openserach.graph.asg.translator.cypher.strategies.expressions;




import com.bpodgursky.jbool_expressions.Expression;
import org.openserach.graph.asg.translator.cypher.strategies.CypherUtils;
import org.opensearch.graph.model.query.properties.constraint.Constraint;
import org.opencypher.v9_0.expressions.FunctionInvocation;
import org.opencypher.v9_0.expressions.Variable;

import static org.opensearch.graph.model.query.properties.constraint.Constraint.of;
import static org.opensearch.graph.model.query.properties.constraint.ConstraintOp.distinct;
import static scala.collection.JavaConverters.asJavaCollectionConverter;

public class DistinctExpression extends BaseFunctionExpression<FunctionInvocation> {
    public static final String DISTINCT = "distinct";

    @Override
    protected Variable getFuncVars(FunctionInvocation invocation) {
        return ((Variable)((FunctionInvocation)asJavaCollectionConverter(invocation.inputs()).asJavaCollection().iterator().next()._1()).args().iterator().next());
    }

    @Override
    protected String getFuncName(FunctionInvocation invocation) {
        return ((FunctionInvocation)asJavaCollectionConverter(invocation.inputs()).asJavaCollection().iterator().next()._1()).name();
    }

    @Override
    protected FunctionInvocation get(org.opencypher.v9_0.expressions.Expression expression) {
        return (FunctionInvocation) expression;
    }

    @Override
    protected Constraint constraint(String operator, String literal) {
        switch (operator) {
            case "distinct":
                return of(distinct, literal);
        }
        throw new IllegalArgumentException("condition " + literal.toString() + " doesn't match any supported V1 constraints");
    }


    @Override
    public boolean isApply(Expression expression) {
        return (expression instanceof com.bpodgursky.jbool_expressions.Variable) &&
                (((CypherUtils.Wrapper) ((com.bpodgursky.jbool_expressions.Variable) expression).getValue()).getExpression() instanceof org.opencypher.v9_0.expressions.FunctionInvocation) &&
                ((FunctionInvocation) ((CypherUtils.Wrapper) ((com.bpodgursky.jbool_expressions.Variable) expression).getValue()).getExpression()).name().equals(DISTINCT);
    }
}
