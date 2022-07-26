package org.opensearch.graph.epb.plan.statistics;





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
