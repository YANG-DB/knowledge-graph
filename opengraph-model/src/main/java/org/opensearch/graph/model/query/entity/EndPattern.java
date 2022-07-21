package org.opensearch.graph.model.query.entity;


import org.opensearch.graph.model.query.EBase;
import org.opensearch.graph.model.query.properties.EProp;

import java.util.List;

public interface EndPattern<T extends EBase> {
    T getEndEntity();
    List<EProp> getFilter();
}
