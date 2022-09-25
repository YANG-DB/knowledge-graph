package org.opensearch.graph.epb.plan;

import org.junit.Ignore;
import org.opensearch.graph.dispatcher.epb.PlanPruneStrategy;
import org.opensearch.graph.dispatcher.epb.PlanSelector;
import org.opensearch.graph.dispatcher.epb.PlanValidator;
import org.opensearch.graph.dispatcher.ontology.OntologyProvider;
import org.opensearch.graph.epb.plan.estimation.CostEstimationConfig;
import org.opensearch.graph.epb.plan.estimation.pattern.EntityJoinPattern;
import org.opensearch.graph.epb.plan.estimation.pattern.RegexPatternCostEstimator;
import org.opensearch.graph.epb.plan.estimation.pattern.estimators.EntityJoinPatternCostEstimator;
import org.opensearch.graph.epb.plan.estimation.pattern.estimators.M2PatternCostEstimator;
import org.opensearch.graph.epb.plan.extenders.M2.M2PlanExtensionStrategy;
import org.opensearch.graph.epb.plan.pruners.M2GlobalPruner;
import org.opensearch.graph.epb.plan.pruners.NoPruningPruneStrategy;
import org.opensearch.graph.epb.plan.selectors.AllCompletePlanSelector;
import org.opensearch.graph.epb.plan.selectors.CheapestPlanSelector;
import org.opensearch.graph.epb.plan.statistics.EBaseStatisticsProvider;
import org.opensearch.graph.epb.plan.statistics.GraphStatisticsProvider;
import org.opensearch.graph.epb.plan.statistics.Statistics;
import org.opensearch.graph.epb.plan.validation.M2PlanValidator;
import org.opensearch.graph.epb.utils.PlanMockUtils;
import org.opensearch.graph.model.GlobalConstants;
import org.opensearch.graph.model.OntologyTestUtils;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.PlanAssert;
import org.opensearch.graph.model.execution.plan.PlanWithCost;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.query.Rel;
import org.opensearch.graph.model.query.properties.EProp;
import org.opensearch.graph.model.schema.BaseTypeElement;
import org.opensearch.graph.unipop.controller.utils.traversal.TraversalValuesByKeyProvider;
import org.opensearch.graph.unipop.schemaProviders.*;
import org.opensearch.graph.unipop.schemaProviders.indexPartitions.IndexPartitions;
import org.opensearch.graph.unipop.schemaProviders.indexPartitions.StaticIndexPartitions;
import org.opensearch.graph.unipop.schemaProviders.indexPartitions.TimeSeriesIndexPartitions;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.opensearch.graph.model.query.properties.constraint.Constraint.of;
import static org.opensearch.graph.model.query.properties.constraint.ConstraintOp.eq;

import static org.opensearch.graph.model.OntologyTestUtils.*;
import static org.opensearch.graph.model.asgQuery.AsgQuery.Builder.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.opensearch.graph.model.schema.BaseTypeElement.*;

