package org.opensearch.graph.stats;

import org.opensearch.graph.stats.model.bucket.BucketRange;
import org.opensearch.graph.stats.model.bucket.BucketTerm;
import org.opensearch.graph.stats.model.enums.DataType;
import org.opensearch.graph.stats.model.result.StatRangeResult;
import org.opensearch.graph.stats.model.result.StatTermResult;
import org.opensearch.graph.stats.util.SearchStatsUtil;
import org.opensearch.graph.stats.util.StatTestUtil;
import org.opensearch.graph.stats.util.StatUtil;
import javaslang.collection.Stream;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.random.EmpiricalDistribution;
import org.opensearch.action.admin.indices.delete.DeleteIndexRequest;
import org.opensearch.action.admin.indices.refresh.RefreshRequest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opensearch.graph.test.framework.index.MappingFileConfigurer;
import org.opensearch.graph.test.framework.populator.SearchEngineDataPopulator;

import java.util.*;

import static org.opensearch.graph.stats.StatTestSuite.dataClient;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by benishue on 24/05/2017.
 */
public class SearchStatsUtilTest {
    private static Iterable<Map<String, Object>> dragonsList;

    private static final int NUM_OF_DRAGONS_IN_INDEX = 1000;
    private static final String STAT_INDEX_NAME = "stat";
    private static final String DATA_INDEX_NAME = "index5";
    private static final String DATA_TYPE_NAME = "Dragon";
    private static final String DATA_FIELD_NAME_AGE = "age";
    private static final String DATA_FIELD_NAME_ADDRESS = "address";
    private static final String DATA_FIELD_NAME_GENDER = "gender";

    private static final int DRAGON_MIN_AGE = 1;
    private static final int DRAGON_MAX_AGE = 10;
    private static final int DRAGON_ADDRESS_LENGTH = 20;
    private static final int DRAGON_NAME_PREFIX_LENGTH = 10;
    private static final List<String> DRAGON_COLORS =
            Arrays.asList("red", "green", "yellow", "blue");
    private static final List<String> DRAGON_GENDERS =
            Arrays.asList("male", "female");


    @Test
    public void getNumericHistogramResultsTest() throws Exception {
        final int numOfBins = 10;
        final double min = DRAGON_MIN_AGE;
        final double max = DRAGON_MAX_AGE;

        List<BucketRange<? extends Number>> numericBuckets = new ArrayList<>(StatUtil.createDoubleBuckets(
                min,
                max,
                numOfBins));

        List<StatRangeResult<? extends Number>> numericHistogramResults = SearchStatsUtil.getNumericHistogramResults(
                dataClient,
                DATA_INDEX_NAME,
                DATA_TYPE_NAME,
                DATA_FIELD_NAME_AGE,
                DataType.numericDouble,
                numericBuckets);

        //Checking that we have 1o buckets
        assertEquals(numOfBins, numericHistogramResults.size());
        //Checking that the lower bound of the left most bucket is MIN
        assertEquals(min, numericHistogramResults.get(0).getLowerBound());
        //Checking that the upper bound of the right most bucket is MAX
        assertEquals(max, numericHistogramResults.get(numOfBins - 1).getUpperBound());

        //Comparing our function of buckets creation with the one of Apache Math
        double[] data = {min, 8.2, 6.333, 1.4, 1.5, 4.2, 7.3, 9.4, 1.1, max};
        EmpiricalDistribution distribution = new EmpiricalDistribution(numOfBins);
        distribution.load(data);

        List<Double> upperBoundsApache = Stream.ofAll(distribution.getUpperBounds()).sorted().toJavaList();
        List<Double> upperBoundsOurs = Stream.ofAll(numericHistogramResults).map(statRangeResult -> (Double) statRangeResult.getUpperBound()).sorted().toJavaList();
        assertTrue(CollectionUtils.isEqualCollection(upperBoundsApache, upperBoundsOurs));

        /*
        Selecting random bin and checking that the number  of elements
        in the bucket is approximately ~ NumOfDragons/NumOfBuckets
        */
        for (int i = 0; i < 10; i++) {
            StatRangeResult statRangeResult = numericHistogramResults.get(new Random().nextInt(numericHistogramResults.size()));
            assertEquals(statRangeResult.getDocCount(), NUM_OF_DRAGONS_IN_INDEX / numOfBins, NUM_OF_DRAGONS_IN_INDEX * 0.2);
        }
    }

