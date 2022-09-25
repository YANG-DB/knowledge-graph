package org.opensearch.graph.epb.plan.statistics;

/*-
 * #%L
 * virtual-epb
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





import org.opensearch.graph.model.query.properties.constraint.Constraint;
import org.opensearch.graph.model.query.Rel;
import org.opensearch.graph.unipop.schemaProviders.GraphEdgeSchema;
import org.opensearch.graph.unipop.schemaProviders.GraphElementPropertySchema;
import org.opensearch.graph.unipop.schemaProviders.GraphElementSchema;
import org.opensearch.graph.unipop.schemaProviders.GraphVertexSchema;

import java.util.List;

public interface GraphStatisticsProvider {
    Statistics.SummaryStatistics getVertexCardinality(GraphVertexSchema graphVertexSchema);
    Statistics.SummaryStatistics getVertexCardinality(GraphVertexSchema graphVertexSchema, List<String> relevantIndices);
    Statistics.SummaryStatistics getEdgeCardinality(GraphEdgeSchema graphEdgeSchema);
    Statistics.SummaryStatistics getEdgeCardinality(GraphEdgeSchema graphEdgeSchema, List<String> relevantIndices);

    <T extends Comparable<T>> Statistics.HistogramStatistics<T> getConditionHistogram(GraphElementSchema graphElementSchema,
                                                                                      List<String> relevantIndices,
                                                                                      GraphElementPropertySchema graphElementPropertySchema,
                                                                                      Constraint constraint, Class<T> javaType);

    long getGlobalSelectivity(GraphEdgeSchema graphEdgeSchema, Rel.Direction direction, List<String> relevantIndices);

}
