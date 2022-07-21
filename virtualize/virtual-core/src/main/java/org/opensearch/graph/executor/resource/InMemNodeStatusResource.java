package org.opensearch.graph.executor.resource;


import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import org.opensearch.graph.dispatcher.resource.store.NodeStatusResource;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class InMemNodeStatusResource implements NodeStatusResource {

    public static final String ID = "id";

    private String name;
    private MetricRegistry registry;

    @Inject
    public InMemNodeStatusResource(MetricRegistry registry) throws UnknownHostException {
        this.name = InetAddress.getLocalHost().getHostAddress();
        this.registry = registry;
    }


    @Override
    public Map<String, Object> getMetrics() {
        return getMetrics(name);
    }

    @Override
    public Map<String, Object> getMetrics(String node) {
        return Collections.unmodifiableMap(logStatus(registry));
    }

    @Override
    public boolean report() {
        return true;
    }

    private Map<String, Object> logStatus(MetricRegistry registry) {
        Map<String, Object> stats = new HashMap<>();
        registry.getMetrics().forEach((key, value) -> {
            switch (key) {
                case "memory.total.used" :
                    stats.put(key, ((Gauge) value).getValue());
                    break;
                case "memory.heap.usage" :
                    stats.put(key, ((Gauge) value).getValue());
                    break;
                case "threads.count" :
                    stats.put(key, ((Gauge) value).getValue());
                    break;
                case "cursor.count" :
                    stats.put(key, ((Counter) value).getCount());
                    break;
            }
        });
        return stats;
    }

}
