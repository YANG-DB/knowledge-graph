package org.opensearch.graph.model;





import org.opensearch.graph.model.query.quant.QuantType;

public interface Container<T> extends Next<T> {
    QuantType getqType();
}