public class EpbJoinSelectionTests {
    @Before
    public void setup() throws ParseException {
        startTime = DATE_FORMAT.parse("2017-01-01-10").getTime();
        Map<String, Integer> typeCard = new HashMap<>();
        typeCard.put(OWN.getName(), 1000);
        typeCard.put(REGISTERED.getName(), 5000);
        typeCard.put(SUBJECT.getName(), 10000);
        typeCard.put(MEMBER_OF.getName(), 100);
        typeCard.put(FIRE.getName(), 10000);
        typeCard.put(FREEZE.getName(), 50000);
        typeCard.put(DRAGON.name, 10000);
        typeCard.put(PERSON.name, 2000);
        typeCard.put(HORSE.name, 4600);
        typeCard.put(GUILD.name, 50);
        typeCard.put(KINGDOM.name, 15);

        graphStatisticsProvider = mock(GraphStatisticsProvider.class);
        when(graphStatisticsProvider.getEdgeCardinality(any())).thenAnswer(invocationOnMock -> {
            GraphEdgeSchema edgeSchema = invocationOnMock.getArgument(0, GraphEdgeSchema.class);
            List<String> indices = Stream.ofAll(edgeSchema.getIndexPartitions().get().getPartitions()).flatMap(IndexPartitions.Partition::getIndices).toJavaList();
            return graphStatisticsProvider.getEdgeCardinality(edgeSchema, indices);
        });

        when(graphStatisticsProvider.getEdgeCardinality(any(), any())).thenAnswer(invocationOnMock -> {
            GraphEdgeSchema edgeSchema = invocationOnMock.getArgument(0, GraphEdgeSchema.class);
            List indices = invocationOnMock.getArgument(1, List.class);

            String constraintLabel = Stream.ofAll(
                    new TraversalValuesByKeyProvider().getValueByKey(edgeSchema.getConstraint().getTraversalConstraint(), T.label.getAccessor()))
                    .get(0);

            return new Statistics.SummaryStatistics(typeCard.get(constraintLabel)* indices.size(), typeCard.get(constraintLabel)* indices.size());
        });

        when(graphStatisticsProvider.getVertexCardinality(any())).thenAnswer(invocationOnMock -> {
            GraphVertexSchema vertexSchema = invocationOnMock.getArgument(0, GraphVertexSchema.class);
            List<String> indices = Stream.ofAll(vertexSchema.getIndexPartitions().get().getPartitions()).flatMap(IndexPartitions.Partition::getIndices).toJavaList();
            return graphStatisticsProvider.getVertexCardinality(vertexSchema, indices);
        });

        when(graphStatisticsProvider.getVertexCardinality(any(), any())).thenAnswer(invocationOnMock -> {
            GraphVertexSchema vertexSchema = invocationOnMock.getArgument(0, GraphVertexSchema.class);
            List indices = invocationOnMock.getArgument(1, List.class);

            String constraintLabel = Stream.ofAll(
                    new TraversalValuesByKeyProvider().getValueByKey(vertexSchema.getConstraint().getTraversalConstraint(), T.label.getAccessor()))
                    .get(0);

            return new Statistics.SummaryStatistics(typeCard.get(constraintLabel)*indices.size(), typeCard.get(constraintLabel)*indices.size());
        });

        when(graphStatisticsProvider.getGlobalSelectivity(any(), any(), any())).thenAnswer(invocationOnMock -> {
            GraphEdgeSchema graphEdgeSchema = invocationOnMock.getArgument(0, GraphEdgeSchema.class);
            Rel.Direction direction = invocationOnMock.getArgument(1, Rel.Direction.class);
            if(graphEdgeSchema.getLabel().equals(SUBJECT.getName()) && direction.name().equals(Rel.Direction.L.name())){
                return 10000;
            }
            if(graphEdgeSchema.getLabel().equals(SUBJECT.getName()) && direction.name().equals(Rel.Direction.R)){
                return 1;
            }
            return 5L;
        });

        when(graphStatisticsProvider.getConditionHistogram(any(), any(), any(), any(), eq(String.class))).thenAnswer(invocationOnMock -> {
            GraphElementSchema elementSchema = invocationOnMock.getArgument(0, GraphElementSchema.class);
            List<String> indices = invocationOnMock.getArgument(1, List.class);

            String constraintLabel = Stream.ofAll(
                    new TraversalValuesByKeyProvider().getValueByKey(elementSchema.getConstraint().getTraversalConstraint(), T.label.getAccessor()))
                    .get(0);

            int card = typeCard.get(constraintLabel);
            return createStringHistogram(card, indices.size());
        });

        when(graphStatisticsProvider.getConditionHistogram(any(), any(), any(), any(), eq(Long.class))).thenAnswer(invocationOnMock -> {
            GraphElementSchema elementSchema = invocationOnMock.getArgument(0, GraphElementSchema.class);
            List<String> indices = invocationOnMock.getArgument(1, List.class);

            String constraintLabel = Stream.ofAll(
                    new TraversalValuesByKeyProvider().getValueByKey(elementSchema.getConstraint().getTraversalConstraint(), T.label.getAccessor()))
                    .get(0);

            int card = typeCard.get(constraintLabel);
            return createLongHistogram(card, indices.size());
        });

        when(graphStatisticsProvider.getConditionHistogram(any(), any(), any(), any(), eq(Date.class))).thenAnswer(invocationOnMock -> {
            GraphElementSchema elementSchema = invocationOnMock.getArgument(0, GraphElementSchema.class);
            List<String> indices = invocationOnMock.getArgument(1, List.class);

            String constraintLabel = Stream.ofAll(
                    new TraversalValuesByKeyProvider().getValueByKey(elementSchema.getConstraint().getTraversalConstraint(), T.label.getAccessor()))
                    .get(0);

            int card = typeCard.get(constraintLabel);
            GraphElementPropertySchema propertySchema = invocationOnMock.getArgument(2, GraphElementPropertySchema.class);
            return createDateHistogram(card,elementSchema,propertySchema, indices);
        });

        ont = new Ontology.Accessor(OntologyTestUtils.createDragonsOntologyShort());
        graphElementSchemaProvider = buildSchemaProvider(ont);

        eBaseStatisticsProvider = new EBaseStatisticsProvider(graphElementSchemaProvider, ont, graphStatisticsProvider);
        M2PatternCostEstimator m2PatternCostEstimator = new M2PatternCostEstimator(
                new CostEstimationConfig(0.8, 1),
                (ont) -> eBaseStatisticsProvider,
                new OntologyProvider() {
                    @Override
                    public Optional<Ontology> get(String id) {
                        return Optional.of(ont.get());
                    }

                    @Override
                    public Collection<Ontology> getAll() {
                        return Collections.singleton(ont.get());
                    }

                    @Override
                    public Ontology add(Ontology ontology) {
                return ontology;
             }
                },
                null);
        EntityJoinPatternCostEstimator entityJoinPatternCostEstimator = (EntityJoinPatternCostEstimator)m2PatternCostEstimator.getEstimators().get(EntityJoinPattern.class);

        RegexPatternCostEstimator regexPatternCostEstimator = new RegexPatternCostEstimator(m2PatternCostEstimator);
        entityJoinPatternCostEstimator.setCostEstimator(regexPatternCostEstimator);
        estimator = regexPatternCostEstimator;

        PlanPruneStrategy<PlanWithCost<Plan, PlanDetailedCost>> globalPruner = new M2GlobalPruner();
        PlanValidator<Plan, AsgQuery> validator = new M2PlanValidator();

        PlanPruneStrategy<PlanWithCost<Plan, PlanDetailedCost>> localPruner = new NoPruningPruneStrategy<>();


        globalPlanSelector = new KeepAllPlansSelectorDecorator<>(new CheapestPlanSelector());
        PlanSelector<PlanWithCost<Plan, PlanDetailedCost>, AsgQuery> localPlanSelector = new AllCompletePlanSelector<>();
        planSearcher = new BottomUpPlanSearcher<>(
                new M2PlanExtensionStrategy(new OntologyProvider() {
                    @Override
                    public Ontology add(Ontology ontology) {
                        return ontology;
                    }
                    @Override
                    public Optional<Ontology> get(String id) {
                        return Optional.of(ont.get());
                    }

                    @Override
                    public Collection<Ontology> getAll() {
                        return Collections.singleton(ont.get());
                    }

                }, ont -> graphElementSchemaProvider),
                globalPruner,
                localPruner,
                globalPlanSelector,
                localPlanSelector,
                validator,
                estimator);
    }


