package org.opensearch.graph.epb.plan.statistics;

import com.codahale.metrics.MetricRegistry;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.ImmutableList;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.opensearch.client.transport.TransportClient;
import org.opensearch.graph.epb.plan.statistics.configuration.StatConfig;
import org.opensearch.graph.epb.plan.statistics.provider.EngineStatDocumentProvider;
import org.opensearch.graph.epb.plan.statistics.provider.EngineStatProvider;
import org.opensearch.graph.epb.plan.statistics.provider.EngineStatisticsGraphProvider;
import org.opensearch.graph.epb.plan.statistics.util.StatConfigTestUtil;
import org.opensearch.graph.model.ontology.*;
import org.opensearch.graph.model.schema.BaseTypeElement;
import org.opensearch.graph.stats.model.bucket.BucketRange;
import org.opensearch.graph.stats.model.bucket.BucketTerm;
import org.opensearch.graph.stats.model.configuration.Field;
import org.opensearch.graph.stats.model.configuration.Mapping;
import org.opensearch.graph.stats.model.configuration.StatContainer;
import org.opensearch.graph.stats.model.configuration.Type;
import org.opensearch.graph.stats.model.enums.DataType;
import org.opensearch.graph.stats.model.histogram.*;
import org.opensearch.graph.stats.model.result.StatRangeResult;
import org.opensearch.graph.stats.model.result.StatTermResult;
import org.opensearch.graph.stats.util.SearchStatsUtil;
import org.opensearch.graph.stats.util.StatUtil;
import org.opensearch.graph.test.framework.index.GlobalSearchEmbeddedNode;
import org.opensearch.graph.test.framework.index.MappingFileConfigurer;
import org.opensearch.graph.test.framework.index.SearchEmbeddedNode;
import org.opensearch.graph.test.framework.populator.SearchEngineDataPopulator;
import org.opensearch.graph.unipop.schemaProviders.*;
import org.opensearch.graph.unipop.schemaProviders.indexPartitions.StaticIndexPartitions;

import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.opensearch.graph.model.schema.BaseTypeElement.Type.*;

/**
 * Created by benishue on 25-May-17.
 */
public class EngineStatisticsGraphProviderTest {
    @Test
    public void getVertexCardinality() throws Exception {
        Ontology.Accessor ont = new Ontology.Accessor(getOntology());
        GraphElementSchemaProvider schemaProvider = buildSchemaProvider(ont);
        GraphVertexSchema vertexDragonSchema = Stream.ofAll(schemaProvider.getVertexSchemas(DATA_TYPE_DRAGON)).get(0);

        EngineStatisticsGraphProvider statisticsGraphProvider = new EngineStatisticsGraphProvider(statConfig,
                new EngineStatProvider(statConfig, new EngineStatDocumentProvider(new MetricRegistry(), () -> statClient, statConfig)),
                Caffeine.newBuilder()
                        .maximumSize(10_000)
                        .expireAfterWrite(5, TimeUnit.MINUTES)
                        .build());

        Map<String, Tuple2<Long, Long>> termStatistics = new HashMap<>();
        //We have 1000 dragons in index1, cardinality is 1
        termStatistics.put(DATA_TYPE_DRAGON, new Tuple2<>(NUM_OF_DRAGONS_IN_INDEX_1, 1L));

        //Populating the Elastic Stat Engine index: 'stat' type: 'termBucket', buckets of statistics
        populateTermStatDocs(VERTEX_INDICES, DATA_TYPE_DRAGON, DATA_FIELD_NAME_TYPE, termStatistics);
        //Checking that the ELASTIC STAT TERM TYPE created

        assertTrue(SearchStatsUtil.isTypeExists(statClient, statConfig.getStatIndexName(), statConfig.getStatTermTypeName()));

        //Sanity Checks
        //We have only 1 index ('stat') in the STAT Elastic Engine
        String[] allIndices = SearchStatsUtil.getAllIndices(statClient);
        assertEquals(1, allIndices.length);
        assertEquals(allIndices[0], statConfig.getStatIndexName());

        //Check that the bucket term exists (the bucket is calculated on the field type which is value is 'Dragon')
        String docId = StatUtil.hashString(VERTEX_INDICES.get(0) + DATA_TYPE_DRAGON + DATA_FIELD_NAME_TYPE + DATA_TYPE_DRAGON);
        Optional<Map<String, Object>> doc6Result = SearchStatsUtil.getDocumentSourceById(statClient, statConfig.getStatIndexName(), statConfig.getStatTermTypeName(), docId);
        assertTrue(doc6Result.isPresent());

        assertEquals(1, (int) doc6Result.get().get(statConfig.getStatCardinalityFieldName()));
        assertEquals((int) doc6Result.get().get(statConfig.getStatCountFieldName()), NUM_OF_DRAGONS_IN_INDEX_1);

        Statistics.SummaryStatistics vertexSummaryStatistics = statisticsGraphProvider.getVertexCardinality(vertexDragonSchema);
    }

