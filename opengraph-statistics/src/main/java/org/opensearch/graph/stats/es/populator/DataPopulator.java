package org.opensearch.graph.stats.es.populator;


import java.io.IOException;

/**
 * Created by moti on 3/16/2017.
 */
public interface DataPopulator {
    void populate() throws IOException;
}
