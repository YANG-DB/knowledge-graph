package org.openserach.graph.asg.translator.cypher.strategies.expressions;




import org.opensearch.graph.model.query.properties.EProp;
import org.opensearch.graph.model.query.properties.RelProp;
import org.opensearch.graph.model.query.properties.constraint.Constraint;

public abstract class BaseExpressionStrategy implements ExpressionStrategies {

    protected EProp addPredicate(int current, String propery, Constraint constraint) {
        return new EProp(current + 1, propery, constraint);
    }

    protected RelProp addRelPredicate(int current, String propery, Constraint constraint) {
        return new RelProp(current + 1, propery, constraint);
    }

}
