package org.opensearch.graph.unipop.controller.common.appender;

/*-
 * #%L
 * virtual-unipop
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





import org.opensearch.graph.unipop.controller.common.context.VertexControllerContext;
import org.opensearch.graph.unipop.controller.search.AggregationBuilder;
import org.opensearch.graph.unipop.controller.search.QueryBuilder;
import org.opensearch.graph.unipop.controller.search.SearchBuilder;
import org.opensearch.graph.unipop.controller.utils.traversal.TraversalQueryTranslator;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.opensearch.graph.unipop.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;

public class FilterBulkSearchAppender implements SearchAppender<VertexControllerContext> {
    //region SearchAppender Implementation
    @Override
    public boolean append(SearchBuilder searchBuilder, VertexControllerContext context) {
        Traversal traversal = buildStartVerticesConstraint(context.getBulkVertices());

        QueryBuilder queryBuilder = searchBuilder.getQueryBuilder().seekRoot().query().bool().filter().bool().must();
        AggregationBuilder aggregationBuilder = searchBuilder.getAggregationBuilder().seekRoot();

        TraversalQueryTranslator traversalQueryTranslator = new TraversalQueryTranslator(queryBuilder,aggregationBuilder , false);
        traversalQueryTranslator.visit(traversal);

        return true;
    }
    //endregion

    //region Private Methods
    private Traversal buildStartVerticesConstraint(Iterable<Vertex> vertices) {
        return __.start().has(T.id, P.within(Stream.ofAll(vertices).map(Element::id).toJavaList()));
    }
    //endregion
}
