package org.opensearch.graph.model.query.properties.projection;




import com.fasterxml.jackson.annotation.JsonInclude;
import org.opensearch.graph.model.query.aggregation.AggLOp;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CalculatedFieldProjection extends Projection {
    private AggLOp expression;

    public CalculatedFieldProjection() {}

    public CalculatedFieldProjection(AggLOp expression) {
        this.expression = expression;
    }

    public void setExpression(AggLOp expression) {
        this.expression = expression;
    }

    public AggLOp getExpression() {
        return expression;
    }
}
