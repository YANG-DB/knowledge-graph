package org.opensearch.graph.dispatcher.epb;



/**
 * Created by moti on 3/27/2017.
 */
public interface CostEstimatorDriver<P, C, TContext,DRV > extends CostEstimator<P, C, TContext>{
    Long count(DRV driver);

}
