package org.opensearch.graph.stats.es.providers;



import java.io.IOException;
import java.util.Map;

public interface GenericDataProvider {

    Iterable<Map<String, Object>> getDocuments() throws IOException;
}
