package org.opensearch.graph.services.mockEngine;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.ontology.OntologyFinalizer;
import org.opensearch.graph.model.transport.ContentResponse;
import org.opensearch.graph.services.TestsConfiguration;
import org.opensearch.graph.test.BaseITMarker;
import io.restassured.http.Header;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static io.restassured.RestAssured.given;

public class CatalogIT implements BaseITMarker {
    @Before
    public void before() throws Exception {
        Assume.assumeTrue(TestsConfiguration.instance.shouldRunTestClass(this.getClass()));
//        TestSuiteAPISuite.setup();//remark when doing IT tests
    }

    @Test
    /**
     * execute query with expected plan result
     */
    public void catalog() throws IOException {
        Ontology ontology = TestUtils.loadOntology("Dragons.json");
        given()
                .contentType("application/json")
                .header(new Header("opengraph-external-id", "test"))
                .with().port(8888)
                .get("/opengraph/catalog/ontology/Dragons")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        String expected = new ObjectMapper().writeValueAsString(ontology);
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        String result = new ObjectMapper().writeValueAsString(contentResponse.getData());
                        Assert.assertEquals(expected,result);
                        return result.equals(expected);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");
    }

    @Test
    /**
     * execute query with expected plan result
     */
    public void catalogs() throws IOException {
        Ontology ontology = OntologyFinalizer.finalize(TestUtils.loadOntology("Dragons.json"));
        given()
                .contentType("application/json")
                .header(new Header("opengraph-external-id", "test"))
                .with().port(8888)
                .get("/opengraph/catalog/ontology")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        String result = new ObjectMapper().writeValueAsString(contentResponse.getData());
                        Assert.assertEquals("[{\"ont\":\"Dragons\",\"directives\":[],\"entityTypes\":[{\"idField\":[\"id\"],\"eType\":\"Person\",\"name\":\"Person\",\"properties\":[\"id\",\"firstName\",\"lastName\",\"gender\",\"birthDate\",\"deathDate\",\"height\",\"name\"]},{\"idField\":[\"id\"],\"eType\":\"Horse\",\"name\":\"Horse\",\"properties\":[\"id\",\"name\",\"weight\",\"maxSpeed\",\"distance\"]},{\"idField\":[\"id\"],\"eType\":\"Dragon\",\"name\":\"Dragon\",\"properties\":[\"id\",\"name\",\"birthDate\",\"power\",\"gender\",\"color\"]},{\"idField\":[\"id\"],\"eType\":\"Kingdom\",\"name\":\"Kingdom\",\"properties\":[\"id\",\"name\",\"king\",\"queen\",\"independenceDay\",\"funds\"]},{\"idField\":[\"id\"],\"eType\":\"Guild\",\"name\":\"Guild\",\"properties\":[\"id\",\"name\",\"description\",\"iconId\",\"url\",\"establishDate\"]}],\"relationshipTypes\":[{\"idField\":[\"id\"],\"rType\":\"own\",\"name\":\"own\",\"directional\":true,\"ePairs\":[{\"eTypeA\":\"Person\",\"sideAIdField\":\"entityA.id\",\"eTypeB\":\"Dragon\",\"sideBIdField\":\"entityB.id\"},{\"eTypeA\":\"Person\",\"sideAIdField\":\"entityA.id\",\"eTypeB\":\"Horse\",\"sideBIdField\":\"entityB.id\"}],\"properties\":[\"id\",\"startDate\",\"endDate\"]},{\"idField\":[\"id\"],\"rType\":\"know\",\"name\":\"know\",\"directional\":true,\"ePairs\":[{\"eTypeA\":\"Person\",\"sideAIdField\":\"entityA.id\",\"eTypeB\":\"Person\",\"sideBIdField\":\"entityB.id\"}],\"properties\":[\"id\",\"startDate\"]},{\"idField\":[\"id\"],\"rType\":\"memberOf\",\"name\":\"memberOf\",\"directional\":true,\"ePairs\":[{\"eTypeA\":\"Person\",\"sideAIdField\":\"entityA.id\",\"eTypeB\":\"Guild\",\"sideBIdField\":\"entityB.id\"}],\"properties\":[\"id\",\"startDate\",\"endDate\"]},{\"idField\":[\"id\"],\"rType\":\"fire\",\"name\":\"fire\",\"directional\":true,\"ePairs\":[{\"eTypeA\":\"Dragon\",\"sideAIdField\":\"entityA.id\",\"eTypeB\":\"Dragon\",\"sideBIdField\":\"entityB.id\"}],\"properties\":[\"id\",\"date\",\"temperature\"]},{\"idField\":[\"id\"],\"rType\":\"freeze\",\"name\":\"freeze\",\"directional\":true,\"ePairs\":[{\"eTypeA\":\"Dragon\",\"sideAIdField\":\"entityA.id\",\"eTypeB\":\"Dragon\",\"sideBIdField\":\"entityB.id\"}],\"properties\":[\"id\",\"startDate\",\"endDate\"]},{\"idField\":[\"id\"],\"rType\":\"originatedIn\",\"name\":\"originatedIn\",\"directional\":true,\"ePairs\":[{\"eTypeA\":\"Dragon\",\"sideAIdField\":\"entityA.id\",\"eTypeB\":\"Kingdom\",\"sideBIdField\":\"entityB.id\"}],\"properties\":[\"id\",\"startDate\"]},{\"idField\":[\"id\"],\"rType\":\"subjectOf\",\"name\":\"subjectOf\",\"directional\":true,\"ePairs\":[{\"eTypeA\":\"Person\",\"sideAIdField\":\"entityA.id\",\"eTypeB\":\"Kingdom\",\"sideBIdField\":\"entityB.id\"}],\"properties\":[\"id\",\"startDate\"]},{\"idField\":[\"id\"],\"rType\":\"registeredIn\",\"name\":\"registeredIn\",\"directional\":true,\"ePairs\":[{\"eTypeA\":\"Guild\",\"sideAIdField\":\"entityA.id\",\"eTypeB\":\"Kingdom\",\"sideBIdField\":\"entityB.id\"},{\"eTypeA\":\"Dragon\",\"sideAIdField\":\"entityA.id\",\"eTypeB\":\"Guild\",\"sideBIdField\":\"entityB.id\"},{\"eTypeA\":\"Horse\",\"sideAIdField\":\"entityA.id\",\"eTypeB\":\"Guild\",\"sideBIdField\":\"entityB.id\"}],\"properties\":[\"id\",\"startDate\"]}],\"properties\":[{\"pType\":\"color\",\"name\":\"color\",\"type\":\"TYPE_Color\"},{\"pType\":\"height\",\"name\":\"height\",\"type\":\"int\"},{\"pType\":\"type\",\"name\":\"type\",\"type\":\"string\"},{\"pType\":\"maxSpeed\",\"name\":\"maxSpeed\",\"type\":\"int\"},{\"pType\":\"firstName\",\"name\":\"firstName\",\"type\":\"string\"},{\"pType\":\"gender\",\"name\":\"gender\",\"type\":\"TYPE_Gender\"},{\"pType\":\"iconId\",\"name\":\"iconId\",\"type\":\"string\"},{\"pType\":\"temperature\",\"name\":\"temperature\",\"type\":\"int\"},{\"pType\":\"independenceDay\",\"name\":\"independenceDay\",\"type\":\"string\"},{\"pType\":\"distance\",\"name\":\"distance\",\"type\":\"int\"},{\"pType\":\"url\",\"name\":\"url\",\"type\":\"string\"},{\"pType\":\"power\",\"name\":\"power\",\"type\":\"int\"},{\"pType\":\"funds\",\"name\":\"funds\",\"type\":\"float\"},{\"pType\":\"date\",\"name\":\"date\",\"type\":\"date\"},{\"pType\":\"description\",\"name\":\"description\",\"type\":\"string\"},{\"pType\":\"weight\",\"name\":\"weight\",\"type\":\"int\"},{\"pType\":\"deathDate\",\"name\":\"deathDate\",\"type\":\"string\"},{\"pType\":\"establishDate\",\"name\":\"establishDate\",\"type\":\"date\"},{\"pType\":\"lastName\",\"name\":\"lastName\",\"type\":\"string\"},{\"pType\":\"king\",\"name\":\"king\",\"type\":\"string\"},{\"pType\":\"endDate\",\"name\":\"endDate\",\"type\":\"date\"},{\"pType\":\"timestamp\",\"name\":\"timestamp\",\"type\":\"date\"},{\"pType\":\"queen\",\"name\":\"queen\",\"type\":\"string\"},{\"pType\":\"birthDate\",\"name\":\"birthDate\",\"type\":\"date\"},{\"pType\":\"name\",\"name\":\"name\",\"type\":\"string\"},{\"pType\":\"startDate\",\"name\":\"startDate\",\"type\":\"date\"},{\"pType\":\"id\",\"name\":\"id\",\"type\":\"string\"}],\"enumeratedTypes\":[{\"eType\":\"TYPE_Gender\",\"values\":[{\"val\":0,\"name\":\"MALE\"},{\"val\":1,\"name\":\"FEMALE\"},{\"val\":2,\"name\":\"OTHER\"}]},{\"eType\":\"TYPE_Color\",\"values\":[{\"val\":0,\"name\":\"RED\"},{\"val\":1,\"name\":\"BLUE\"},{\"val\":2,\"name\":\"GREEN\"},{\"val\":3,\"name\":\"YELLOW\"}]}],\"compositeTypes\":[]}]",result);
                        return contentResponse.getData() != null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");
    }
    @Test
    /**
     * execute query with expected plan result
     */
    public void catalogSchemas() throws IOException {
        Ontology ontology = OntologyFinalizer.finalize(TestUtils.loadOntology("Dragons.json"));
        given()
                .contentType("application/json")
                .header(new Header("opengraph-external-id", "test"))
                .with().port(8888)
                .get("/opengraph/catalog/schema")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        String result = new ObjectMapper().writeValueAsString(contentResponse.getData());
                        Assert.assertEquals("[\"{\\\"vertexSchemas\\\":[{\\\"label\\\":\\\"Kingdom\\\",\\\"constraint\\\":{\\\"traversalConstraint\\\":{\\\"traversal\\\":\\\"[HasStep([~label.eq(Kingdom)])]\\\"}},\\\"routing\\\":{\\\"present\\\":false},\\\"indexPartitions\\\":{\\\"present\\\":true},\\\"properties\\\":[{\\\"name\\\":\\\"independenceDay\\\",\\\"type\\\":\\\"string\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"independenceDay\\\"}]},{\\\"name\\\":\\\"king\\\",\\\"type\\\":\\\"string\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"king\\\"}]},{\\\"name\\\":\\\"queen\\\",\\\"type\\\":\\\"string\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"queen\\\"}]},{\\\"name\\\":\\\"name\\\",\\\"type\\\":\\\"string\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"name\\\"}]},{\\\"name\\\":\\\"funds\\\",\\\"type\\\":\\\"float\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"funds\\\"}]},{\\\"name\\\":\\\"id\\\",\\\"type\\\":\\\"string\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"id\\\"}]}],\\\"schemaElementType\\\":\\\"org.apache.tinkerpop.gremlin.structure.Vertex\\\"},{\\\"label\\\":\\\"Horse\\\",\\\"constraint\\\":{\\\"traversalConstraint\\\":{\\\"traversal\\\":\\\"[HasStep([~label.eq(Horse)])]\\\"}},\\\"routing\\\":{\\\"present\\\":false},\\\"indexPartitions\\\":{\\\"present\\\":true},\\\"properties\\\":[{\\\"name\\\":\\\"distance\\\",\\\"type\\\":\\\"int\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"distance\\\"}]},{\\\"name\\\":\\\"name\\\",\\\"type\\\":\\\"string\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"name\\\"}]},{\\\"name\\\":\\\"weight\\\",\\\"type\\\":\\\"int\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"weight\\\"}]},{\\\"name\\\":\\\"id\\\",\\\"type\\\":\\\"string\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"id\\\"}]},{\\\"name\\\":\\\"maxSpeed\\\",\\\"type\\\":\\\"int\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"maxSpeed\\\"}]}],\\\"schemaElementType\\\":\\\"org.apache.tinkerpop.gremlin.structure.Vertex\\\"},{\\\"label\\\":\\\"Guild\\\",\\\"constraint\\\":{\\\"traversalConstraint\\\":{\\\"traversal\\\":\\\"[HasStep([~label.eq(Guild)])]\\\"}},\\\"routing\\\":{\\\"present\\\":false},\\\"indexPartitions\\\":{\\\"present\\\":true},\\\"properties\\\":[{\\\"name\\\":\\\"iconId\\\",\\\"type\\\":\\\"string\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"iconId\\\"}]},{\\\"name\\\":\\\"name\\\",\\\"type\\\":\\\"string\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"name\\\"}]},{\\\"name\\\":\\\"description\\\",\\\"type\\\":\\\"string\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"description\\\"}]},{\\\"name\\\":\\\"id\\\",\\\"type\\\":\\\"string\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"id\\\"}]},{\\\"name\\\":\\\"establishDate\\\",\\\"type\\\":\\\"date\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"establishDate\\\"}]},{\\\"name\\\":\\\"url\\\",\\\"type\\\":\\\"string\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"url\\\"}]}],\\\"schemaElementType\\\":\\\"org.apache.tinkerpop.gremlin.structure.Vertex\\\"},{\\\"label\\\":\\\"Person\\\",\\\"constraint\\\":{\\\"traversalConstraint\\\":{\\\"traversal\\\":\\\"[HasStep([~label.eq(Person)])]\\\"}},\\\"routing\\\":{\\\"present\\\":false},\\\"indexPartitions\\\":{\\\"present\\\":true},\\\"properties\\\":[{\\\"name\\\":\\\"firstName\\\",\\\"type\\\":\\\"string\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"firstName\\\"}]},{\\\"name\\\":\\\"lastName\\\",\\\"type\\\":\\\"string\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"lastName\\\"}]},{\\\"name\\\":\\\"gender\\\",\\\"type\\\":\\\"TYPE_Gender\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"gender\\\"}]},{\\\"name\\\":\\\"deathDate\\\",\\\"type\\\":\\\"string\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"deathDate\\\"}]},{\\\"name\\\":\\\"name\\\",\\\"type\\\":\\\"string\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"name\\\"}]},{\\\"name\\\":\\\"id\\\",\\\"type\\\":\\\"string\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"id\\\"}]},{\\\"name\\\":\\\"birthDate\\\",\\\"type\\\":\\\"date\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"birthDate\\\"}]},{\\\"name\\\":\\\"height\\\",\\\"type\\\":\\\"int\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"height\\\"}]}],\\\"schemaElementType\\\":\\\"org.apache.tinkerpop.gremlin.structure.Vertex\\\"},{\\\"label\\\":\\\"Dragon\\\",\\\"constraint\\\":{\\\"traversalConstraint\\\":{\\\"traversal\\\":\\\"[HasStep([~label.eq(Dragon)])]\\\"}},\\\"routing\\\":{\\\"present\\\":false},\\\"indexPartitions\\\":{\\\"present\\\":true},\\\"properties\\\":[{\\\"name\\\":\\\"gender\\\",\\\"type\\\":\\\"TYPE_Gender\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"gender\\\"}]},{\\\"name\\\":\\\"color\\\",\\\"type\\\":\\\"TYPE_Color\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"color\\\"}]},{\\\"name\\\":\\\"name\\\",\\\"type\\\":\\\"string\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"name\\\"}]},{\\\"name\\\":\\\"id\\\",\\\"type\\\":\\\"string\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"id\\\"}]},{\\\"name\\\":\\\"power\\\",\\\"type\\\":\\\"int\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"power\\\"}]},{\\\"name\\\":\\\"birthDate\\\",\\\"type\\\":\\\"date\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"birthDate\\\"}]}],\\\"schemaElementType\\\":\\\"org.apache.tinkerpop.gremlin.structure.Vertex\\\"}],\\\"edgeSchemas\\\":[{\\\"label\\\":\\\"fire\\\",\\\"constraint\\\":{\\\"traversalConstraint\\\":{\\\"traversal\\\":\\\"[HasStep([~label.eq(fire)])]\\\"}},\\\"routing\\\":{\\\"present\\\":false},\\\"indexPartitions\\\":{\\\"present\\\":true},\\\"properties\\\":[{\\\"name\\\":\\\"date\\\",\\\"type\\\":\\\"date\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"date\\\"}]},{\\\"name\\\":\\\"temperature\\\",\\\"type\\\":\\\"int\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"temperature\\\"}]},{\\\"name\\\":\\\"id\\\",\\\"type\\\":\\\"string\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"id\\\"}]}],\\\"endA\\\":{\\\"present\\\":true},\\\"endB\\\":{\\\"present\\\":true},\\\"directionSchema\\\":{\\\"present\\\":true},\\\"direction\\\":\\\"OUT\\\",\\\"applications\\\":[\\\"endA\\\"],\\\"schemaElementType\\\":\\\"org.apache.tinkerpop.gremlin.structure.Edge\\\"},{\\\"label\\\":\\\"fire\\\",\\\"constraint\\\":{\\\"traversalConstraint\\\":{\\\"traversal\\\":\\\"[HasStep([~label.eq(fire)])]\\\"}},\\\"routing\\\":{\\\"present\\\":false},\\\"indexPartitions\\\":{\\\"present\\\":true},\\\"properties\\\":[{\\\"name\\\":\\\"date\\\",\\\"type\\\":\\\"date\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"date\\\"}]},{\\\"name\\\":\\\"temperature\\\",\\\"type\\\":\\\"int\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"temperature\\\"}]},{\\\"name\\\":\\\"id\\\",\\\"type\\\":\\\"string\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"id\\\"}]}],\\\"endA\\\":{\\\"present\\\":true},\\\"endB\\\":{\\\"present\\\":true},\\\"directionSchema\\\":{\\\"present\\\":true},\\\"direction\\\":\\\"IN\\\",\\\"applications\\\":[\\\"endA\\\"],\\\"schemaElementType\\\":\\\"org.apache.tinkerpop.gremlin.structure.Edge\\\"},{\\\"label\\\":\\\"originatedIn\\\",\\\"constraint\\\":{\\\"traversalConstraint\\\":{\\\"traversal\\\":\\\"[HasStep([~label.eq(originatedIn)])]\\\"}},\\\"routing\\\":{\\\"present\\\":false},\\\"indexPartitions\\\":{\\\"present\\\":true},\\\"properties\\\":[{\\\"name\\\":\\\"id\\\",\\\"type\\\":\\\"string\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"id\\\"}]},{\\\"name\\\":\\\"startDate\\\",\\\"type\\\":\\\"date\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"startDate\\\"}]}],\\\"endA\\\":{\\\"present\\\":true},\\\"endB\\\":{\\\"present\\\":true},\\\"directionSchema\\\":{\\\"present\\\":true},\\\"direction\\\":\\\"OUT\\\",\\\"applications\\\":[\\\"endA\\\"],\\\"schemaElementType\\\":\\\"org.apache.tinkerpop.gremlin.structure.Edge\\\"},{\\\"label\\\":\\\"originatedIn\\\",\\\"constraint\\\":{\\\"traversalConstraint\\\":{\\\"traversal\\\":\\\"[HasStep([~label.eq(originatedIn)])]\\\"}},\\\"routing\\\":{\\\"present\\\":false},\\\"indexPartitions\\\":{\\\"present\\\":true},\\\"properties\\\":[{\\\"name\\\":\\\"id\\\",\\\"type\\\":\\\"string\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"id\\\"}]},{\\\"name\\\":\\\"startDate\\\",\\\"type\\\":\\\"date\\\",\\\"indexingSchemes\\\":[{\\\"type\\\":\\\"exact\\\",\\\"name\\\":\\\"startDate\\\"}]}],\\\"endA\\\":{\\\"present\\\":true},\\\"endB\\\":{\\\"present\\\":true},\\\"directionSchema\\\":{\\\"present\\\":true},\\\"direction\\\":\\\"IN\\\",\\\"applications\\\":[\\\"endA\\\"],\\\"schemaElementType\\\":\\\"org.apache.tinkerpop.gremlin.structure.Edge\\\"}],\\\"vertexLabels\\\":[\\\"Kingdom\\\",\\\"Horse\\\",\\\"Guild\\\",\\\"Person\\\",\\\"Dragon\\\"],\\\"edgeLabels\\\":[\\\"fire\\\",\\\"originatedIn\\\"],\\\"labelFieldName\\\":{\\\"present\\\":false}}\"]",result);
                        return contentResponse.getData() != null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");
    }

}