    @Test
    public void getVertexCardinality1() throws Exception {

    }

    @Test
    public void getEdgeCardinality() throws Exception {

    }

    @Test
    public void getEdgeCardinality1() throws Exception {

    }

    @Test
    public void getConditionHistogram1() throws Exception {

    }

    @Test
    public void getGlobalSelectivity() throws Exception {

    }

    /**
     * (1) Starting Elastic engine in-memory
     * (2) Loading statistics configuration
     *
     * @throws Exception
     */
    @BeforeClass
    public static void setup() throws Exception {
        statConfig = StatConfigTestUtil.getStatConfig(buildStatContainer());

        searchEmbeddedNode = GlobalSearchEmbeddedNode.getInstance();
        new MappingFileConfigurer(statConfig.getStatIndexName(), MAPPING_STAT_FILE_PATH).configure(SearchEmbeddedNode.getClient());

        statClient = SearchEmbeddedNode.getClient();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        statClient.admin().indices().prepareDelete(statConfig.getStatIndexName()).execute().actionGet();
    }

    /**
     * Populating elastic statistics documents for terms
     *
     * @param indices        Elastic Indices Names
     * @param type           Elastic Type Name (e.g., Person)
     * @param field          Elastic Field Name (e.g., gender, _type)
     * @param termStatistics Map <Key = 'Term', Tuple<Count, SummaryStatistics>, (e.g < 'Key = female', Value = [500, 1] >
     */
    private static void populateTermStatDocs(List<String> indices,
                                             String type,
                                             String field,
                                             Map<String, Tuple2<Long, Long>> termStatistics
    ) throws Exception {
        new SearchEngineDataPopulator(
                statClient,
                statConfig.getStatIndexName(),
                "statBucket",
                "id",
                () -> prepareTermStatisticsDocs(
                        indices,
                        type,
                        field,
                        termStatistics
                )).populate();
        statClient.admin().indices().prepareRefresh(statConfig.getStatIndexName()).execute().actionGet();
    }

    //region Private Methods
    private static StatContainer buildStatContainer() {
        HistogramNumeric histogramDragonAge = HistogramNumeric.Builder.get()
                .withMin(10).withMax(100).withNumOfBins(10).build();

        HistogramString histogramDragonName = HistogramString.Builder.get()
                .withPrefixSize(3)
                .withInterval(10).withNumOfChars(26).withFirstCharCode("97").build();

        HistogramManual histogramDragonAddress = HistogramManual.Builder.get()
                .withBuckets(Arrays.asList(
                        new BucketRange("abc", "dzz"),
                        new BucketRange("efg", "hij"),
                        new BucketRange("klm", "xyz")
                )).withDataType(DataType.string)
                .build();

        HistogramComposite histogramDragonColor = HistogramComposite.Builder.get()
                .withManualBuckets(Arrays.asList(
                        new BucketRange("00", "11"),
                        new BucketRange("22", "33"),
                        new BucketRange("44", "55")
                )).withDataType(DataType.string)
                .withAutoBuckets(HistogramString.Builder.get()
                        .withFirstCharCode("97")
                        .withInterval(10)
                        .withNumOfChars(26)
                        .withPrefixSize(3).build())
                .build();

        HistogramTerm histogramTerm = HistogramTerm.Builder.get()
                .withDataType(DataType.string).withBuckets(Arrays.asList(
                        new BucketTerm(DRAGON_GENDERS.get(0)),
                        new BucketTerm(DRAGON_GENDERS.get(1))
                )).build();

        HistogramTerm histogramDocType = HistogramTerm.Builder.get()
                .withDataType(DataType.string).withBuckets(Collections.singletonList(
                        new BucketTerm(DATA_TYPE_DRAGON)
                )).build();


        Type typeDragon = new Type(DATA_TYPE_DRAGON, Arrays.asList(
                new Field(DATA_FIELD_NAME_AGE, histogramDragonAge),
                new Field(DATA_FIELD_NAME_NAME, histogramDragonName),
                new Field(DATA_FIELD_NAME_ADDRESS, histogramDragonAddress),
                new Field(DATA_FIELD_NAME_COLOR, histogramDragonColor),
                new Field(DATA_FIELD_NAME_GENDER, histogramTerm),
                new Field(DATA_FIELD_NAME_TYPE, histogramDocType)));

        Mapping mapping = Mapping.Builder.get().withIndices(VERTEX_INDICES)
                .withTypes(Collections.singletonList(DATA_TYPE_DRAGON)).build();

        return StatContainer.Builder.get()
                .withMappings(Collections.singletonList(mapping))
                .withTypes(Collections.singletonList(typeDragon))
                .build();
    }

