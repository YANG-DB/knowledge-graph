package org.opensearch.graph.test.framework.providers;





import java.util.Map;

public interface GenericDataProvider {

    Iterable<Map<String, Object>> getDocuments() throws Exception;
}
