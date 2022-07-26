package org.opensearch.graph.model.logical;





import java.util.Map;

public interface Edge  {
    String id();

    String tag();

    String label();

    Map<String,Object> metadata();

    Map<String,Object> fields();

    String source();

    String target();
}
