package org.opensearch.graph.unipop.controller.common.converter;


import org.unipop.process.ProfilerIfc;

public interface ElementConverter<TElementSource, TElementDest> extends ProfilerIfc {
    Iterable<TElementDest> convert(TElementSource source);
}
