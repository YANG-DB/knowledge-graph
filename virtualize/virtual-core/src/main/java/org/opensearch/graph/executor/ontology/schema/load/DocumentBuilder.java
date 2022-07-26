package org.opensearch.graph.executor.ontology.schema.load;





import com.fasterxml.jackson.databind.node.ObjectNode;
import org.opensearch.graph.model.resourceInfo.GraphError;
import javaslang.Tuple2;

import java.util.Optional;

public class DocumentBuilder {
    private ObjectNode node;
    private String id;
    private String type;
    private Optional<String> routing;
    private Optional<Tuple2<String,String>> partitionField;
    private GraphError error;

    public DocumentBuilder(GraphError error) {
        this.error = error;
    }

    public DocumentBuilder(ObjectNode node, String id, String type) {
        this(node,id,type,Optional.empty());
    }

    public DocumentBuilder(ObjectNode node, String id, String type, Optional<String> routing) {
        this(node,id,type,routing,Optional.empty());
    }

    public DocumentBuilder(ObjectNode node, String id, String type, Optional<String> routing, Optional<Tuple2<String,String>> partitionField) {
        this.node = node;
        this.id = id;
        this.type = type;
        this.routing = routing;
        this.partitionField = partitionField;
    }


    public ObjectNode getNode() {
        return node;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public Optional<String> getRouting() {
        return routing;
    }

    public Optional<Tuple2<String,String>> getPartitionField() {
        return partitionField;
    }


    public boolean isSuccess() {
        return getError()==null;
    }

    public boolean isFailure() {
        return getError()!=null;
    }

    public GraphError getError() {
        return error;
    }
}
