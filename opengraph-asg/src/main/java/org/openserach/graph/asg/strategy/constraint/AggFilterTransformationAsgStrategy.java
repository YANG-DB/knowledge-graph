package org.openserach.graph.asg.strategy.constraint;



import org.openserach.graph.asg.strategy.AsgStrategy;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;
import org.opensearch.graph.model.query.aggregation.Agg;

/**
 * this transformation search for Agg elements and push them into the preceding relevant entity/relation filter
 */
public class AggFilterTransformationAsgStrategy implements AsgStrategy {
    //region AsgStrategy Implementation
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        //todo - transform any found Agg into an Eprop / RelProp with relevant constraint
        AsgQueryUtil.elements(query, Agg.class);
    }
    //endregion

    //endregion
}




