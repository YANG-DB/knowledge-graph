package org.opensearch.graph.model.query.properties.order;




public class OrderClause {

    public OrderClause(int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }

    private int order;
}