    private GraphElementSchemaProvider buildSchemaProvider(Ontology.Accessor ont) {
        Iterable<GraphVertexSchema> vertexSchemas =
                Stream.ofAll(ont.entities())
                        .map(entity -> (GraphVertexSchema) new GraphVertexSchema.Impl(
                                of(entity.geteType()),
                                new StaticIndexPartitions(VERTEX_INDICES)))
                        .toJavaList();

        Iterable<GraphEdgeSchema> edgeSchemas =
                Stream.ofAll(ont.relations())
                        .map(relation -> (GraphEdgeSchema) new GraphEdgeSchema.Impl(
                                of(relation.getrType()),
                                new GraphElementConstraint.Impl(__.has(T.label, relation.getrType())),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(relation.getePairs().get(0).geteTypeA() + "IdA"),
                                        Optional.of(relation.getePairs().get(0).geteTypeA()))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(relation.getePairs().get(0).geteTypeB() + "IdB"),
                                        Optional.of(relation.getePairs().get(0).geteTypeB()))),
                                Direction.OUT,
                                Optional.of(new GraphEdgeSchema.DirectionSchema.Impl("direction", "out", "in")),
                                Optional.empty(),
                                Optional.of(new StaticIndexPartitions(EDGE_INDICES)),
                                Collections.emptyList()))
                        .toJavaList();

        return new OntologySchemaProvider(ont.get(), new GraphElementSchemaProvider.Impl(vertexSchemas, edgeSchemas));
    }

    private Ontology getOntology() {
        Ontology ontology = Mockito.mock(Ontology.class);
        List<EPair> ePairs = Arrays.asList(new EPair() {{
            seteTypeA(DATA_TYPE_DRAGON);
            seteTypeB(DATA_TYPE_DRAGON);
        }});

        RelationshipType fireRelationshipType = RelationshipType.Builder.get()
                .withRType(DATA_TYPE_FIRE).withName(DATA_TYPE_FIRE).withEPairs(ePairs).build();

        Property nameProp = new Property(DATA_FIELD_NAME_NAME, DATA_FIELD_NAME_NAME, "string");
        Property ageProp = new Property(DATA_FIELD_NAME_AGE, DATA_FIELD_NAME_AGE, "int");

        when(ontology.getProperties()).then(invocationOnMock -> Collections.singleton(nameProp));

        when(ontology.getEntityTypes()).thenAnswer(invocationOnMock ->
                {
                    ArrayList<EntityType> entityTypes = new ArrayList<>();
                    entityTypes.add(EntityType.Builder.get()
                            .withEType(DATA_TYPE_DRAGON).withName(DATA_TYPE_DRAGON)
                            .withProperties(Collections.singletonList(ageProp.getpType()))
                            .build());
                    return entityTypes;
                }
        );

        when(ontology.getRelationshipTypes()).thenAnswer(invocationOnMock ->
                {
                    ArrayList<RelationshipType> relTypes = new ArrayList<>();
                    relTypes.add(fireRelationshipType);
                    return relTypes;
                }
        );

        return ontology;
    }

    /**
     * @param indices
     * @param type
     * @param field
     * @param min
     * @param max
     * @param numOfBins
     * @return List of documents where each numeric bucket count & cardinality = index of bucket
     */
    private static Iterable<Map<String, Object>> prepareNumericStatisticsDocs(List<String> indices,
                                                                              String type,
                                                                              String field,
                                                                              long min,
                                                                              long max,
                                                                              int numOfBins) {
        List<BucketRange<Double>> numericBuckets = StatUtil.createDoubleBuckets(min, max, Math.toIntExact(numOfBins));
        List<StatRangeResult> statRangeResults = new ArrayList<>();
        int j = 0;
        for (BucketRange<Double> bucketRange : numericBuckets) {
            for (String index : indices) {
                StatRangeResult<Double> statRangeResult = new StatRangeResult<>
                        (index, type, field, Integer.toString(j), DataType.numericDouble, bucketRange.getStart(), bucketRange.getEnd(), j, j);
                statRangeResults.add(statRangeResult);
            }
            j++;
        }
        return StatUtil.prepareStatDocs(statConfig.getStatNumericTypeName(), statRangeResults);
    }

    /**
     * @param indices        Elastic Indices Names
     * @param type           Elastic Type Name (e.g., Person)
     * @param field          Elastic Field Name (e.g., gender, _type)
     * @param termStatistics Map <Key = 'Term', Tuple<Count, SummaryStatistics>, (e.g < 'Key = female', Value = [500, 1] >
     * @return list of documents
     */
    private static Iterable<Map<String, Object>> prepareTermStatisticsDocs(List<String> indices,
                                                                           String type,
                                                                           String field,
                                                                           Map<String, Tuple2<Long, Long>> termStatistics) {
        List<StatTermResult> statRangeResults = new ArrayList<>();
        for (Map.Entry<String, Tuple2<Long, Long>> entry : termStatistics.entrySet()) {
            String term = entry.getKey();
            Long count = entry.getValue()._1;
            Long cardinality = entry.getValue()._2;
            for (String index : indices) {
                StatTermResult<String> statTermResult =
                        new StatTermResult<>(index, type, field, term, DataType.string, term, count, cardinality);
                statRangeResults.add(statTermResult);
            }
        }
        return StatUtil.prepareStatDocs(statConfig.getStatTermTypeName(), statRangeResults);
    }
    //endregion

    //region Fields
    private static TransportClient statClient;
    private static SearchEmbeddedNode searchEmbeddedNode;
    private static StatConfig statConfig;

    private static final String MAPPING_STAT_FILE_PATH = Paths.get("src", "test", "resources", "elastic.test.stat.mapping.json").toString();

    private static final long NUM_OF_DRAGONS_IN_INDEX_1 = 1000L;

    private static final String DATA_TYPE_DRAGON = "Dragon";
    private static final String DATA_TYPE_FIRE = "fire";

    private static final String DATA_INDEX_NAME_1 = "index1";
    private static final String DATA_INDEX_NAME_2 = "index2";
    private static final String DATA_INDEX_NAME_3 = "index3";
    private static final String DATA_INDEX_NAME_4 = "index4";

    private static final String DATA_FIELD_NAME_NAME = "name"; //Dragon Name
    private static final String DATA_FIELD_NAME_AGE = "age";
    private static final String DATA_FIELD_NAME_ADDRESS = "address";
    private static final String DATA_FIELD_NAME_COLOR = "color";
    private static final String DATA_FIELD_NAME_GENDER = "gender";
    private static final String DATA_FIELD_NAME_TYPE = "type";

    private static final List<String> DRAGON_GENDERS =
            Arrays.asList("male", "female");

    private static final List<String> VERTEX_INDICES = ImmutableList.of(DATA_INDEX_NAME_1, DATA_INDEX_NAME_2);
    private static final List<String> EDGE_INDICES = ImmutableList.of(DATA_INDEX_NAME_3, DATA_INDEX_NAME_4);
    //endregion
}
