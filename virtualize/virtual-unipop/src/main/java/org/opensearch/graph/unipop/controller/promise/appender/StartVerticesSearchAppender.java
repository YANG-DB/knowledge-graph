package org.opensearch.graph.unipop.controller.promise.appender;



import org.opensearch.graph.unipop.controller.common.appender.SearchAppender;
import org.opensearch.graph.unipop.controller.common.context.VertexControllerContext;
import org.opensearch.graph.model.GlobalConstants;
import org.opensearch.graph.unipop.controller.search.AggregationBuilder;
import org.opensearch.graph.unipop.controller.search.QueryBuilder;
import org.opensearch.graph.unipop.controller.search.SearchBuilder;
import org.opensearch.graph.unipop.controller.utils.traversal.TraversalQueryTranslator;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;

public class StartVerticesSearchAppender implements SearchAppender<VertexControllerContext> {

    @Override
    public boolean append(SearchBuilder searchBuilder, VertexControllerContext context) {
        Traversal traversal = buildStartVerticesConstraint(context.getBulkVertices());
        QueryBuilder queryBuilder = searchBuilder.getQueryBuilder().seekRoot().query().bool().filter().bool().must();
        AggregationBuilder aggregationBuilder = searchBuilder.getAggregationBuilder().seekRoot();
        TraversalQueryTranslator traversalQueryTranslator = new TraversalQueryTranslator(queryBuilder,aggregationBuilder , false);
        traversalQueryTranslator.visit(traversal);

        return true;

    }

    private Traversal buildStartVerticesConstraint(Iterable<Vertex> vertices) {
        return __.has(GlobalConstants.EdgeSchema.SOURCE_ID, P.within(Stream.ofAll(vertices).map(Element::id).toJavaList()));
    }

}