    @Test
    public void getManualHistogramResultsTest() throws Exception {
        BucketRange<Double> bucketRange_1dot0_TO_1dot5 = new BucketRange<>(1.0, 1.5);
        List<BucketRange<Double>> manualBuckets = Arrays.asList(
                bucketRange_1dot0_TO_1dot5,
                new BucketRange<>(1.5, 2.0),
                new BucketRange<>(2.0, 2.5)
        );

        StatRangeResult<? extends Number> statRangeResult_1dot0_TO_1dot5 = new StatRangeResult<>(DATA_INDEX_NAME,
                DATA_TYPE_NAME,
                DATA_FIELD_NAME_AGE,
                "don't care",
                DataType.numericDouble,
                bucketRange_1dot0_TO_1dot5.getStart(),
                bucketRange_1dot0_TO_1dot5.getEnd(),
                0,
                0);

        dragonsList.forEach(dragon -> {
            double age = ((Number) dragon.get(DATA_FIELD_NAME_AGE)).doubleValue();
            double lowerBoundFirstBucket = (bucketRange_1dot0_TO_1dot5.getStart()).doubleValue();
            double upperBoundFirstBucket = (bucketRange_1dot0_TO_1dot5.getEnd()).doubleValue();

            if (age >= lowerBoundFirstBucket && age < upperBoundFirstBucket) {
                statRangeResult_1dot0_TO_1dot5.setDocCount(statRangeResult_1dot0_TO_1dot5.getDocCount() + 1);
            }
        });

        List<StatRangeResult<Double>> manualHistogramResults = SearchStatsUtil.getManualHistogramResults(dataClient,
                DATA_INDEX_NAME,
                DATA_TYPE_NAME,
                DATA_FIELD_NAME_AGE,
                DataType.numericDouble,
                manualBuckets);
        assertEquals(manualBuckets.size(), manualHistogramResults.size());

        assertEquals(statRangeResult_1dot0_TO_1dot5.getDocCount(), manualHistogramResults.get(0).getDocCount());
    }

    @Test
    public void getStringBucketsStatResultsTest() throws Exception {
        BucketRange<String> stringBucketRange_A_TO_B = new BucketRange<>("a", "b");
        List<BucketRange<String>> stringBuckets = Arrays.asList(
                stringBucketRange_A_TO_B,
                new BucketRange<>("c", "d"));
        StatRangeResult<String> statRangeResult_A_TO_B = new StatRangeResult<>(DATA_INDEX_NAME,
                DATA_TYPE_NAME,
                DATA_FIELD_NAME_ADDRESS,
                "don't care",
                DataType.string,
                stringBucketRange_A_TO_B.getStart(),
                stringBucketRange_A_TO_B.getEnd(),
                0,
                0);


        List<StatRangeResult> stringBucketsStatResults = SearchStatsUtil.getStringBucketsStatResults(dataClient,
                DATA_INDEX_NAME,
                DATA_TYPE_NAME,
                DATA_FIELD_NAME_ADDRESS,
                stringBuckets);

        dragonsList.forEach(dragon -> {
            String address = dragon.get(DATA_FIELD_NAME_ADDRESS).toString();
            if (address.startsWith("a")) {
                statRangeResult_A_TO_B.setDocCount(statRangeResult_A_TO_B.getDocCount() + 1);
            }
        });
        assertEquals(stringBuckets.size(), stringBucketsStatResults.size());
        assertEquals(statRangeResult_A_TO_B.getDocCount(), stringBucketsStatResults.get(0).getDocCount());
    }


