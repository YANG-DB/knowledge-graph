package org.opensearch.graph.unipop.controller.common.context;


import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;

public interface SelectContext {
    Iterable<HasContainer> getSelectPHasContainers();
}
