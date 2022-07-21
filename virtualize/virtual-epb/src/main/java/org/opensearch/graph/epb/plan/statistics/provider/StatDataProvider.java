package org.opensearch.graph.epb.plan.statistics.provider;



import java.util.Map;

public interface StatDataProvider {
    Iterable<Map<String, Object>> getStatDataItems(
            Iterable<String> indices,
            Iterable<String> types,
            Iterable<String> fields,
            Map<String, Object> constraints);
}