    private Statistics.HistogramStatistics<Date> createDateHistogram(long card, GraphElementSchema elementSchema, GraphElementPropertySchema graphElementPropertySchema,List<String> indices) {
        List<Statistics.BucketInfo<Date>> buckets = new ArrayList<>();
        if(elementSchema.getIndexPartitions().get() instanceof TimeSeriesIndexPartitions){
            TimeSeriesIndexPartitions timeSeriesIndexPartition = (TimeSeriesIndexPartitions) elementSchema.getIndexPartitions().get();
            if(timeSeriesIndexPartition.getTimeField().equals(graphElementPropertySchema.getName())){
                for(int i = 0;i<3;i++){
                    Date dt = new Date(startTime - i*60*60*1000);
                    String indexName = timeSeriesIndexPartition.getIndexName(dt);
                    if(indices.contains(indexName)){
                        buckets.add(new Statistics.BucketInfo<>(card, card/10, dt, new Date(startTime - (i-1) * 60*60*1000)));
                    }
                }
                return new Statistics.HistogramStatistics<>(buckets);
            }
        }
        long bucketSize = card * indices.size() / 3;
        for(int i = 0;i < 3;i++){
            Date dt = new Date(startTime - i*60*60*1000);
            buckets.add(new Statistics.BucketInfo<>(bucketSize, bucketSize/10, dt, new Date(startTime - (i-1) * 60*60*1000)));
        }
        return new Statistics.HistogramStatistics<>(buckets);
    }

