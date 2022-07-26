package org.opensearch.graph.unipop.controller.common.appender;





import org.opensearch.graph.model.GlobalConstants;
import org.opensearch.graph.unipop.controller.common.context.ElementControllerContext;
import org.opensearch.graph.unipop.controller.search.SearchBuilder;
import org.opensearch.graph.unipop.controller.utils.CollectionUtil;
import org.opensearch.graph.unipop.controller.utils.traversal.TraversalHasStepFinder;
import org.opensearch.graph.unipop.controller.utils.traversal.TraversalValuesByKeyProvider;
import org.opensearch.graph.unipop.schemaProviders.GraphElementSchema;
import org.opensearch.graph.unipop.schemaProviders.indexPartitions.IndexPartitions;
import org.opensearch.graph.unipop.structure.ElementType;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Compare;
import org.apache.tinkerpop.gremlin.process.traversal.Contains;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.HasStep;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ElementIndexSearchAppender implements SearchAppender<ElementControllerContext> {
    //region SearchAppender Implementation
    @Override
    public boolean append(SearchBuilder searchBuilder, ElementControllerContext context) {
        Set<String> labels = Collections.emptySet();
        if (context.getConstraint().isPresent()) {
            TraversalValuesByKeyProvider traversalValuesByKeyProvider = new TraversalValuesByKeyProvider();
            labels = traversalValuesByKeyProvider.getValueByKey(context.getConstraint().get().getTraversal(), T.label.getAccessor());
        }

        if (labels.isEmpty()) {
            labels = Stream.ofAll(context.getElementType().equals(ElementType.vertex) ?
                    context.getSchemaProvider().getVertexLabels() :
                    context.getSchemaProvider().getEdgeLabels()).toJavaSet();
        }

        Set<String> indices =
                Stream.ofAll(labels)
                .flatMap(label -> context.getElementType().equals(ElementType.vertex) ?
                            context.getSchemaProvider().getVertexSchemas(label) :
                            context.getSchemaProvider().getEdgeSchemas(label))
                .map(GraphElementSchema::getIndexPartitions)
                .flatMap(indexPartitions -> getIndices(indexPartitions, context))
                .toJavaSet();

        searchBuilder.getIndices().addAll(indices);
        return indices.size() > 0;
    }
    //endregion

    //region Private Methods
    private Iterable<String> getIndices(Optional<IndexPartitions> indexPartitions, ElementControllerContext context) {
        if (!indexPartitions.isPresent()) {
                return Collections.emptyList();
        }

        if (!indexPartitions.get().getPartitionField().isPresent() || !context.getConstraint().isPresent()) {
            return Stream.ofAll(indexPartitions.get().getPartitions()).flatMap(IndexPartitions.Partition::getIndices).toJavaSet();
        }

        String partitionField = indexPartitions.get().getPartitionField().get().equals(GlobalConstants._ID) ?
                T.id.getAccessor() :
                indexPartitions.get().getPartitionField().get();


        //currently supporting only compare eq and contains within
        List<HasStep> hasSteps =
                Stream.ofAll(new TraversalHasStepFinder(hasStep -> hasStep.getHasContainers().get(0).getKey().equals(partitionField) &&
                               (hasStep.getHasContainers().get(0).getBiPredicate().equals(Compare.eq) ||
                                hasStep.getHasContainers().get(0).getBiPredicate().equals(Contains.within)))
                .getValue(context.getConstraint().get().getTraversal())).toJavaList();

        if (hasSteps.isEmpty()) {
            return Stream.ofAll(indexPartitions.get().getPartitions()).flatMap(IndexPartitions.Partition::getIndices).toJavaSet();
        }

        Set<String> indices = Stream.ofAll(indexPartitions.get().getPartitions())
                .filter(partition -> !(partition instanceof IndexPartitions.Partition.Range))
                .flatMap(IndexPartitions.Partition::getIndices)
                .toJavaSet();

        //currently assuming one has step
        HasStep<?> hasStep = hasSteps.get(0);
        List<Object> values = CollectionUtil.listFromObjectValue(hasStep.getHasContainers().get(0).getValue());
        if (!values.isEmpty() && values.get(0) instanceof Comparable) {
            indices.addAll(Stream.ofAll(indexPartitions.get().getPartitions())
                    .filter(partition -> partition instanceof IndexPartitions.Partition.Range)
                    .map(partition -> (IndexPartitions.Partition.Range) partition)
                    .filter(partition -> Stream.ofAll(values).find(value -> partition.isWithin((Comparable)value)).toJavaOptional().isPresent())
                    .flatMap(IndexPartitions.Partition::getIndices)
                    .toJavaSet());
        }

        return indices;
    }
    //endregion
}