    @Test
    public void getTermHistogramResultsTest() throws Exception {
        String randomGender = DRAGON_GENDERS.get(StatTestUtil.randomInt(0, DRAGON_GENDERS.size() - 1));
        BucketTerm<String> bucketTerm = new BucketTerm<>(randomGender);
        List<StatTermResult> termHistogramResults = SearchStatsUtil.getTermHistogramResults(dataClient,
                DATA_INDEX_NAME,
                DATA_TYPE_NAME,
                DATA_FIELD_NAME_GENDER,
                DataType.string,
                Collections.singletonList(bucketTerm)
        );

        //We have only one bucket
        assertEquals(1, termHistogramResults.size());
        //We should get proportional (~equal) docCount for each gender
        assertEquals(NUM_OF_DRAGONS_IN_INDEX / (double) DRAGON_GENDERS.size(),
                termHistogramResults.get(0).getDocCount(),
                NUM_OF_DRAGONS_IN_INDEX * 0.05);
    }

    @Test
    public void checkIfEsIndexExistsTest() throws Exception {
        assertTrue(SearchStatsUtil.isIndexExists(dataClient, DATA_INDEX_NAME));
    }

    @Test
    public void checkIfEsTypeExistsTest() throws Exception {
        assertTrue(SearchStatsUtil.isTypeExists(dataClient, DATA_INDEX_NAME, DATA_TYPE_NAME));

    }

    @Test
    public void checkIfEsDocExistsTest() throws Exception {
        assertTrue(SearchStatsUtil.isDocExists(dataClient,
                DATA_INDEX_NAME,
                DATA_TYPE_NAME,
                Integer.toString(StatTestUtil.randomInt(0, NUM_OF_DRAGONS_IN_INDEX - 1))
        ));
    }

    @Test
    public void getDocumentByIdTest() throws Exception {
        for (int i = 0; i < NUM_OF_DRAGONS_IN_INDEX; i++) {
            Optional<Map<String, Object>> documentById = SearchStatsUtil.getDocumentSourceById(dataClient, DATA_INDEX_NAME, DATA_TYPE_NAME, Integer.toString(i));
            assertTrue(documentById.isPresent());
        }
    }

    @Test
    public void getDocumentTypeByDocIdTest() throws Exception {
        for (int i = 0; i < NUM_OF_DRAGONS_IN_INDEX; i++) {
            Optional<String> documentTypeByDocId = SearchStatsUtil.getDocumentTypeByDocId(dataClient
                    , DATA_INDEX_NAME,
                    Integer.toString(i));

            assertTrue(documentTypeByDocId.isPresent());
            assertEquals(DATA_TYPE_NAME, documentTypeByDocId.get());
        }
    }

    @BeforeClass
    public static void setup() throws Exception {
        dragonsList = StatTestUtil.createDragons(NUM_OF_DRAGONS_IN_INDEX,
                DRAGON_MIN_AGE,
                DRAGON_MAX_AGE,
                DRAGON_NAME_PREFIX_LENGTH,
                DRAGON_COLORS,
                DRAGON_GENDERS,
                DRAGON_ADDRESS_LENGTH);

        new MappingFileConfigurer(DATA_INDEX_NAME, StatTestSuite.MAPPING_DATA_FILE_DRAGON_PATH).configure(dataClient);

        new SearchEngineDataPopulator(
                dataClient,
                DATA_INDEX_NAME,
                "pge",
                "id",
                () -> dragonsList
        ).populate();

        dataClient.admin().indices().refresh(new RefreshRequest(DATA_INDEX_NAME)).actionGet();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        if (dataClient != null) {
            dataClient.admin().indices().delete(new DeleteIndexRequest(DATA_INDEX_NAME)).actionGet();
        }
    }

}