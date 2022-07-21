package org.opensearch.graph.model.execution.plan.entity;




import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.execution.plan.AsgEBasePlanOp;
import org.opensearch.graph.model.query.aggregation.Agg;

/**
 * Created by lior.perry on 20/02/2017.
 */
public class EntityGroupByFilterOp extends AsgEBasePlanOp<Agg> {
    //region Constructor
    public EntityGroupByFilterOp() {
        super(new AsgEBase<>());
    }

    public EntityGroupByFilterOp(String name, String vertexTag, AsgEBase<Agg> agg) {
        super(agg);
        this.name = name;
        this.vertexTag = vertexTag;
    }
    //endregion

    //region Properties
    public String getName() {
        return this.name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public String getVertexTag() {
        return this.vertexTag;
    }

    public void setVertexTag(String value) {
        this.vertexTag = value;
    }

    //endregion

    //region Fields
    private String vertexTag;
    private String name;
    //endregion
}
