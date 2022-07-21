package org.openserach.graph.asg.translator.sparql.strategies.expressions;



import org.openserach.graph.asg.translator.sparql.strategies.SparqlStrategyContext;
import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.query.EBase;
import org.opensearch.graph.model.query.properties.BaseProp;
import org.opensearch.graph.model.query.properties.constraint.Constraint;
import org.opensearch.graph.model.query.properties.constraint.ConstraintOp;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.algebra.Compare;
import org.eclipse.rdf4j.query.algebra.ValueConstant;
import org.eclipse.rdf4j.query.algebra.ValueExpr;
import org.eclipse.rdf4j.query.algebra.helpers.VarNameCollector;

import java.util.Optional;

public class CompareExpressionStrategy implements ExpressionStrategies {

    @Override
    public void apply(ValueExpr expression, AsgQuery query, SparqlStrategyContext context) {
        if (Compare.class.isAssignableFrom(expression.getClass())) {
            ConstraintOp operator = operator(((Compare) expression).getOperator());
            //collect left side operand
            ValueExpr leftArg = ((Compare) expression).getLeftArg();
            //collect right side operand
            ValueExpr rightArg = ((Compare) expression).getRightArg();

            ValueExpr expr = ValueConstant.class.isAssignableFrom(leftArg.getClass()) ? leftArg : rightArg;
            ValueExpr var = !ValueConstant.class.isAssignableFrom(leftArg.getClass()) ? leftArg : rightArg;

            //lhe is a variable
            // find the node element by its var name in the query
            VarNameCollector varName = new VarNameCollector();
            var.visit(varName);

            Optional<AsgEBase<EBase>> byTag = AsgQueryUtil.getByTag(context.getQuery().getStart(), varName.getVarNames().iterator().next());
            if (byTag.isPresent()) {
                //todo populate entity / relation constraints
            } else if (AsgQueryUtil.getByPropTag(query.getStart(), varName.getVarNames().iterator().next()).isPresent()) {
                //populate property constraints
                BaseProp prop = AsgQueryUtil.getByPropTag(query.getStart(), varName.getVarNames().iterator().next()).get();
                Value value = ((ValueConstant) expr).getValue();
                prop.setCon(new Constraint(operator,value.stringValue()));
            }

        }
    }

    public static ConstraintOp operator(Compare.CompareOp op) {
        switch (op) {
            case EQ:
                return ConstraintOp.eq;
            case LE:
                return ConstraintOp.le;
            case GE:
                return ConstraintOp.ge;
            case GT:
                return ConstraintOp.gt;
            case NE:
                return ConstraintOp.ne;
            case LT:
                return ConstraintOp.lt;
            default:
                return ConstraintOp.notEmpty;
        }
    }
}