    private Statistics.HistogramStatistics<Long> createLongHistogram(int card, int numIndices) {
        long bucketSize = card * numIndices / 3;
        List<Statistics.BucketInfo<Long>> bucketInfos = new ArrayList<>();
        bucketInfos.add(new Statistics.BucketInfo<>(bucketSize, bucketSize/10, 0l,1000l));
        bucketInfos.add(new Statistics.BucketInfo<>(bucketSize, bucketSize/10, 1000l,2000l));
        bucketInfos.add(new Statistics.BucketInfo<>(bucketSize, bucketSize/10, 2000l,3000l));
        return new Statistics.HistogramStatistics<>(bucketInfos);
    }

    private Statistics.HistogramStatistics<String> createStringHistogram(int card, int numIndices) {
        long bucketSize = card * numIndices / 3;
        List<Statistics.BucketInfo<String>> bucketInfos = new ArrayList<>();
        bucketInfos.add(new Statistics.BucketInfo<>(bucketSize, bucketSize/10, "a","g"));
        bucketInfos.add(new Statistics.BucketInfo<>(bucketSize, bucketSize/10, "g","o"));
        bucketInfos.add(new Statistics.BucketInfo<>(bucketSize, bucketSize/10, "o","z"));
        return new Statistics.HistogramStatistics<>(bucketInfos);
    }

