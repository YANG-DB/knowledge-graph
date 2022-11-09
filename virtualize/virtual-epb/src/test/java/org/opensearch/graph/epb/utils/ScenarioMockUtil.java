package org.opensearch.graph.epb.utils;

import org.opensearch.graph.epb.plan.statistics.GraphStatisticsProvider;
import org.opensearch.graph.epb.plan.statistics.Statistics;
import org.opensearch.graph.model.GlobalConstants;
import org.opensearch.graph.model.OntologyTestUtils;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.unipop.controller.utils.traversal.TraversalValuesByKeyProvider;
import org.opensearch.graph.unipop.schema.providers.*;
import org.opensearch.graph.unipop.schema.providers.indexPartitions.IndexPartitions;
import org.opensearch.graph.unipop.schema.providers.indexPartitions.StaticIndexPartitions;
import org.opensearch.graph.unipop.schema.providers.indexPartitions.TimeSeriesIndexPartitions;
import org.opensearch.graph.unipop.structure.ElementType;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.T;
import org.opensearch.common.collect.Tuple;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.opensearch.graph.model.schema.BaseTypeElement.*;

/**
 * Created by moti on 5/17/2017.
 */
public class ScenarioMockUtil {
    public ScenarioMockUtil(long nodeScaleFactor, long edgeScaleFactor) {
        this.nodeScaleFactor = nodeScaleFactor;
        this.edgeScaleFactor = edgeScaleFactor;

        this.ont = new Ontology.Accessor(OntologyTestUtils.createDragonsOntologyShort());

        this.graphStatisticsProvider = mock(GraphStatisticsProvider.class);
        when(graphStatisticsProvider.getGlobalSelectivity(any(), any(), any())).thenAnswer(invocationOnMock -> {
            GraphEdgeSchema schema = invocationOnMock.getArgument(0, GraphEdgeSchema.class);
            List<String> indices = (List<String>) invocationOnMock.getArgument(2, List.class);

            String constraintLabel = Stream.ofAll(
                    new TraversalValuesByKeyProvider().getValueByKey(schema.getConstraint().getTraversalConstraint(), org.apache.tinkerpop.gremlin.structure.T.label.getAccessor()))
                    .get(0);

            Long globalSelectivity = this.globalSelectivity.getOrDefault(constraintLabel, 10L);
            return globalSelectivity*indices.size();
        });

        when(graphStatisticsProvider.getVertexCardinality(any())).thenAnswer(invocationOnMock -> {
            GraphVertexSchema vertex = invocationOnMock.getArgument(0, GraphVertexSchema.class);
            IndexPartitions indexPartitions = vertex.getIndexPartitions().get();
            return graphStatisticsProvider.getVertexCardinality(
                    vertex,
                    Stream.ofAll(indexPartitions.getPartitions()).flatMap(IndexPartitions.Partition::getIndices).toJavaList());
        });

        when(graphStatisticsProvider.getVertexCardinality(any(), any())).thenAnswer(invocationOnMock -> {
            GraphVertexSchema graphVertexSchema = invocationOnMock.getArgument(0, GraphVertexSchema.class);
            List<String> indices = invocationOnMock.getArgument(1, List.class);
            return getCardinality(graphVertexSchema, indices,nodeScaleFactor);
        });

        when(graphStatisticsProvider.getEdgeCardinality(any())).thenAnswer(invocationOnMock -> {
            GraphEdgeSchema edge = invocationOnMock.getArgument(0, GraphEdgeSchema.class);
            IndexPartitions indexPartitions = edge.getIndexPartitions().get();
            return graphStatisticsProvider.getEdgeCardinality(
                    edge,
                    Stream.ofAll(indexPartitions.getPartitions()).flatMap(IndexPartitions.Partition::getIndices).toJavaList());
        });

        when(graphStatisticsProvider.getEdgeCardinality(any(), any())).thenAnswer(invocationOnMock -> {
            GraphEdgeSchema edge = invocationOnMock.getArgument(0, GraphEdgeSchema.class);
            List<String> indices = invocationOnMock.getArgument(1, List.class);
            return getCardinality(edge, indices,edgeScaleFactor);
        });

        when(graphStatisticsProvider.getConditionHistogram(any(), any(), any(), any(), eq(String.class))).thenAnswer(invocationOnMock -> {
            List<String> indices = invocationOnMock.getArgument(1, List.class);
            GraphElementPropertySchema propertySchema = invocationOnMock.getArgument(2, GraphElementPropertySchema.class);

                List<Statistics.HistogramStatistics<String>> histograms = IntStream.range(0, indices.size()).mapToObj(i -> (Statistics.HistogramStatistics<String>)histogramPerPropPerIndex.get(propertySchema.getName())).collect(Collectors.toList());
                return Statistics.HistogramStatistics.combine(histograms);
        });

        when(graphStatisticsProvider.getConditionHistogram(any(), any(), any(), any(), eq(Date.class))).thenAnswer(invocationOnMock -> {
            List<String> indices = invocationOnMock.getArgument(1, List.class);
            GraphElementPropertySchema propertySchema = invocationOnMock.getArgument(2, GraphElementPropertySchema.class);

            List<Statistics.HistogramStatistics<Date>> histograms = IntStream.range(0, indices.size()).mapToObj(i -> (Statistics.HistogramStatistics<Date>)histogramPerPropPerIndex.get(propertySchema.getName())).collect(Collectors.toList());
            return Statistics.HistogramStatistics.combine(histograms);
        });

        when(graphStatisticsProvider.getConditionHistogram(any(), any(), any(), any(), eq(Long.class))).thenAnswer(invocationOnMock -> {
            List<String> indices = invocationOnMock.getArgument(1, List.class);
            GraphElementPropertySchema propertySchema = invocationOnMock.getArgument(2, GraphElementPropertySchema.class);

            List<Statistics.HistogramStatistics<Long>> histograms = IntStream.range(0, indices.size()).mapToObj(i -> (Statistics.HistogramStatistics<Long>)histogramPerPropPerIndex.get(propertySchema.getName())).collect(Collectors.toList());
            return Statistics.HistogramStatistics.combine(histograms);
        });
    }

