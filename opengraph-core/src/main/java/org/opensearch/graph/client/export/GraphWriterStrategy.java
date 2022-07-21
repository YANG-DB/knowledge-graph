package org.opensearch.graph.client.export;





import org.opensearch.graph.client.export.graphml.GraphMLWriter;
import org.opensearch.graph.client.export.graphml.GraphMLTokens;
import org.opensearch.graph.model.transport.cursor.LogicalGraphCursorRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyMap;

public class GraphWriterStrategy {
    private Map<LogicalGraphCursorRequest.GraphFormat, GraphWriter> writerMap;

    public GraphWriterStrategy() {
        this.writerMap = new HashMap<>();
        this.writerMap.put(LogicalGraphCursorRequest.GraphFormat.XML,
                new GraphMLWriter(true, emptyMap(), emptyMap(), GraphMLTokens.GRAPHML_XMLNS, GraphMLTokens.LABEL, GraphMLTokens.LABEL));

    }

    public Optional<GraphWriter> writer(LogicalGraphCursorRequest.GraphFormat format) {
        return Optional.ofNullable(writerMap.get(format));
    }
}
