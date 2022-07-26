package org.opensearch.graph.dispatcher.epb;






public interface CostEstimatorDriver<P, C, TContext,DRV > extends CostEstimator<P, C, TContext>{
    Long count(DRV driver);

}