    private Statistics.SummaryStatistics getCardinality(GraphElementSchema graphElementSchema, List<String> indices, long scale) {
        String constraintLabel = Stream.ofAll(
                new TraversalValuesByKeyProvider().getValueByKey(graphElementSchema.getConstraint().getTraversalConstraint(), org.apache.tinkerpop.gremlin.structure.T.label.getAccessor()))
                .get(0);

        return new Statistics.SummaryStatistics(cardinalityPerTypePerIndex.getOrDefault(constraintLabel, 1000L)*indices.size()* scale,
                cardinalityPerTypePerIndex.getOrDefault(constraintLabel, 1000L)*indices.size());
    }

    public ScenarioMockUtil withLayoutRedundancy(String edgeType, String propertyName, String redundantPropertyName){
        if(!redundantProps.containsKey(edgeType)){
            redundantProps.put(edgeType, new HashMap<>());
        }
        redundantProps.get(edgeType).put(propertyName, redundantPropertyName);
        return this;
    }

    public ScenarioMockUtil withTimeSeriesIndex(String type, ElementType elementType, String timeField, int numIndices){
        TimeSeriesIndexPartitions indexPartition = new TimeSeriesIndexPartitions() {
            @Override
            public Optional<String> getPartitionField() {
                return Optional.of(timeField);
            }

            @Override
            public Iterable<Partition> getPartitions() {
                return Collections.singletonList(
                        () ->  IntStream.range(0, numIndices).mapToObj(i -> new Date(scenarioTime - 60*60*1000 * i)).
                                map(this::getIndexName).collect(Collectors.toList()));
            }

            @Override
            public String getDateFormat() {
                return DATE_FORMAT_STRING;
            }

            @Override
            public String getIndexPrefix() {
                return type + INDEX_PREFIX;
            }

            @Override
            public String getIndexFormat() {
                return type + INDEX_FORMAT;
            }

            @Override
            public String getTimeField() {
                return timeField;
            }

            @Override
            public String getIndexName(Date date) {
                return String.format(getIndexFormat(), DATE_FORMAT.format(date));
            }
        };
        indexPartitionMap.put(new Tuple<>(type, elementType), indexPartition);

        for(int i = 0;i<numIndices;i++){

        }

        return this;
    }

    public ScenarioMockUtil withGlobalSelectivity(String edgeType, long gs){
        globalSelectivity.put(edgeType, gs);
        return this;
    }

    public ScenarioMockUtil withElementCardinality(String type, Long cardinality){
        cardinalityPerTypePerIndex.put(type, cardinality);
        return this;
    }

    public ScenarioMockUtil withHistogram(String prop, Statistics.HistogramStatistics histogram){
        histogramPerPropPerIndex.put(prop, histogram);
        return this;
    }

