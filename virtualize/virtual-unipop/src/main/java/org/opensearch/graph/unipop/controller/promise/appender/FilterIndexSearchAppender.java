package org.opensearch.graph.unipop.controller.promise.appender;



import org.opensearch.graph.unipop.controller.common.appender.SearchAppender;
import org.opensearch.graph.unipop.controller.common.context.VertexControllerContext;
import org.opensearch.graph.unipop.controller.search.SearchBuilder;
import org.opensearch.graph.unipop.promise.IdPromise;
import org.opensearch.graph.unipop.schemaProviders.indexPartitions.IndexPartitions;
import org.opensearch.graph.unipop.structure.promise.PromiseVertex;
import javaslang.collection.Stream;

import java.util.Collection;

public class FilterIndexSearchAppender implements SearchAppender<VertexControllerContext> {
    //region SearchAppender Implementation
    @Override
    public boolean append(SearchBuilder searchBuilder, VertexControllerContext context) {
        Collection<String> indices = Stream.ofAll(context.getBulkVertices())
                .map(vertex -> (PromiseVertex)vertex)
                .map(PromiseVertex::getPromise)
                .map(promise -> (IdPromise)promise)
                .map(promise -> promise.getLabel().get())
                .flatMap(label -> context.getSchemaProvider().getVertexSchemas(label))
                .map(schema -> schema.getIndexPartitions().get())
                .flatMap(IndexPartitions::getPartitions)
                .flatMap(IndexPartitions.Partition::getIndices)
                .distinct()
                .toJavaList();

        searchBuilder.getIndices().addAll(indices);
        return true;
    }
    //endregion
}
