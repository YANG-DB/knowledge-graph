package org.opensearch.graph.unipop.controller.common.appender;

/*-
 * #%L
 * fuse-dv-unipop
 * %%
 * Copyright (C) 2016 - 2022 org.opensearch
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.opensearch.graph.model.GlobalConstants;
import org.opensearch.graph.unipop.controller.common.context.VertexControllerContext;
import org.opensearch.graph.unipop.controller.search.SearchBuilder;
import org.opensearch.graph.unipop.controller.utils.ElementUtil;
import org.opensearch.graph.unipop.controller.utils.traversal.TraversalValuesByKeyProvider;
import org.opensearch.graph.unipop.schemaProviders.GraphEdgeSchema;
import org.opensearch.graph.unipop.schemaProviders.indexPartitions.IndexPartitions;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.opensearch.graph.unipop.controller.common.appender.EdgeUtils.getLabel;

/**
 * Created by roman.margolis on 18/10/2017.
 */
public class EdgeIndexSearchAppender implements SearchAppender<VertexControllerContext> {
    //region SearchAppender Implementation
    @Override
    public boolean append(SearchBuilder searchBuilder, VertexControllerContext context) {
        Set<String> labels = context.getConstraint().isPresent() ?
                new TraversalValuesByKeyProvider().getValueByKey(context.getConstraint().get().getTraversal(), T.label.getAccessor()) :
                Stream.ofAll(context.getSchemaProvider().getEdgeLabels()).toJavaSet();

        //currently assuming a single edge label
        String edgeLabel = Stream.ofAll(labels).get(0);

        //currently assuming a single vertex label in bulk
        String contextVertexLabel = getLabel(context,"?");


        Iterable<GraphEdgeSchema> edgeSchemas = context.getSchemaProvider().getEdgeSchemas(contextVertexLabel, context.getDirection(), edgeLabel);
        if (Stream.ofAll(edgeSchemas).isEmpty()) {
            return false;
        }

        //currently assuming only one relevant schema
        GraphEdgeSchema edgeSchema = Stream.ofAll(edgeSchemas).get(0);

        if (edgeSchema.getIndexPartitions().isPresent()) {
            return false;
        }

        GraphEdgeSchema.End endSchema = edgeSchema.getDirectionSchema().isPresent() ?
                edgeSchema.getEndA().get() :
                context.getDirection().equals(Direction.OUT) ?
                    edgeSchema.getEndA().get() :
                    edgeSchema.getEndB().get();

        if (!endSchema.getIndexPartitions().isPresent()) {
            return false;
        }

        String partitionField = endSchema.getIndexPartitions().get().getPartitionField().get().equals(GlobalConstants._ID) ?
                T.id.getAccessor() :
                endSchema.getIndexPartitions().get().getPartitionField().get();

        boolean isPartitionFieldFullyAvailable =
                Stream.ofAll(context.getBulkVertices())
                        .map(vertex -> ElementUtil.value(vertex, partitionField))
                        .filter(value -> !value.isPresent())
                        .size() == 0;

        List<IndexPartitions.Partition.Range> rangePartitions =
                Stream.ofAll(endSchema.getIndexPartitions().get().getPartitions())
                        .filter(partition -> partition instanceof IndexPartitions.Partition.Range)
                        .map(partition -> (IndexPartitions.Partition.Range)partition)
                        .<Comparable>sortBy(partition -> (Comparable)partition.getTo())
                        .toJavaList();

        Iterable<IndexPartitions.Partition.Range> relevantRangePartitions = rangePartitions;
        if (isPartitionFieldFullyAvailable) {
            List<Comparable> partitionValues = Stream.ofAll(context.getBulkVertices())
                    .map(vertex -> ElementUtil.value(vertex, partitionField))
                    .filter(Optional::isPresent)
                    .map(value -> (Comparable) value.get())
                    .distinct().sorted().toJavaList();

            relevantRangePartitions = findRelevantRangePartitions(rangePartitions, partitionValues);
        }

        Set<String> indices =
                Stream.ofAll(endSchema.getIndexPartitions().get().getPartitions())
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
