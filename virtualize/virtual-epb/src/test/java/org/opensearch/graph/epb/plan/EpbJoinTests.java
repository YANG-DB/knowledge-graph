package org.opensearch.graph.epb.plan;

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
import org.opensearch.graph.epb.plan.pruners.NoPruningPruneStrategy;
import org.opensearch.graph.epb.plan.selectors.AllCompletePlanSelector;
import org.opensearch.graph.epb.plan.selectors.CheapestPlanSelector;
import org.opensearch.graph.epb.plan.statistics.EBaseStatisticsProvider;
import org.opensearch.graph.epb.plan.statistics.GraphStatisticsProvider;
import org.opensearch.graph.epb.plan.statistics.Statistics;
import org.opensearch.graph.epb.plan.validation.M2PlanValidator;
import org.opensearch.graph.model.GlobalConstants;
import org.opensearch.graph.model.OntologyTestUtils;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.execution.plan.PlanOp;
import org.opensearch.graph.model.execution.plan.PlanWithCost;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.costs.CountEstimatesCost;
import org.opensearch.graph.model.execution.plan.costs.JoinCost;
import org.opensearch.graph.model.execution.plan.costs.PlanDetailedCost;
import org.opensearch.graph.model.execution.plan.descriptors.PlanWithCostDescriptor;
import org.opensearch.graph.model.execution.plan.entity.EntityJoinOp;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.query.Rel;
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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.opensearch.graph.model.OntologyTestUtils.*;
import static org.opensearch.graph.model.asgQuery.AsgQuery.Builder.*;
import static org.opensearch.graph.model.asgQuery.AsgQuery.Builder.ePropGroup;
import static org.opensearch.graph.model.asgQuery.AsgQuery.Builder.typed;
import static org.opensearch.graph.model.query.quant.QuantType.all;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.opensearch.graph.model.schema.BaseTypeElement.*;