    private GraphElementSchemaProvider buildSchemaProvider(Ontology.Accessor ont) {
        Iterable<GraphVertexSchema> vertexSchemas =
                Stream.ofAll(ont.entities())
                        .map(entity -> (GraphVertexSchema) new GraphVertexSchema.Impl(
                                Type.of(entity.geteType()),
                                entity.geteType().equals(PERSON.name) ? new StaticIndexPartitions(Arrays.asList("Persons1","Persons2")) :
                                        entity.geteType().equals(DRAGON.name) ? new StaticIndexPartitions(Arrays.asList("Dragons1","Dragons2")) :
                                                new StaticIndexPartitions(Collections.singletonList("idx1"))))
                        .toJavaList();

        Iterable<GraphEdgeSchema> edgeSchemas =
                Stream.ofAll(ont.relations())
                        .map(relation -> (GraphEdgeSchema) new GraphEdgeSchema.Impl(
                                Type.of(relation.getrType()),
                                new GraphElementConstraint.Impl(__.has(T.label, relation.getrType())),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(relation.getePairs().get(0).geteTypeA() + "IdA"),
                                        Optional.of(relation.getePairs().get(0).geteTypeA()))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(relation.getePairs().get(0).geteTypeB() + "IdB"),
                                        Optional.of(relation.getePairs().get(0).geteTypeB()))),
                                Direction.OUT,
                                Optional.of(new GraphEdgeSchema.DirectionSchema.Impl(GlobalConstants.EdgeSchema.DIRECTION, "out", "in")),
                                Optional.empty(),
                                Optional.of(relation.getrType().equals(OWN.getName()) ?
                                        new TimeSeriesIndexPartitions() {
                                            @Override
                                            public Optional<String> getPartitionField() {
                                                return Optional.of(START_DATE.name);
                                            }

                                            @Override
                                            public Iterable<Partition> getPartitions() {
                                                return Collections.singletonList(() ->
                                                        IntStream.range(0, 3).mapToObj(i -> new Date(startTime - 60*60*1000 * i)).
                                                                map(this::getIndexName).collect(Collectors.toList()));
                                            }

                                            @Override
                                            public String getDateFormat() {
                                                return DATE_FORMAT_STRING;
                                            }

                                            @Override
                                            public String getIndexPrefix() {
                                                return INDEX_PREFIX;
                                            }

                                            @Override
                                            public String getIndexFormat() {
                                                return INDEX_FORMAT;
                                            }

                                            @Override
                                            public String getTimeField() {
                                                return START_DATE.name;
                                            }

                                            @Override
                                            public String getIndexName(Date date) {
                                                return String.format(getIndexFormat(), DATE_FORMAT.format(date));
                                            }
                                        } : new StaticIndexPartitions(Collections.singletonList("idx1"))),
                                Collections.emptyList()))
                        .toJavaList();

        return new OntologySchemaProvider(ont.get(), new GraphElementSchemaProvider.Impl(vertexSchemas, edgeSchemas));
    }

    @Test
    @Ignore("Fix test - join step should receive precedence over other steps ")
    public void testJoinPlanSelection(){
            AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                    next(typed(1, PERSON.type)).
                    next(ePropGroup(2, EProp.of(2, NAME.name, of(eq, "abc")))).
                    next(rel(3, OWN.getrType(), Rel.Direction.R).below(relProp(4))).
                    next(typed(5, DRAGON.type)).
                    next(ePropGroup(6)).
                    next(rel(7, SUBJECT.getrType(), Rel.Direction.R).below(relProp(8))).
                    next(typed(9, KINGDOM.type)).
                    next(ePropGroup(10)).
                    next(rel(11, SUBJECT.getrType(), Rel.Direction.L).below(relProp(12))).
                    next(typed(13, DRAGON.type)).
                    next(ePropGroup(14)).
                    next(rel(15, OWN.getrType(), Rel.Direction.L).below(relProp(16))).
                    next(typed(17, PERSON.type)).
                    next(ePropGroup(18,EProp.of(18, NAME.name, of(eq, "abc")))).
                    build();

            PlanWithCost<Plan, PlanDetailedCost> plan = planSearcher.search(query);
            Plan expected = PlanMockUtils.PlanMockBuilder.mock(query).join(PlanMockUtils.PlanMockBuilder.mock(query).entity(1).entityFilter(2).rel(3).relFilter(4).entity(5).entityFilter(6).rel(7).relFilter(8).entity(9).entityFilter(10).plan(),
                    PlanMockUtils.PlanMockBuilder.mock(query).entity(17).entityFilter(18).rel(15, Rel.Direction.R).relFilter(16).entity(13).entityFilter(14).rel(11, Rel.Direction.R).relFilter(12).entity(9).entityFilter(10).plan()).plan();
            PlanAssert.assertEquals(expected, plan.getPlan());
    }

    //region Fields
    private GraphElementSchemaProvider graphElementSchemaProvider;
    private Ontology.Accessor ont;
    private GraphStatisticsProvider graphStatisticsProvider;

    private EBaseStatisticsProvider eBaseStatisticsProvider;
    private RegexPatternCostEstimator estimator;

    protected BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher;
    private KeepAllPlansSelectorDecorator<PlanWithCost<Plan, PlanDetailedCost>, AsgQuery> globalPlanSelector;
    protected long startTime;

    private static String INDEX_PREFIX = "idx-";
    private static String INDEX_FORMAT = "idx-%s";
    private static String DATE_FORMAT_STRING = "yyyy-MM-dd-HH";
    private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_STRING);
    //endregion


    private class KeepAllPlansSelectorDecorator<P, Q> implements PlanSelector<P, Q>{
        private Iterable<P> plans;
        private PlanSelector<P,Q> innerSelector;

        public KeepAllPlansSelectorDecorator(PlanSelector<P, Q> innerSelector) {
            this.innerSelector = innerSelector;
        }

        public Iterable<P> getPlans() {
            return plans;
        }


        @Override
        public Iterable<P> select(Q query, Iterable<P> plans) {
            this.plans = plans;
            return this.innerSelector.select(query, plans);
        }
    }
}
