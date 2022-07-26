package org.opensearch.graph.dispatcher.driver;







import java.util.List;

public interface IdGeneratorDriver<TId> {
    TId getNext(String genName, int numIds);
    boolean init(List<String> names);

}
