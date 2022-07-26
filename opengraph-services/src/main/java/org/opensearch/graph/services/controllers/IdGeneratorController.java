package org.opensearch.graph.services.controllers;




import org.opensearch.graph.model.transport.ContentResponse;

import java.util.List;

/**
 * Created by roman.margolis on 20/03/2018.
 */
public interface IdGeneratorController<TId> {

    String IDGENERATOR_INDEX = ".idgenerator";

    ContentResponse<TId> getNext(String genName, int numIds);
    ContentResponse<Boolean> init(List<String> names);
}
