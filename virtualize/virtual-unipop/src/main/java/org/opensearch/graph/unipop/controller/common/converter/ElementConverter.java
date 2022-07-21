package org.opensearch.graph.unipop.controller.common.converter;


import org.unipop.process.ProfilerIfc;

/**
 * Created by roman on 3/16/2015.
 */
public interface ElementConverter<TElementSource, TElementDest> extends ProfilerIfc {
    Iterable<TElementDest> convert(TElementSource source);
}
