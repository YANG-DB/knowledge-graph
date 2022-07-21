package org.opensearch.graph.epb.plan.statistics;



import org.opensearch.graph.model.execution.plan.Direction;
import org.opensearch.graph.model.query.EBase;
import org.opensearch.graph.model.query.Rel;
import org.opensearch.graph.model.query.entity.EEntityBase;
import org.opensearch.graph.model.query.properties.EPropGroup;
import org.opensearch.graph.model.query.properties.RelPropGroup;

public interface StatisticsProvider {
    /**
     *
     * @param item
     * @return
     */
    Statistics.SummaryStatistics getNodeStatistics(EEntityBase item);

    /**
     *
     * @param item
     * @param entityFilter
     * @return
     */
    Statistics.SummaryStatistics getNodeFilterStatistics(EEntityBase item, EPropGroup entityFilter);

    /**
     *
     * @param item
     * @param source
     * @return
     */
    Statistics.SummaryStatistics getEdgeStatistics(Rel item, EEntityBase source);

    /**
     *
     * @param item
     * @param entityFilter
     * @param source
     * @return
     */
    Statistics.SummaryStatistics getEdgeFilterStatistics(Rel item, RelPropGroup entityFilter, EEntityBase source);


    //Statistics.SummaryStatistics getRedundantEdgeStatistics(Rel rel, RelPropGroup relPropGroup,DirectionSchema direction);


    Statistics.SummaryStatistics getRedundantNodeStatistics(EEntityBase entity, RelPropGroup relPropGroup);

    /**
     * get average number of edges per node (by label context)
     * @return
     */
    long getGlobalSelectivity(Rel rel, RelPropGroup filter, EBase entity, Direction direction) ;

}
