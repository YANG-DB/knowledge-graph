package org.opensearch.graph.unipop.controller.common.appender;



import org.opensearch.graph.model.GlobalConstants;
import org.opensearch.graph.unipop.controller.common.context.VertexControllerContext;
import org.opensearch.graph.unipop.controller.search.SearchBuilder;
import org.opensearch.graph.unipop.controller.utils.ElementUtil;
import org.opensearch.graph.unipop.schemaProviders.GraphVertexSchema;
import org.opensearch.graph.unipop.schemaProviders.indexPartitions.IndexPartitions;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.*;

import static org.opensearch.graph.unipop.controller.common.appender.EdgeUtils.getLabel;

public class FilterIndexSearchAppender implements SearchAppender<VertexControllerContext> {
    //region SearchAppender Implementation
    @Override
    public boolean append(SearchBuilder searchBuilder, VertexControllerContext context) {
        // currently assuming homogeneous vertex bulk
        String vertexLabel = getLabel(context,"?");

        Iterable<GraphVertexSchema> vertexSchemas = context.getSchemaProvider().getVertexSchemas(vertexLabel);
        if (Stream.ofAll(vertexSchemas).isEmpty()) {
            return false;
        }

        // currently supports a single vertex schema
        GraphVertexSchema vertexSchema = Stream.ofAll(vertexSchemas).get(0);

        Optional<String> partitionField = vertexSchema.getIndexPartitions().get().getPartitionField().isPresent() ?
                    vertexSchema.getIndexPartitions().get().getPartitionField().get().equals(GlobalConstants._ID) ?
                        Optional.of(T.id.getAccessor()) :
                        Optional.of(vertexSchema.getIndexPartitions().get().getPartitionField().get()) :
                Optional.empty();

        boolean isPartitionFieldFullyAvailable = partitionField.isPresent() ?
                Stream.ofAll(context.getBulkVertices())
                        .map(vertex -> ElementUtil.value(vertex, partitionField.get()))
                        .filter(value -> !value.isPresent())
                        .size() == 0 :
                false;

        List<IndexPartitions.Partition.Range> rangePartitions =
                Stream.ofAll(vertexSchema.getIndexPartitions().get().getPartitions())
                        .filter(partition -> partition instanceof IndexPartitions.Partition.Range)
                        .map(partition -> (IndexPartitions.Partition.Range) partition)
                        .<Comparable>sortBy(partition -> (Comparable) partition.getTo())
                        .toJavaList();

        Iterable<IndexPartitions.Partition.Range> relevantRangePartitions = rangePartitions;
        if (isPartitionFieldFullyAvailable) {
            List<Comparable> partitionValues = Stream.ofAll(context.getBulkVertices())
                    .map(vertex -> ElementUtil.value(vertex, partitionField.get()))
                    .filter(Optional::isPresent)
                    .map(value -> (Comparable) value.get())
                    .distinct().sorted().toJavaList();

            relevantRangePartitions = findRelevantRangePartitions(rangePartitions, partitionValues);
        }

        Set<String> indices =
                Stream.ofAll(vertexSchema.getIndexPartitions().get().getPartitions())
                        .filter(partition -> !(partition instanceof IndexPartitions.Partition.Range))
                        .appendAll(relevantRangePartitions)
                        .flatMap(IndexPartitions.Partition::getIndices)
                        .toJavaSet();

        searchBuilder.getIndices().addAll(indices);
        return indices.size() > 0;
    }
    //endregion

    //Private Methods
    private Set<IndexPartitions.Partition.Range> findRelevantRangePartitions(
            List<IndexPartitions.Partition.Range> partitions,
            List<Comparable> values) {
        Set<IndexPartitions.Partition.Range> foundPartitions = new HashSet<>();
        int partitionIndex = 0;
        int valueIndex = 0;

        while(partitionIndex < partitions.size() && valueIndex < values.size()) {
            IndexPartitions.Partition.Range partition = partitions.get(partitionIndex);
            Comparable value = values.get(valueIndex);

            if (partition.isWithin(value)) {
                foundPartitions.add(partition);
                valueIndex++;
            } else if (partition.getTo().compareTo(value) < 0) {
                partitionIndex++;
            } else {
                valueIndex++;
            }
        }

        return foundPartitions;
    }
    //endregion
}
