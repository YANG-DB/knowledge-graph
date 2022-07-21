package org.opensearch.graph.dispatcher.driver;




import java.util.List;

/**
 * Created by roman.margolis on 20/03/2018.
 */
public interface IdGeneratorDriver<TId> {
    TId getNext(String genName, int numIds);
    boolean init(List<String> names);

}
