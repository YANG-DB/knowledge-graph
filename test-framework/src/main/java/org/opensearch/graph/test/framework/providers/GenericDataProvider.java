package org.opensearch.graph.test.framework.providers;


import java.util.Map;

/**
 * Created by moti on 3/12/2017.
 */
public interface GenericDataProvider {

    Iterable<Map<String, Object>> getDocuments() throws Exception;
}
