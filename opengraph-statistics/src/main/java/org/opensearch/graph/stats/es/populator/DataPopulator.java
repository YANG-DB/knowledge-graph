package org.opensearch.graph.stats.es.populator;



import java.io.IOException;

public interface DataPopulator {
    void populate() throws IOException;
}
