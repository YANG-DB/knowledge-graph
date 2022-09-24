package org.opensearch.graph.stats;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.opensearch.graph.model.GlobalConstants;
import org.opensearch.graph.stats.model.bucket.BucketRange;
import org.opensearch.graph.stats.model.bucket.BucketTerm;
import org.opensearch.graph.stats.model.configuration.*;
import org.opensearch.graph.stats.model.enums.DataType;
import org.opensearch.graph.stats.model.histogram.*;
import org.opensearch.graph.stats.util.StatUtil;
import org.opensearch.graph.test.BaseITMarker;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.Arrays;
import java.util.Collections;

/**
 * Created by benishue on 30-Apr-17.
 */
@Ignore
public class StatConfigurationIT implements BaseITMarker {
    @Test
    public void statModelTest() throws Exception {
        StatContainer statContainer = buildStatContainer();

        ObjectMapper mapper = new ObjectMapper();
        String statActualJson = mapper.writeValueAsString(statContainer);
//        System.out.println(statActualJson);
        String statExpectedJson = StatUtil.readJsonToString("src/test/resources/stats_fields_test_with_dynamics.json");

        JSONAssert.assertEquals(statExpectedJson, statActualJson, false);

        StatContainer resultObj = new ObjectMapper().readValue(statExpectedJson, StatContainer.class);
        Assert.assertNotNull(resultObj);

    }

    private StatContainer buildStatContainer() {
        HistogramNumeric histogramDragonAge = HistogramNumeric.Builder.get()
                .withMin(10).withMax(100).withNumOfBins(10).withDataType(DataType.numericLong).build();

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
                        new BucketTerm("male"),
                        new BucketTerm("female")
                )).build();

        HistogramTerm histogramDocType = HistogramTerm.Builder.get()
                .withDataType(DataType.string).withBuckets(Collections.singletonList(
                        new BucketTerm("Dragon")
                )).build();


        HistogramManual histogramFireEntity = HistogramManual.Builder.get()
                .withBuckets(Arrays.asList(
                        new BucketRange("0", "~")
                )).withDataType(DataType.string)
                .build();


        HistogramDynamic histogramFireTimestampEntity = HistogramDynamic.Builder.get()
                .withNumOfBins(10)
                .withDataType(DataType.numericLong)
                .build();


        Field nameField = new Field("name", histogramDragonName);
        Field ageField = new Field("age", histogramDragonAge);
        Field addressField = new Field("address", histogramDragonAddress);
        Field colorField = new Field("color", histogramDragonColor);
        Field genderField = new Field("gender", histogramTerm);
        Field dragonTypeField = new Field("type", histogramDocType);

        Field fireEntityAOutField = new Field(GlobalConstants.EdgeSchema.SOURCE_ID,
                histogramFireEntity,
                Arrays.asList(new Filter("direction", "OUT")));

        Field fireEntityAInField = new Field(GlobalConstants.EdgeSchema.SOURCE_ID,
                histogramFireEntity,
                Arrays.asList(new Filter("direction", "IN")));


        Field fireTimestampField = new Field("timestamp", histogramFireTimestampEntity);

        Type typeDragon = new Type("Dragon", Arrays.asList(ageField, nameField, addressField, colorField, genderField, dragonTypeField));
        Type typeFire = new Type("fire", Arrays.asList(fireEntityAInField, fireEntityAOutField, fireTimestampField));

        Mapping mappingDragon = Mapping.Builder.get().withIndices(Arrays.asList("index1", "index2"))
                .withTypes(Collections.singletonList("Dragon")).build();

        Mapping mappingFire = Mapping.Builder.get().withIndices(Arrays.asList("index3", "index4"))
                .withTypes(Collections.singletonList("fire")).build();


        return StatContainer.Builder.get()
                .withMappings(Arrays.asList(mappingDragon, mappingFire))
                .withTypes(Arrays.asList(typeDragon, typeFire))
                .build();
    }
}