package org.opensearch.graph.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opensearch.graph.model.query.EBase;
import org.opensearch.graph.model.query.Query;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opensearch.graph.model.results.*;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.opensearch.graph.model.results.AssignmentsQueryResult.Builder.instance;

/**
 * Created by benishue on 21-Feb-17.
 */
public class AssignmentsQueryResultsTest {
    private ObjectMapper mapper = new ObjectMapper();
    private static AssignmentsQueryResult result1Obj = instance().build();


    @Test
    public void testResults1Serialization() throws IOException, JSONException {
        String result1ActualJSON = mapper.writeValueAsString(result1Obj);
        String result1ExpectedJSONString = "{\n" +
                "  \"resultType\": \"assignments\",\n" +
                "  \"pattern\": {\n" +
                "    \"ont\": \"Dragons\",\n" +
                "    \"name\": \"Q1\"\n" +
                "  },\n" +
                "  \"assignments\": [\n" +
                "    {\n" +
                "      \"entities\": [\n" +
                "        {\n" +
                "          \"eTag\": [\n" +
                "            \"A\",\n" +
                "            \"C\"\n" +
                "          ],\n" +
                "          \"eType\": \"Person\",\n" +
                "          \"properties\": [\n" +
                "            {\n" +
                "              \"pType\": \"1\",\n" +
                "              \"agg\": \"raw\",\n" +
                "              \"value\": \"a\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"pType\": \"3\",\n" +
                "              \"agg\": \"raw\",\n" +
                "              \"value\": 5.35\n" +
                "            }\n" +
                "          ],\n" +
                "          \"attachedProperties\": [\n" +
                "            {\n" +
                "              \"pName\": \"count(relationships)\",\n" +
                "              \"value\": 53\n" +
                "            }\n" +
                "          ],\n" +
                "          \"id\": \"12345678\",\n" +
                "          \"tag\": \"A\",\n" +
                "          \"label\": \"Person\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"relationships\": [\n" +
                "        {\n" +
                "          \"rID\": \"12345678\",\n" +
                "          \"agg\": true,\n" +
                "          \"rType\": \"memberof\",\n" +
                "          \"directional\": true,\n" +
                "          \"eID1\": \"12345678\",\n" +
                "          \"eID2\": \"12345679\",\n" +
                "          \"properties\": [\n" +
                "            {\n" +
                "              \"pType\": \"1\",\n" +
                "              \"agg\": \"max\",\n" +
                "              \"value\": 76\n" +
                "            },\n" +
                "            {\n" +
                "              \"pType\": \"1\",\n" +
                "              \"agg\": \"avg\",\n" +
                "              \"value\": 34.56\n" +
                "            }\n" +
                "          ],\n" +
                "          \"attachedProperties\": [\n" +
                "            {\n" +
                "              \"pName\": \"sum(duration)\",\n" +
                "              \"value\": 124\n" +
                "            }\n" +
                "          ]\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"resultType\": \"assignments\"\n" +
                "}";
//        System.out.println("result1ExpectedJSONString:" + result1ExpectedJSONString);
//        System.out.println("result1ActualJSON:" + result1ActualJSON);
        JSONAssert.assertEquals(result1ExpectedJSONString, result1ActualJSON,false);
    }


    @Test
    public void testDeSerialization() throws Exception {
        String result1ExpectedJson = readJsonToString("results1.json");
        AssignmentsQueryResult resultObj = (new ObjectMapper()).readValue(result1ExpectedJson, new TypeReference<AssignmentsQueryResult<Entity, Relationship>>(){});
        Assert.assertNotNull(resultObj);
        String result1ActualJSON = mapper.writeValueAsString(resultObj);
        JSONAssert.assertEquals(result1ExpectedJson, result1ActualJSON,false);
    }

    private static void createResults1()
    {
        Query pattern = new Query();
        pattern.setOnt("Dragons");
        pattern.setName("Q1");
        pattern.setElements(new ArrayList<EBase>() {});

        List<Assignment> assignments = new ArrayList<Assignment>();
        List<Entity> entities = new ArrayList<Entity>();

        Entity entity = new Entity();
        entity.seteTag(new HashSet<>(Arrays.asList("A", "C")));
        entity.seteID("12345678");
        entity.seteType("Person");

        List<Property> properties = new ArrayList<Property>();
        Property property1 = new Property();
        property1.setpType("1");
        property1.setAgg("raw");
        property1.setValue("a");

        Property property2 = new Property();
        property2.setpType("3");
        property2.setAgg("raw");
        property2.setValue(5.35);

        AttachedProperty attachedProperty = new AttachedProperty();
        attachedProperty.setPName("count(relationships)");
        attachedProperty.setValue(53);

        entity.setProperties(Arrays.asList(property1,property2));
        entity.setAttachedProperties(Arrays.asList(attachedProperty));

        entities.add(entity);


        List<Relationship> relationships = new ArrayList<Relationship>();
        Relationship relationship1 = new Relationship();
        relationship1.setrID("12345678");
        relationship1.setAgg(true);
        relationship1.setrType("memberof");
        relationship1.setDirectional(true);
        relationship1.seteID1("12345678");
        relationship1.seteID2("12345679");

        List<Property> propertiesRelationship = new ArrayList<Property>();
        Property propertyRelationship1 =  new Property();
        propertyRelationship1.setpType("1");
        propertyRelationship1.setAgg("max");
        propertyRelationship1.setValue(76);

        Property propertyRelationship2 =  new Property();
        propertyRelationship2.setpType("1");
        propertyRelationship2.setAgg("avg");
        propertyRelationship2.setValue(34.56);

        AttachedProperty attachedPropertyRelationship1 =  new AttachedProperty();
        attachedPropertyRelationship1.setPName("sum(duration)");
        attachedPropertyRelationship1.setValue(124);

        relationship1.setProperties(Arrays.asList(propertyRelationship1,propertyRelationship2));
        relationship1.setAttachedProperties(Arrays.asList(attachedPropertyRelationship1));

        relationships.add(relationship1);

        Assignment assignment = new Assignment();
        assignment.setEntities(entities);
        assignment.setRelationships(relationships);

        assignments.add(assignment);
        result1Obj.setPattern(pattern);
        result1Obj.setAssignments(assignments);


    }


    private String readJsonToString(String jsonFileName) throws Exception {
        String result = "";
        ClassLoader classLoader = getClass().getClassLoader();
        try {
            result = IOUtils.toString(classLoader.getResourceAsStream("ResultsJsons/" + jsonFileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Before
    public void setup() {
    }

    @BeforeClass
    public static void setUpOnce() {
        createResults1();

    }

}
