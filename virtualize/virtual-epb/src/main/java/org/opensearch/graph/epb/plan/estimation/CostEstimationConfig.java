package org.opensearch.graph.epb.plan.estimation;





public final class CostEstimationConfig {
    private double alpha;
    private double delta;

    public CostEstimationConfig(double alpha,double delta) {
        this.alpha = alpha;
        this.delta = delta;
    }

    public double getAlpha() {
        return alpha;
    }

    public double getDelta() {
        return delta;
    }
}
