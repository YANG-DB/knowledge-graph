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





import org.opensearch.graph.unipop.controller.common.context.ElementControllerContext;
import org.opensearch.graph.unipop.controller.promise.appender.SearchQueryAppenderBase;
import org.opensearch.graph.unipop.controller.search.AggregationBuilder;
import org.opensearch.graph.unipop.controller.search.QueryBuilder;

@Deprecated
public class ElementGlobalTypeSearchAppender extends SearchQueryAppenderBase<ElementControllerContext> {
    //region SearchQueryAppenderBase Implementation
    @Override
    public boolean append(QueryBuilder queryBuilder, AggregationBuilder aggregationBuilder, ElementControllerContext context) {
       /* OptionalComp<TraversalConstraint> constraint = context.getConstraint();
        if (constraint.isPresent()) {
            TraversalValuesByKeyProvider traversalValuesByKeyProvider = new TraversalValuesByKeyProvider();
            Set<String> labels = traversalValuesByKeyProvider.getValueByKey(context.getConstraint().get().getTraversal(), T.label.getAccessor());

            // If there are labels in the constraint, this appender is not relevant, exit.
            if (!labels.isEmpty())
                return false;
        }
        // If there is no Constraint
        if (context.getElementType() == ElementType.vertex) {
            Iterable<String> vertexLabels = Stream.ofAll(context.getSchemaProvider().getVertexLabels())
                    .map(label -> context.getSchemaProvider().getVertexSchemas(label).get().getTyped())
                    .toJavaList();
            queryBuilder.seekRoot().query().filtered().filter().bool().must().terms(this.getClass().getSimpleName(),"_type", vertexLabels);
        }
        else if (context.getElementType() == ElementType.edge) {
            Iterable<String> edgeLabels = Stream.ofAll(context.getSchemaProvider().getEdgeLabels())
                    .map(label -> context.getSchemaProvider().getEdgeSchema(label).get().getTyped())
                    .toJavaList();
            queryBuilder.seekRoot().query().filtered().filter().bool().must().terms(this.getClass().getSimpleName(),"_type", edgeLabels);
        }*/

        return true;
    }
    //endregion
}