public class EpbJoinTests {
    @Before
    public void setup() throws ParseException {
        startTime = DATE_FORMAT.parse("2017-01-01-10").getTime();
        Map<String, Integer> typeCard = new HashMap<>();
        typeCard.put(OWN.getName(), 1000);
        typeCard.put(REGISTERED.getName(), 5000);
        typeCard.put(MEMBER_OF.getName(), 100);
        typeCard.put(FIRE.getName(), 10000);
        typeCard.put(FREEZE.getName(), 50000);
        typeCard.put(DRAGON.name, 1000);
        typeCard.put(PERSON.name, 200);
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
                    new TraversalValuesByKeyProvider().getValueByKey(edgeSchema.getConstraint().getTraversalConstraint(), org.apache.tinkerpop.gremlin.structure.T.label.getAccessor()))
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
                    new TraversalValuesByKeyProvider().getValueByKey(vertexSchema.getConstraint().getTraversalConstraint(), org.apache.tinkerpop.gremlin.structure.T.label.getAccessor()))
                    .get(0);

            return new Statistics.SummaryStatistics(typeCard.get(constraintLabel)*indices.size(), typeCard.get(constraintLabel)*indices.size());
        });

        when(graphStatisticsProvider.getGlobalSelectivity(any(), any(), any())).thenReturn(10l);

        when(graphStatisticsProvider.getConditionHistogram(any(), any(), any(), any(), eq(String.class))).thenAnswer(invocationOnMock -> {
            GraphElementSchema elementSchema = invocationOnMock.getArgument(0, GraphElementSchema.class);
            List<String> indices = invocationOnMock.getArgument(1, List.class);

            String constraintLabel = Stream.ofAll(
                    new TraversalValuesByKeyProvider().getValueByKey(elementSchema.getConstraint().getTraversalConstraint(), org.apache.tinkerpop.gremlin.structure.T.label.getAccessor()))
                    .get(0);

            int card = typeCard.get(constraintLabel);
            return createStringHistogram(card, indices.size());
        });

        when(graphStatisticsProvider.getConditionHistogram(any(), any(), any(), any(), eq(Long.class))).thenAnswer(invocationOnMock -> {
            GraphElementSchema elementSchema = invocationOnMock.getArgument(0, GraphElementSchema.class);
            List<String> indices = invocationOnMock.getArgument(1, List.class);

            String constraintLabel = Stream.ofAll(
                    new TraversalValuesByKeyProvider().getValueByKey(elementSchema.getConstraint().getTraversalConstraint(), org.apache.tinkerpop.gremlin.structure.T.label.getAccessor()))
                    .get(0);

            int card = typeCard.get(constraintLabel);
            return createLongHistogram(card, indices.size());
        });

        when(graphStatisticsProvider.getConditionHistogram(any(), any(), any(), any(), eq(Date.class))).thenAnswer(invocationOnMock -> {
            GraphElementSchema elementSchema = invocationOnMock.getArgument(0, GraphElementSchema.class);
            List<String> indices = invocationOnMock.getArgument(1, List.class);

            String constraintLabel = Stream.ofAll(
                    new TraversalValuesByKeyProvider().getValueByKey(elementSchema.getConstraint().getTraversalConstraint(), org.apache.tinkerpop.gremlin.structure.T.label.getAccessor()))
                    .get(0);

            int card = typeCard.get(constraintLabel);
            GraphElementPropertySchema propertySchema = invocationOnMock.getArgument(2, GraphElementPropertySchema.class);
            return createDateHistogram(card,elementSchema,propertySchema, indices);
        });

        ont = new Ontology.Accessor(OntologyTestUtils.createDragonsOntologyShort());
        graphElementSchemaProvider = buildSchemaProvider(ont);

        eBaseStatisticsProvider = new EBaseStatisticsProvider(graphElementSchemaProvider, ont, graphStatisticsProvider);
        M2PatternCostEstimator m2PatternCostEstimator = new M2PatternCostEstimator(
                new CostEstimationConfig(1.0, 0.001),
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

        PlanPruneStrategy<PlanWithCost<Plan, PlanDetailedCost>> globalPruner = new NoPruningPruneStrategy<>();
        PlanValidator<Plan, AsgQuery> validator = new M2PlanValidator();

        PlanPruneStrategy<PlanWithCost<Plan, PlanDetailedCost>> localPruner = new NoPruningPruneStrategy<>();


        globalPlanSelector = new KeepAllPlansSelectorDecorator<>(new CheapestPlanSelector());
        PlanSelector<PlanWithCost<Plan, PlanDetailedCost>, AsgQuery> localPlanSelector = new AllCompletePlanSelector<>();
        planSearcher = new BottomUpPlanSearcher<>(
                new M2PlanExtensionStrategy(new OntologyProvider() {
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
    public void testSimplePatternNoJoin(){
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(typed(1, PERSON.type)).
                next(AsgQuery.Builder.ePropGroup(2)).
                next(rel(3, OWN.getrType(), Rel.Direction.R).below(relProp(4))).
                next(typed(5, DRAGON.type)).
                next(AsgQuery.Builder.ePropGroup(6)).
                build();
        PlanWithCost<Plan, PlanDetailedCost> search = planSearcher.search(query);
        PlanWithCostDescriptor.graph(search,true);
        Assert.assertEquals(2,Stream.ofAll(globalPlanSelector.getPlans()).length());

        assertNoJoinPlans(globalPlanSelector.getPlans());

    }

    @Test
    public void test3HopsJoinCreation(){
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(typed(1, PERSON.type)).
                next(AsgQuery.Builder.ePropGroup(2)).
                next(rel(3, OWN.getrType(), Rel.Direction.R).below(relProp(4))).
                next(typed(5, DRAGON.type)).
                next(AsgQuery.Builder.ePropGroup(6)).
                next(rel(7, OWN.getrType(), Rel.Direction.R).below(relProp(8))).
                next(typed(9, DRAGON.type)).
                next(AsgQuery.Builder.ePropGroup(10)).
                build();
        planSearcher.search(query);
        List<PlanWithCost<Plan, PlanDetailedCost>> joinPlans = Stream.ofAll(this.globalPlanSelector.getPlans()).filter(p -> p.getPlan().getOps().stream().anyMatch(op -> op instanceof EntityJoinOp)).toJavaList();
        Assert.assertEquals(2, joinPlans.size());
        Assert.assertTrue(joinPlans.get(0).getCost().getGlobalCost().getCost() == joinPlans.get(1).getCost().getGlobalCost().getCost());
    }

    @Test
    public void test3HopsJoinCreationEConcrete(){
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(concrete(1,"Per", PERSON.type,PERSON.name,"P")).
                next(AsgQuery.Builder.ePropGroup(2)).
                next(rel(3, OWN.getrType(), Rel.Direction.R).below(relProp(4))).
                next(typed(5, DRAGON.type)).
                next(AsgQuery.Builder.ePropGroup(6)).
                next(rel(7, OWN.getrType(), Rel.Direction.R).below(relProp(8))).
                next(typed(9, DRAGON.type)).
                next(AsgQuery.Builder.ePropGroup(10)).
                build();
        planSearcher.search(query);
        List<PlanWithCost<Plan, PlanDetailedCost>> joinPlans = Stream.ofAll(this.globalPlanSelector.getPlans()).filter(p -> p.getPlan().getOps().stream().anyMatch(op -> op instanceof EntityJoinOp)).sorted(Comparator.comparing(p -> p.getPlan().toString())).toJavaList();
        Assert.assertEquals(2, joinPlans.size());
        Assert.assertTrue(joinPlans.get(0).getCost().getGlobalCost().getCost() == joinPlans.get(1).getCost().getGlobalCost().getCost());

        PlanWithCost<Plan, CountEstimatesCost> joinCost = joinPlans.get(0).getCost().getPlanStepCosts().iterator().next();
        PlanDetailedCost rightBranchCost = ((JoinCost) joinCost.getCost()).getRightBranchCost();
        Assert.assertEquals(Stream.ofAll(rightBranchCost.getPlanStepCosts()).last().getCost().peek(), 10,0.01);
        Assert.assertEquals(Stream.ofAll(rightBranchCost.getPlanStepCosts()).last().getCost().getCountEstimates().pop(), 10,0.01);
        Assert.assertEquals(Stream.ofAll(rightBranchCost.getPlanStepCosts()).last().getCost().getCountEstimates().pop(), 2000,0.01);
    }

    @Test
    public void test4HopsJoinCreation(){
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(typed(1, PERSON.type)).
                next(AsgQuery.Builder.ePropGroup(2)).
                next(rel(3, OWN.getrType(), Rel.Direction.R).below(relProp(4))).
                next(typed(5, DRAGON.type)).
                next(AsgQuery.Builder.ePropGroup(6)).
                next(rel(7, OWN.getrType(), Rel.Direction.R).below(relProp(8))).
                next(typed(9, DRAGON.type)).
                next(AsgQuery.Builder.ePropGroup(10)).
                next(rel(11, OWN.getrType(), Rel.Direction.R).below(relProp(12))).
                next(typed(13, DRAGON.type)).
                next(AsgQuery.Builder.ePropGroup(14)).
                build();
        PlanWithCost<Plan, PlanDetailedCost> plan = planSearcher.search(query);
        List<PlanWithCost<Plan, PlanDetailedCost>> joinPlans = Stream.ofAll(this.globalPlanSelector.getPlans()).filter(p -> p.getPlan().getOps().stream().anyMatch(op -> op instanceof EntityJoinOp)).toJavaList();
        Assert.assertEquals(joinPlans.stream().map(p -> p.getPlan().toString()).collect(Collectors.toSet()).size(), joinPlans.size());
        Assert.assertEquals(12, joinPlans.size());
        for (PlanWithCost<Plan, PlanDetailedCost> joinPlan : joinPlans) {
            Iterable<Plan> permutations = permute(joinPlan.getPlan());
            for (Plan newPlan : permutations) {
                Assert.assertTrue(joinPlans.stream().anyMatch(p -> p.getPlan().toString().equals(newPlan.toString())));
                Assert.assertEquals(joinPlan.getCost().getGlobalCost().cost, Stream.ofAll(joinPlans).find(p -> p.getPlan().toString().equals(newPlan.toString())).get().getCost().getGlobalCost().cost,0.0001);
            }
        }
    }

    @Test
    public void testStarJoinCreation(){
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(typed(1, PERSON.type)).
                next(quant1(2, all))
                .in(AsgQuery.Builder.ePropGroup(3)
                        , rel(4, OWN.getrType(), Rel.Direction.R).below(relProp(5)).
                                next(typed(6, DRAGON.type).
                                next(AsgQuery.Builder.ePropGroup(7)))
                        , rel(8, OWN.getrType(), Rel.Direction.R).below(relProp(9)).
                            next(typed(10, DRAGON.type).
                            next(AsgQuery.Builder.ePropGroup(11)))
                        , rel(12, OWN.getrType(), Rel.Direction.R).below(relProp(13)).
                                next(typed(14, DRAGON.type).
                                next(AsgQuery.Builder.ePropGroup(15)))
                ).
                build();
        PlanWithCost<Plan, PlanDetailedCost> plan = planSearcher.search(query);
        List<PlanWithCost<Plan, PlanDetailedCost>> joinPlans = Stream.ofAll(this.globalPlanSelector.getPlans()).filter(p -> p.getPlan().getOps().stream().anyMatch(op -> op instanceof EntityJoinOp)).toJavaList();
        Assert.assertEquals(joinPlans.stream().map(p -> p.getPlan().toString()).collect(Collectors.toSet()).size(), joinPlans.size());
        Assert.assertEquals(18, joinPlans.size());
        for (PlanWithCost<Plan, PlanDetailedCost> joinPlan : joinPlans) {
            Iterable<Plan> permutations = permute(joinPlan.getPlan());
            for (Plan newPlan : permutations) {
                Assert.assertTrue(joinPlans.stream().anyMatch(p -> p.getPlan().toString().equals(newPlan.toString())));
                Assert.assertEquals(joinPlan.getCost().getGlobalCost().cost, Stream.ofAll(joinPlans).find(p -> p.getPlan().toString().equals(newPlan.toString())).get().getCost().getGlobalCost().cost,0.0001);
            }
        }
    }

    private Iterable<Plan> permute(Plan plan){
        List<Plan> plans = new ArrayList<>();

        if(plan.getOps().get(0) instanceof EntityJoinOp){
            EntityJoinOp entityJoinOp = (EntityJoinOp) plan.getOps().get(0);

            Iterable<Plan> leftPlans = permute(entityJoinOp.getLeftBranch());
            Iterable<Plan> rightPlans = permute(entityJoinOp.getRightBranch());
            for(Plan leftPlan : leftPlans){
                for(Plan rightPlan : rightPlans){
                    EntityJoinOp newJoinOp = new EntityJoinOp(leftPlan, rightPlan,true);
                    Plan newPlan = new Plan(Stream.of((PlanOp)newJoinOp).appendAll(plan.getOps().stream().skip(1).collect(Collectors.toList())));
                    plans.add(newPlan);

                    newJoinOp = new EntityJoinOp(rightPlan, leftPlan,true);
                    newPlan = new Plan(Stream.of((PlanOp)newJoinOp).appendAll(plan.getOps().stream().skip(1).collect(Collectors.toList())));
                    plans.add(newPlan);
                }
            }
        }else{
            plans.add(plan);
        }

        return plans;
    }


    private void assertNoJoinPlans(Iterable<PlanWithCost<Plan, PlanDetailedCost>> plans) {
        for (PlanWithCost<Plan, PlanDetailedCost> plan : plans) {
            Assert.assertFalse(plan.getPlan().getOps().stream().anyMatch(op -> op instanceof EntityJoinOp));
        }
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