    public ScenarioMockUtil build() {
        this.graphElementSchemaProvider = buildSchemaProvider();
        return this;
    }

    public static ScenarioMockUtil start(long nodeScaleFactor, long edgeScaleFactor){
        return new ScenarioMockUtil(nodeScaleFactor, edgeScaleFactor);
    }


    public Ontology.Accessor getOntologyAccessor() {
        return ont;
    }

    public GraphElementSchemaProvider getGraphElementSchemaProvider() {
        return graphElementSchemaProvider;
    }

    public long getScenarioTime() {
        return scenarioTime;
    }

    public GraphStatisticsProvider getGraphStatisticsProvider() {
        return graphStatisticsProvider;
    }

    public Map<String, Long> getGlobalSelectivity() {
        return globalSelectivity;
    }

    //region Fields
    private GraphElementSchemaProvider buildSchemaProvider() {
        Iterable<GraphVertexSchema> vertexSchemas =
                Stream.ofAll(this.ont.entities())
                        .map(entity -> (GraphVertexSchema) new GraphVertexSchema.Impl(
                                Type.of(entity.geteType()),
                                indexPartitionMap.getOrDefault(new Tuple<>(entity.geteType(), ElementType.vertex),
                                        new StaticIndexPartitions(Collections.singletonList("idx1")))))
                        .toJavaList();

        Iterable<GraphEdgeSchema> edgeSchemas =
                Stream.ofAll(this.ont.relations())
                        .map(relation -> (GraphEdgeSchema) new GraphEdgeSchema.Impl(
                                Type.of(relation.getrType()),
                                new GraphElementConstraint.Impl(__.has(T.label, relation.getrType())),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(relation.getePairs().get(0).geteTypeA() + "IdA"),
                                        Optional.of(relation.getePairs().get(0).geteTypeA()),
                                        Stream.ofAll(this.redundantProps.getOrDefault(relation.getrType(), Collections.emptyMap()).entrySet())
                                                .map(redundantEntry -> (GraphRedundantPropertySchema) new GraphRedundantPropertySchema.Impl(
                                                        redundantEntry.getKey(),
                                                        redundantEntry.getValue(),
                                                        this.ont.pName$(redundantEntry.getKey()).getType()))
                                                .toJavaList())),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(relation.getePairs().get(0).geteTypeB() + "IdB"),
                                        Optional.of(relation.getePairs().get(0).geteTypeB()),
                                        Stream.ofAll(this.redundantProps.getOrDefault(relation.getrType(), Collections.emptyMap()).entrySet())
                                                .map(redundantEntry -> (GraphRedundantPropertySchema) new GraphRedundantPropertySchema.Impl(
                                                        redundantEntry.getKey(),
                                                        redundantEntry.getValue(),
                                                        this.ont.pName$(redundantEntry.getKey()).getType()))
                                                .toJavaList())),
                                Direction.OUT,
                                Optional.of(new GraphEdgeSchema.DirectionSchema.Impl(GlobalConstants.EdgeSchema.DIRECTION, "out", "in")),
                                Optional.empty(),
                                Optional.of(indexPartitionMap.getOrDefault(new Tuple<>(relation.getrType(), ElementType.edge),
                                        new StaticIndexPartitions(Collections.singletonList("idx1")))),
                                Collections.emptyList()))
                        .toJavaList();

        return new OntologySchemaProvider(this.ont.get(), new GraphElementSchemaProvider.Impl(vertexSchemas, edgeSchemas));
    }
    //endregion

    //region Fields
    private Ontology.Accessor ont;
    private GraphElementSchemaProvider graphElementSchemaProvider;
    private Map<String, Map<String, String>> redundantProps = new HashMap<>();
    private Map<Tuple<String, ElementType>, IndexPartitions> indexPartitionMap = new HashMap<>();
    private static String INDEX_PREFIX = "idx-";
    private static String INDEX_FORMAT = "idx-%s";
    private static String DATE_FORMAT_STRING = "yyyy-MM-dd-HH";
    private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_STRING);
    private long scenarioTime = new Date().getTime();
    private GraphStatisticsProvider graphStatisticsProvider;
    private Map<String, Long> globalSelectivity = new HashMap<>();
    private Map<String, Long> cardinalityPerTypePerIndex = new HashMap<>();
    private Map<String, Statistics.HistogramStatistics> histogramPerPropPerIndex = new HashMap<>();

    private long nodeScaleFactor;
    private long edgeScaleFactor;
    //endregion
}
